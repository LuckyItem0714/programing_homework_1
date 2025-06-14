package myplayer;

import java.util.Map;
import java.util.HashMap;

public class NormalizedTTAccessor implements TTAccessor{
    private final Map<Long, TTEntry> table;

    public NormalizedTTAccessor(Map<Long, TTEntry> table){
        this.table = table;
    }

    public void store(long black, long white, TTEntry entry){
        long hash = ZobristHasher.computeZobrist(
            BoardNormalizer.normalize(black),
            BoardNormalizer.normalize(white)
        );
        table.put(hash, entry);
    }

    public TTEntry lookup(long black, long white){
        long hash = ZobristHasher.computeZobrist(
            BoardNormalizer.normalize(black),
            BoardNormalizer.normalize(white)
        );
        return table.get(hash);
    }
}