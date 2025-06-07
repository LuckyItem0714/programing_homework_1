package myplayer;

import java.util.Map;
import java.util.HashMap;

public class TranspositionTable{
    Map<Long, TTEntry> transpositionTable = new HashMap<>();

    void store(long black, long white, TTEntry entry){
        long normBlack = BoardNormalizer.normalize(black);
        long normWhite = BoardNormalizer.normalize(white);
        long hash = ZobristHasher.computeZobrist(normBlack, normWhite);
        transpositionTable.put(hash, entry);
    }
}