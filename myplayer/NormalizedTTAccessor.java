package myplayer;

import java.util.Map;
import java.util.HashMap;

public class NormalizedTTAccessor implements TTAccessor{
    private final Map<Long, TTEntry> table;

    public NormalizedTTAccessor(Map<Long, TTEntry> table){
        this.table = table;
    }

    public void store(BitBoard board, TTEntry entry){
        long hash = ZobristHasher.computeZobrist(
            BoardNormalizer.normalize(board.getBlack()),
            BoardNormalizer.normalize(board.getWhite())
        );
        table.put(hash, entry);
    }

    public TTEntry lookup(BitBoard board){
        long hash = ZobristHasher.computeZobrist(
            BoardNormalizer.normalize(board.getBlack()),
            BoardNormalizer.normalize(board.getWhite())
        );
        return table.get(hash);
    }
}