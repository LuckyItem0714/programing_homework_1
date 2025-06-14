package myplayer;

public interface TTAccessor{
    void store(BitBoard board, TTEntry tt);
    TTEntry lookup(BitBoard board);
}