package myplayer;

import ap25.*;

public class SearchUtil{
	static final int ENDGAME_THRESHOLD = 12;
    public static boolean isTerminal(Board board, int depth) {// ゲーム終了または探索深さ制限に達したかを判定。
		return board.isEnd() || depth < 0;
	}

	public static boolean isEndgame(BitBoard board){
		int emptiesCount = board.countEmpties();
		return emptiesCount == ENDGAME_THRESHOLD || emptiesCount == ENDGAME_THRESHOLD - 1;
	}
}