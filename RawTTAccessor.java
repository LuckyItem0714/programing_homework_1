package myplayer;

import java.util.Map;
import java.util.HashMap;

public class RawTTAccessor implements TTAccessor{
    private final Map<Long, TTEntry> table;

    public RawTTAccessor(Map<Long, TTEntry> table){
        this.table = table;
    }

    public void store(BitBoard board, TTEntry entry){
        long hash = ZobristHasher.computeZobrist(board.getBlack(), board.getWhite());
        table.put(hash, entry);
    }

    public TTEntry lookup(BitBoard board){
        long hash = ZobristHasher.computeZobrist(board.getBlack(), board.getWhite());
        return table.get(hash);
    }
}