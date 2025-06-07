package myplayer;

public class TTEntry{//置換表に格納するクラス
    //フィールドに何を持たせるか検討中
    int score;
    int depth;
    int bestMoveIdx;

    public TTEntry(int score, int depth, int bestMoveIdx){
        this.score = score;
        this.depth = depth;
        this.bestMoveIdx = bestMoveIdx;
    }

}