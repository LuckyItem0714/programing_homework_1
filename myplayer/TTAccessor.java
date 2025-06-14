package myplayer;

public interface TTAccessor{
    void store(long black, long white, TTEntry tt);
    TTEntry lookup(long black, long white);
}