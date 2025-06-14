package myplayer;

import static ap25.Color.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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
    TTAccessor previousRawTTA = new RawTTAccessor(previousTable);

    public NegaScoutStrategy(){}

    public Move search(BitBoard board){
        for(int searchDepth = START_DEPTH; searchDepth <= DEPTH_LIMIT; searchDepth++){
            previousTable = new HashMap<>(currentTable);
            previousRawTTA = new RawTTAccessor(previousTable);
            currentTable.clear();
            negaScout(board, Integer.MIN_VALUE, Integer.MAX_VALUE, searchDepth, currentRawTTA);
        }
        return currentRawTTA.lookup(board).bestMove;
    }

    int negaScout(BitBoard board, int alpha, int beta, int depth, TTAccessor tta) {
        if (SearchUtil.isTerminal(board, depth)) return eval.value(board);

        int u = Integer.MAX_VALUE;
        int l = Integer.MIN_VALUE;
        TTEntry entry = tta.lookup(board);
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

        var moveBoardPairs = order(board);

        var newBoard = moveBoardPairs.get(0).second;//フリップ済み
        int v = -negaScout(newBoard, -beta, -alpha, depth - 1, tta);
        if(v >= beta){
            if(v > l){
                tta.store(board, new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
            }
            return v;
        }
        Move bestMove = null;
        if(v > alpha){
            alpha = v;
            bestMove = moveBoardPairs.get(0).first;
        }
        int score = v;
        
        //Null Window Search
        for (int i = 1; i < moveBoardPairs.size(); i++) {
            newBoard = moveBoardPairs.get(i).second;
            v = -negaAlpha(newBoard, -alpha - 1, -alpha, depth - 1, tta);
            if (v >= beta) {
                if(v > l){
                    tta.store(board, new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                }
                return v;
            }
            if(v > alpha){
                alpha = v;
                v = -negaScout(newBoard, -beta, -alpha, depth - 1, tta);
                if(v >= beta){
                    if(v > l){
                        tta.store(board, new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                    }
                    return v;
                }
            }
            if(v > alpha){
                alpha = v;
                bestMove = moveBoardPairs.get(i).first;
            }
            score = Math.max(score, v);
        }
        if(score < alpha){
            tta.store(board, new TTEntry(score, TTEntry.EntryType.UPPERBOUND, null));
        } else{
            tta.store(board, new TTEntry(score, TTEntry.EntryType.EXACT, bestMove));
        }
        return score;
    }

    int negaAlpha(BitBoard board, int alpha, int beta, int depth, TTAccessor tta) {
       if (SearchUtil.isTerminal(board, depth)) return eval.value(board);

        int u = Integer.MAX_VALUE;
        int l = Integer.MIN_VALUE;
        TTEntry entry = tta.lookup(board);
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

        var moveBoardPairs = order(board);

        int score = Integer.MIN_VALUE;
        Move bestMove = null;
        for (var pair : moveBoardPairs) {
            var newBoard = pair.second;
            int v = -negaAlpha(newBoard, -beta, -alpha, depth - 1, tta);
            if(v >= beta){
                if(v > l){
                    tta.store(board, new TTEntry(v, TTEntry.EntryType.LOWERBOUND, null));
                }
                return v;
            }
            if(v > alpha){
                alpha = v;
                bestMove = pair.first;
            }
            score = Math.max(score, v);
        }

        if(score <= alpha){
            tta.store(board, new TTEntry(score, TTEntry.EntryType.UPPERBOUND, null));
        }else{
            tta.store(board, new TTEntry(score, TTEntry.EntryType.EXACT, bestMove));
        }
        return score;
    }
    
    List<Pair<Move, BitBoard>> order(BitBoard board){
        List<Pair<Move, BitBoard>> moveBoardPairs = board.findLegalMoveResults();
        List<MoveScoreBoard> scoredBoards = new ArrayList<>();

        TTEntry entry = previousRawTTA.lookup(board);
        BitBoard bestBoard = (entry != null && entry.bestMove != null)
            ? board.placed(entry.bestMove).flipped()
            : null;

        for(Pair<Move, BitBoard> pair : moveBoardPairs){
            BitBoard newBoard = pair.second;
            TTEntry childEntry = previousRawTTA.lookup(newBoard);

            int score;
            if(childEntry != null){
                score = CASHE_HIT_BONUS - childEntry.value;
            }else{
                score = -eval.value(newBoard);
            }

            scoredBoards.add(new MoveScoreBoard(pair.first, newBoard, score));
        }

        scoredBoards.sort((a, b) ->
            Integer.compare(b.score, a.score));

        List<Pair<Move, BitBoard>> ordered = new ArrayList<>();
        Set<Integer> seenBoards = new HashSet<>();
        if(bestBoard != null){
            for(MoveScoreBoard msb : scoredBoards){
                if(msb.board.equals(bestBoard)){
                    ordered.add(new Pair<>(msb.move, msb.board));
                    seenBoards.add(msb.board.hashCode());
                    break;
                }
            }
        }
        for(MoveScoreBoard msb : scoredBoards){
            int hash = msb.board.hashCode();
            if(!seenBoards.contains(hash)){
                ordered.add(new Pair<>(msb.move, msb.board));
                seenBoards.add(hash);
            }
        }
        return ordered;
    }

    static class MoveScoreBoard{
        Move move;
        BitBoard board;
        int score;
        MoveScoreBoard(Move move, BitBoard board, int score){
            this.move = move;
            this.board = board;
            this.score = score;
        }
    }
}