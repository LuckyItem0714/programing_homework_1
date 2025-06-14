package myplayer;

public class BoardNormalizer{
    public static long normalize(long x){
        long min = x;
        long[] variants = {
            BitBoardUtil.verticalFlip(x),
            BitBoardUtil.horizontalFlip(x),
            BitBoardUtil.diagonalFlip(x),
            BitBoardUtil.antiDiagonalFlip(x),
            BitBoardUtil.rotate90(x),
            BitBoardUtil.rotate180(x),
            BitBoardUtil.rotate270(x)
        };
        for(long l : variants){
            if(l < min){
                min = l;
            }
        }
        return min;
    }
}