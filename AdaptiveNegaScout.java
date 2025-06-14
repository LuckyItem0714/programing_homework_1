package myplayer;

import static ap25.Color.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import ap25.*;
//問題点：bestMoveを正規化に合わせて調整する必要がある。
public class AdaptiveNegaScout extends NegaScoutStrategy{
    private static final int NORMALIZATION_CUTOFF_EMPTIES = 25;

    private final NormalizedTTAccessor normalized = new NormalizedTTAccessor(currentTable);

    public AdaptiveNegaScout(){}

    public Move search(BitBoard board){
        List<Move> moves = board.findLegalMoves(BLACK);
        if(moves.size() == 1){
            return moves.get(0);
        }
        currentTable.clear();
        previousTable.clear();

        TTAccessor tta = board.countEmpties() > NORMALIZATION_CUTOFF_EMPTIES ? normalized : currentRawTTA;
        for(int searchDepth = START_DEPTH; searchDepth <= DEPTH_LIMIT; searchDepth++){
            previousTable = new HashMap<>(currentTable);
            currentTable.clear();
            negaScout(board, Integer.MIN_VALUE, Integer.MAX_VALUE, searchDepth, tta);
        }
        return tta.lookup(board.getBlack(), board.getWhite()).bestMove;
    }

    int negaScout(BitBoard board, int alpha, int beta, int depth, TTAccessor tt){
        if(board.countEmpties() < NORMALIZATION_CUTOFF_EMPTIES){
            tt = currentRawTTA;
        }
        return super.negaScout(board, alpha, beta, depth, tt);
    }

    int negaAlpha(BitBoard board, int alpha, int beta, int depth, TTAccessor tt){
        if(board.countEmpties() < NORMALIZATION_CUTOFF_EMPTIES){
            tt = currentRawTTA;
        }
        return super.negaAlpha(board, alpha, beta, depth, tt);
    }
}