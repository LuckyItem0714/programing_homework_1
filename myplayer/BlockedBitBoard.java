package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;

import ap25.*;

public class BlockedBitBoard extends BitBoard{
    private final long BLOCKED;

    public BlockedBitBoard(){
        super();
        BLOCKED = 0L;
    }

    public BlockedBitBoard(Board board){
        super();
        long blocked = 0L;
        for(int k6 = 0; k6 < LENGTH; k6++){
            int k8 = BitBoardUtil.IDX_6_TO_8[k6];
            Color color = board.get(k6);
            long mask = 1L << k8;
            if(color == BLOCK){
                blocked |= mask;
                continue;
            }
            set(k8, color);
        }
        BLOCKED = blocked;
    }


    
    @Override
    protected void update() {
        this.occupied  = black | white | BLOCKED;
        this.empty = (~this.occupied) & BitBoardUtil.PLAYABLE_6x6;
    }

    @Override
    public Color get(int k6){
        long mask = 1L << BitBoardUtil.IDX_6_TO_8[k6];
        if ((BLOCKED & mask) != 0){
            return BLOCK;
        }
        return super.get(k6);
    }
}