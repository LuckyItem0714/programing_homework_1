package myplayer;

import ap25.*;

public class WinLossEval implements Eval{
    public int value(BitBoard board){
        return board.winner().getValue();
    }
}