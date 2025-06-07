package myplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class BoardNormalizer{
    public static long normalize(long x){
        long[] variants = {
            x,
            BitBoardUtil.verticalFlip(x),
            BitBoardUtil.horizontalFlip(x),
            BitBoardUtil.diagonalFlip(x),
            BitBoardUtil.antiDiagonalFlip(x),
            BitBoardUtil.rotate90(x),
            BitBoardUtil.rotate180(x),
            BitBoardUtil.rotate270(x)
        };
        return Arrays.stream(variants).min().getAsLong();
    }
}