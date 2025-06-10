package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import ap25.*;

class MyEval {// 盤面の評価を行うクラス。各マスに重みを与え、石の配置に基づいてスコアを計算します。
	// 6×6 の評価行列。角や端が高得点、中央は低得点、辺の隅に近い部分はマイナス評価。

	private Color myColor;

	public MyEval(Color myColor) {
		this.myColor = myColor;
	}

	public int value(Board board) {// ゲーム終了時はスコアに大きな重みをかけて返す。それ以外は、複合評価関数でスコアを計算。
		if (board.isEnd()) {
			return 1000000 * board.score();
		}
		;
		int[][] M;
		int stoneCount = board.count(BLACK) + board.count(WHITE);

		if (stoneCount < 12) {
			M = new int[][] { 
				{ 30, -5, 1, 1, -5, 30 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 1, -1, -1, -1, -1, 1 },
				{ 1, -1, -1, -1, -1, 1 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 30, -5, 1, 1, -5, 30 }, };
		} else if (stoneCount < 24) {
			M = new int[][] { 
				{ 20, -5, 1, 1, -5, 20 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 1, -1, -1, -1, -1, 1 },
				{ 1, -1, -1, -1, -1, 1 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 20, -5, 1, 1, -5, 20 }, };
		} else {
			M = new int[][] { 
				{ 10, -5, 1, 1, -5, 10 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 1, -1, -1, -1, -1, 1 },
				{ 1, -1, -1, -1, -1, 1 }, 
				{ -5, -10, -1, -1, -10, -5 }, 
				{ 10, -5, 1, 1, -5, 10 }, };
		}

		int lb = board.findLegalMoves(myColor).size();
		int lw = board.findLegalMoves(myColor.flipped()).size();
		int nb = (int) IntStream.range(0, LENGTH).filter(i -> board.get(i) == myColor).count();
		int nw = (int) IntStream.range(0, LENGTH).filter(i -> board.get(i) == myColor.flipped()).count();

		int w1 = 2; // 盤面の重み
		int w2 = 4; // 自分の合法手の数
		int w3 = -4; // 相手の合法手の数
		int w4 = 0; // 自分の石の数
		int w5 = 0; // 相手の石の数

		if (myColor == BLACK) {
			if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 4; // 盤面の重み
				w2 = 6; // 自分の合法手の数
				w3 = -10; // 相手の合法手の数
				w4 = 2; // 自分の石の数
				w5 = -2; // 相手の石の数
			} else if (nb + nw < 25) {
				w1 = 2; // 盤面の重み
				w2 = 4; // 自分の合法手の数
				w3 = -4; // 相手の合法手の数
				w4 = 6; // 自分の石の数
				w5 = -6; // 相手の石の数
			}
		} else {
			if(nb + nw >= 4 && nb + nw < 7) {
				w1 = 2;
				w2 = -4;
				w3 = 2;
				w4 = 2;
				w5 = -2;
			}else if(nb + nw >= 7 && nb + nw < 13) {
			w1 = 4; // 盤面の重み
			w2 = 6; // 自分の合法手の数
			w3 = -8; // 相手の合法手の数
			w4 = 2; // 自分の石の数
			w5 = -2; // 相手の石の数
			}else if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 3; // 盤面の重み
				w2 = 5; // 自分の合法手の数
				w3 = -7; // 相手の合法手の数
				w4 = 4; // 自分の石の数
				w5 = -4; // 相手の石の数
			} else if (nb + nw >= 25 && nb + nw < 36) {
				 w1 = 2; // 盤面の重み
				 w2 = 4; // 自分の合法手の数
				 w3 = -4; // 相手の合法手の数
				 w4 = 2; // 自分の石の数
				 w5 = 2; // 相手の石の数
			}
		}

		return w1 * positionalValue(board,M) + w2 * lb + w3 * lw + w4 * nb + w5 * nw;
	}

	private int positionalValue(Board board,int[][] M) {
		return (int) IntStream.range(0, LENGTH).mapToDouble(k -> score(board, k,M)).reduce(Double::sum).orElse(0);
	}

	private int score(Board board, int k, int[][] M) {// インデックス k のマスのスコアを計算。
		return M[k / SIZE][k % SIZE] * board.get(k).getValue();
	}
}