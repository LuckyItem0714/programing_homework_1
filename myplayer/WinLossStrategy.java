package myplayer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import ap25.*;

public class WinLossStrategy implements Strategy{
    private static final int LOW = -2;
    Eval eval = new WinLossEval();
    final Map<Long, TTEntry> table = new HashMap<>();
    final TTAccessor tta = new RawTTAccessor(table);

    public WinLossStrategy(){}

    public Move search(BitBoard board){
        negaAlpha(board, -2, 2);
        return tta.lookup(board).bestMove();
    }

    int negaAlpha(BitBoard board, int alpha, int beta){
        TTEntry entry = tta.lookup(board);
        if(entry != null){
            switch(entry.type()){
                case EXACT:
                    return entry.value();
                case LOWERBOUND:
                    alpha = Math.max(alpha, entry.value());
                    break;
                case UPPERBOUND:
                    beta = Math.min(beta, entry.value());
            }
            if(alpha >= beta){
                return entry.value();
            }
        }

        if(board.isEnd()){
            int value = eval.value(board);
            tta.store(board, new TTEntry(value, TTEntry.EntryType.EXACT, null));
            return value;
        }

        int maxScore = -2;
        Move bestMove = null;
        List<Pair<Move, BitBoard>> moveBoardPairs = order(board);
        for(Pair<Move, BitBoard> pair : moveBoardPairs){
            int value = -negaAlpha(pair.second, -beta, -alpha);
            if(value > maxScore){
                maxScore = value;
                bestMove = pair.first;
            }
            alpha = Math.max(alpha, value);
            if(alpha >= beta){
                break;
            }
        }

        TTEntry.EntryType type;
        if(maxScore <= alpha){
            type = TTEntry.EntryType.UPPERBOUND;
        }else if(maxScore >= beta){
            type = TTEntry.EntryType.LOWERBOUND;
        }else{
            type = TTEntry.EntryType.EXACT;
        }
        tta.store(board, new TTEntry(maxScore, type, bestMove));
        return maxScore;
    }

        //速さ優先探索(相手の着手可能位置が少ない順)
    public List<Pair<Move, BitBoard>> order(BitBoard board) {
        List<Pair<Move, BitBoard>> moveBoardPairs = board.findLegalMoveResults();
        List<MoveScoreBoard> scoredBoards = new ArrayList<>();

        for (Pair<Move, BitBoard> pair : moveBoardPairs) {
            BitBoard newBoard = pair.second;
            int opponentMoves = newBoard.findLegalMoves(newBoard.getTurn()).size();
            scoredBoards.add(new MoveScoreBoard(pair.first, newBoard, opponentMoves));
        }

        scoredBoards.sort(Comparator.naturalOrder()); // 昇順（相手の手が少ない順）

        List<Pair<Move, BitBoard>> ordered = new ArrayList<>();
        for (MoveScoreBoard msb : scoredBoards) {
            ordered.add(new Pair<>(msb.move(), msb.board()));
        }

        return ordered;
    }   
}