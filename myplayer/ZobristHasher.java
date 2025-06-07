package myplayer;

import ap25.*;
import java.util.Random;
//TODO: 定数化
public class ZobristHasher{
    private static final long[][] zobristTable = new long[64][2];//[2][64]より効率的
    static{
        Random rand = new Random();
        for(int k6 = 0; k6 < 36; k6++){
            int k8 = BitBoardUtil.IDX_6_TO_8[k6];
            zobristTable[k8][0] = rand.nextLong();
            zobristTable[k8][1] = rand.nextLong();
        }
    }

    public static long computeZobrist(long black, long white) {
        long hash = 0;
        for (int k8 = 0; k8 < 64; k8++) {
            long mask = 1L << k8;
            if ((black & mask) != 0) hash ^= zobristTable[k8][0];
            if ((white & mask) != 0) hash ^= zobristTable[k8][1];
        }
        return hash;
    }
}