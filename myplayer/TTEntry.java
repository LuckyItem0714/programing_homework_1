package myplayer;

import ap25.*;

public record TTEntry(Integer value, EntryType type, Move bestMove){//置換表に格納するクラス
    public enum EntryType{
        EXACT, LOWERBOUND, UPPERBOUND
    }
}