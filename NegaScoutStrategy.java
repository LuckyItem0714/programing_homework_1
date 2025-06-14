package myplayer;

import static ap25.Color.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import ap25.*;

public class NegaScoutStrategy{
    static final int CASHE_HIT_BONUS = 500;
    static final int START_DEPTH = 4;//反復深化の最初
    static final int DEPTH_LIMIT = 7;//反復深化の最後

    MyEval eval = new MyEval();
    final Map<Long, TTEntry> currentTable = new HashMap<>();
    Map<Long, TTEntry> previousTable = new HashMap<>();
    final TTAccessor currentRawTTA = new RawTTAccessor(currentTable);
    final TTAccessor previousRawTTA = new RawTTAccessor(previousTable);

    public NegaScoutStrategy(){}

    public Move search(BitBoard board){
        List<Move> moves = board.findLegalMoves(BLACK);
        if(moves.size() == 1){//着手可能位置が0または1の場合. 
            return moves.get(0);
        }
        currentTable.clear();
        previousTable.clear();

        for(int searchDepth = START_DEPTH; searchDepth <= DEPTH_LIMIT; searchDepth++){
            previousTable = new HashMap<>(currentTable);
            currentTable.clear();
            negaScout(board, Integer.MIN_VALUE, Integer.MAX_VALUE, searchDepth, currentRawTTA);

        }
        return currentRawTTA.lookup(board.getBlack(), board.getWhite()).bestMove;
    }

    int negaScout(BitBoard board, int alpha, int beta, int depth, TTAccessor tta) {
        if (SearchUtil.isTerminal(board, depth)) return eval.value(board);

        int u = Integer.MAX_VALUE;
        int l = Integer.MIN_VALUE;
        TTEntry entry = tta.lookup(board.getBlack(), board.getWhite());
        if (entry != null){
            switch(entry.type){
                case UPPERBOUND:
                    u = entry.value;
                    break;
                case LOWERBOUND:
                    l = entry.value;
                    break;
                case EXACT:
                    return entry.value;
            }
        }
        alpha = Math.max(alpha, l);
        beta = Math.min(beta, u);

        var moves = board.findLegalMoves(BLACK);
        moves = order(board, moves);

        Move move = moves.get(0);
        var newBoard = board.placed(move);
        int v = -negaScout(newBoard.flipped(), -beta, -alpha, depth - 1, tta);
        if(v >= beta){
            if(v > l){
                tta.store(board.getBlack(), board.getWhite(), new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
            }
            return v;
        }
        Move bestMove = null;
        if(v > alpha){
            alpha = v;
            bestMove = move;
        }
        int score = v;
        
        //Null Window Search
        for (int i = 1; i < moves.size(); i++) {
            move = moves.get(i);
            newBoard = board.placed(move).flipped();
            v = -negaAlpha(newBoard, -alpha - 1, -alpha, depth - 1, tta);
            if (v >= beta) {
                if(v > l){
                    tta.store(board.getBlack(), board.getWhite(), new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                }
                return v;
            }
            if(v > alpha){
                alpha = v;
                v = -negaScout(newBoard, -beta, -alpha, depth - 1, tta);
                if(v >= beta){
                    if(v > l){
                        tta.store(board.getBlack(), board.getWhite(), new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                    }
                    return v;
                }
            }
            if(v > alpha){
                alpha = v;
                bestMove = move;
            }
            score = Math.max(score, v);
        }
        if(score < alpha){
            tta.store(board.getBlack(), board.getWhite(), new TTEntry(score, TTEntry.EntryType.UPPERBOUND, null));
        } else{
            tta.store(board.getBlack(), board.getWhite(), new TTEntry(score, TTEntry.EntryType.EXACT, bestMove));
        }
        return score;
    }

    int negaAlpha(BitBoard board, int alpha, int beta, int depth, TTAccessor tta) {
       if (SearchUtil.isTerminal(board, depth)) return eval.value(board);

        int u = Integer.MAX_VALUE;
        int l = Integer.MIN_VALUE;
        TTEntry entry = tta.lookup(board.getBlack(), board.getWhite());
        if (entry != null) {
            switch (entry.type) {
                case LOWERBOUND: 
                    l = entry.value;
                    break;
                case UPPERBOUND:
                    u = entry.value;
                    break;
                 case EXACT:
                    return entry.value;
            }
        }

        alpha = Math.max(alpha, l);
        beta = Math.min(beta, u);

        var moves = board.findLegalMoves(BLACK);
        moves = order(board, moves);

        int score = Integer.MIN_VALUE;
        Move bestMove = null;
        for (var move : moves) {
            var newBoard = board.placed(move);
            int v = -negaAlpha(newBoard.flipped(), -beta, -alpha, depth - 1, tta);
            if(v >= beta){
                if(v > l){
                    tta.store(board.getBlack(), board.getWhite(), new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                }
                return v;
            }
            if(v > alpha){
                alpha = v;
                bestMove = move;
            }
            score = Math.max(score, v);
        }

        if(score <= alpha){
            tta.store(board.getBlack(), board.getWhite(), new TTEntry(score, TTEntry.EntryType.UPPERBOUND, null));
        }else{
            tta.store(board.getBlack(), board.getWhite(), new TTEntry(score, TTEntry.EntryType.EXACT, bestMove));
        }
        return score;
    }
    
    List<Move> order(BitBoard board, List<Move> moves){
        TTEntry entry = previousRawTTA.lookup(board.getBlack(), board.getWhite());
        Move bestMove = entry == null ? null : entry.bestMove;

        List<MoveScore> scoredMoves = new ArrayList<>();
        for(Move move : moves){
            BitBoard newBoard = board.placed(move).flipped();
            TTEntry childEntry = previousRawTTA.lookup(newBoard.getBlack(), newBoard.getWhite());

            int score;
            if(childEntry != null){
                score = CASHE_HIT_BONUS - childEntry.value;
            }else{
                score = -eval.value(newBoard);
            }

            scoredMoves.add(new MoveScore(move, score));
        }

        scoredMoves.sort((a, b) ->
            Integer.compare(b.score, a.score));

        List<Move> ordered = new ArrayList<>();
        if(bestMove != null){
            ordered.add(bestMove);
            scoredMoves.removeIf(ms -> ms.move.equals(bestMove));
        }
        for(MoveScore ms : scoredMoves){
            ordered.add(ms.move);
        }
        return ordered;
    }

    static class MoveScore{
        Move move;
        int score;
        MoveScore(Move move, int score){
            this.move = move;
            this.score = score;
        }
    }
}