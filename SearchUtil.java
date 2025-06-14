package myplayer;

import ap25.*;

public class SearchUtil{
    public static boolean isTerminal(Board board, int depth) {// ゲーム終了または探索深さ制限に達したかを判定。
		return board.isEnd() || depth < 0;
	}
}