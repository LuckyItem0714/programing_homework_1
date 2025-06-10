package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import ap25.*;

class MyEval {// 盤面の評価を行うクラス。各マスに重みを与え、石の配置に基づいてスコアを計算します。
	// 6×6 の評価行列。角や端が高得点、中央は低得点、辺の隅に近い部分はマイナス評価。

	private Color myColor;

	public MyEval(Color myColor) {
		this.myColor = myColor;
	}

	public float value(Board board) {// ゲーム終了時はスコアに大きな重みをかけて返す。それ以外は、複合評価関数でスコアを計算。
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

	private float positionalValue(Board board,int[][] M) {
		return (int) IntStream.range(0, LENGTH).mapToDouble(k -> score(board, k,M)).reduce(Double::sum).orElse(0);
	}

	private float score(Board board, int k, int[][] M) {// インデックス k のマスのスコアを計算。
		return M[k / SIZE][k % SIZE] * board.get(k).getValue();
	}
}

public class MyPlayer extends ap25.Player {
	/*
	 *
	 * MyEval eval: 評価関数。 int depthLimit: 探索の深さ制限。 Move move: 現在選んでいる手。 MyBoard
	 * board: 内部で保持する盤面。
	 *
	 */
	static final String MY_NAME = "MY24";
	MyEval eval;
	int depthLimit;
	Move move;
	BitBoard board;

	public MyPlayer(Color color) {// デフォルト名 "MY24"、評価関数、深さ2で初期化。
		this(MY_NAME, color, new MyEval(color), 2);
	}

	public MyPlayer(String name, Color color, MyEval eval, int depthLimit) {// 名前、色、評価関数、探索深さを指定して初期化。
		super(name, color);
		this.eval = eval;
		this.depthLimit = depthLimit;
		this.board = new BitBoard();
	}

	public MyPlayer(String name, Color color, int depthLimit) {
		this(name, color, new MyEval(color), depthLimit);
	}

	public void setBoard(Board board) {// 外部から渡された盤面を内部の BitBoard にコピー。
		for (var i = 0; i < LENGTH; i++) {
			this.board.set(i, board.get(i));
		}
		this.board.move = board.getMove(); // 最後の手もコピー
	}

	boolean isBlack() {
		return getColor() == BLACK;
	}

	public Move think(Board board) {
		this.board = (BitBoard)board;

		if (this.board.findNoPassLegalIndexes(getColor()).size() == 0) {
			this.move = Move.ofPass(getColor());
		} else {
			var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
			this.move = null;

			maxSearch(newBoard, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0);

			this.move = this.move.colored(getColor());
		}

		this.board = this.board.placed(this.move);
		return this.move;
		/*
		 * プレイヤーの思考メソッド。次の手を決定します。 処理の流れ： 直前の手を placed() で盤面に反映。 合法手がなければ PASS を返す。
		 * 自分が白番なら盤面を反転（白視点で探索）。 maxSearch() を呼び出して最善手を探索。 結果の手を自分の色に戻して返す。
		 */
	}

	// ミニマックス探索（α-β枝切り）で最善手を評価。
	float maxSearch(Board board, float alpha, float beta, int depth) {// 黒番（自分）の手番で最大化。深さ0のときに this.move に最善手を記録。
		if (isTerminal(board, depth))
			return this.eval.value(board);

		var moves = board.findLegalMoves(BLACK);
		moves = order(moves);

		if (depth == 0)
			this.move = moves.get(0);

		for (var move : moves) {
			var newBoard = board.placed(move);
			float v = minSearch(newBoard, alpha, beta, depth + 1);

			if (v > alpha) {
				alpha = v;
				if (depth == 0)
					this.move = move;
			}

			if (alpha >= beta)
				break;
		}

		return alpha;
	}

	float minSearch(Board board, float alpha, float beta, int depth) {// 白番（相手）の手番で最小化。
		if (isTerminal(board, depth))
			return this.eval.value(board);

		var moves = board.findLegalMoves(WHITE);
		moves = order(moves);

		for (var move : moves) {
			var newBoard = board.placed(move);
			float v = maxSearch(newBoard, alpha, beta, depth + 1);
			beta = Math.min(beta, v);
			if (alpha >= beta)
				break;
		}

		return beta;
	}

	boolean isTerminal(Board board, int depth) {// ゲーム終了または探索深さ制限に達したかを判定。
		return board.isEnd() || depth > this.depthLimit;
	}

	List<Move> order(List<Move> moves) {// 手の順序をランダムにシャッフルして探索の多様性を確保。
		var shuffled = new ArrayList<Move>(moves);
		Collections.shuffle(shuffled);
		return shuffled;
	}
}
