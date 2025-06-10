package p25x13;

import ap25.*;
import static ap25.Color.*;
import java.util.ArrayList;
import java.util.List;
//TODO: 定数化, 変換のキャッシュ検討
public class BitBoardUtil{
        //8x8盤面の中央に6x6盤面を埋め込む
    public static final int BOARD_SIZE_6x6 = 36;
    public static final int BOARD_SIZE_8x8 = 64;
    public static final int BOARD_WIDTH_6 = 6;
    public static final int BOARD_WIDTH_8 = 8;

    public static final long PLAYABLE_6x6 =0x7e7e7e7e7e7e00L;

    public static final int[] IDX_6_TO_8 = new int[BOARD_SIZE_6x6];//8x8盤と6x6盤の変換
    public static final int[] IDX_8_TO_6 = new int[BOARD_SIZE_8x8];

    static{//インデックス変換のリストの初期化
        for(int i6 = 0; i6 < BOARD_SIZE_6x6; i6++){
            int i8 = 8 * (i6 / 6 + 1) + (i6 % 6) + 1;
            IDX_6_TO_8[i6] = i8;
            IDX_8_TO_6[i8] = i6;
        }
    }

    private static long deltaSwap(long x, long mask, int delta){
        long y = ((x >>> delta) ^ x) & mask;
        return y ^ (y << delta) ^ x;
    }
//以下、row, colというと8x8盤から0-indexで見た値とする
//D_4群の位数は8。
    public static long verticalFlip(long x){//行に関して反転
        x = deltaSwap(x, 0x7e7e7e00L, 24);//1, 2, 3をそれぞれ4, 5, 6と入れ替え
        return deltaSwap(x, 0x7e00007e00L, 16);//1, 4をそれぞれ3, 6と入れ替え
    }

    public static long horizontalFlip(long x){//列に関して反転
        x = deltaSwap(x, 0xe0e0e0e0e0e00L, 3);//verticalFlip()と同様
        return deltaSwap(x,0x12121212121200L, 2);
    }

    public static long diagonalFlip(long x){//row+colに関して反転
        x = deltaSwap(x, 0x2070e04L, 36);//2, 3, 4をそれぞれ10, 11, 12と入れ替え
        x = deltaSwap(x, 0x10224408112204L, 9);//2, 7, 10をそれぞれ4, 9, 11と入れ替え
        return deltaSwap(x, 0x204081000L, 18);//6を10と入れ替え
    }

    public static long antiDiagonalFlip(long x){//row-colに関して反転
        x = deltaSwap(x, 0x40e07020L, 28);//diagonalFlip()と同様
        x = deltaSwap(x, 0x8442210884420L, 7);
        return deltaSwap(x, 0x402010080400L, 14);
    }

    public static long rotate90(long x){//正の向きに90度回転
        x = diagonalFlip(x);
        return horizontalFlip(x);
    }

    public static long rotate180(long x){
        x = verticalFlip(x);
        return horizontalFlip(x);
    }

    public static long rotate270(long x){
        x = diagonalFlip(x);
        return verticalFlip(x);
    }

    public static List<Integer> bitmaskToIndices(long mask){//maskの1が立っているビットの番号のリスト
    List<Integer> list = new ArrayList<>();
    while(mask != 0){
      int idx = Long.numberOfTrailingZeros(mask);//最下位ビットの番号を取得
      list.add(idx);
      mask &= (mask - 1);//最下位ビットを消去
    }
    return list;
    }
}
