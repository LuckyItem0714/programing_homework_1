package myplayer;

import java.util.Map;
import java.util.HashMap;

public class RawTTAccessor implements TTAccessor{
    private final Map<Long, TTEntry> table;

    public RawTTAccessor(Map<Long, TTEntry> table){
        this.table = table;
    }

    public void store(long black, long white, TTEntry entry){
        long hash = ZobristHasher.computeZobrist(black, white);
        table.put(hash, entry);
    }

    public TTEntry lookup(long black, long white){
        long hash = ZobristHasher.computeZobrist(black, white);
        return table.get(hash);
    }
}