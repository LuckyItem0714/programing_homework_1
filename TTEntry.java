package myplayer;

import ap25.*;

public class TTEntry{//置換表に格納するクラス
    //フィールドに何を持たせるか検討中
    public enum EntryType{
        EXACT, LOWERBOUND, UPPERBOUND
    }
    int value;
    EntryType type;
    Move bestMove;

    public TTEntry(int value, EntryType type, Move bestMove){
        this.value = value;
        this.type = type;
        this.bestMove = bestMove;
    }
}