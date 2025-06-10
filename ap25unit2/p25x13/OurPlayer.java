package p25x13;

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
		float[][] M;
		int stoneCount = board.count(BLACK) + board.count(WHITE);

		if (stoneCount < 12) {
			M = new float[][] { { 30, -5, 1, 1, -5, 30 }, { -5, -10, -1, -1, -10, -5 }, { 1, -1, -1, -1, -1, 1 },
					{ 1, -1, -1, -1, -1, 1 }, { -5, -10, -1, -1, -10, -5 }, { 30, -5, 1, 1, -5, 30 }, };
		} else if (stoneCount < 24) {
			M = new float[][] { { 20, -5, 1, 1, -5, 20 }, { -5, -10, -1, -1, -10, -5 }, { 1, -1, -1, -1, -1, 1 },
					{ 1, -1, -1, -1, -1, 1 }, { -5, -10, -1, -1, -10, -5 }, { 20, -5, 1, 1, -5, 20 }, };
		} else {
			M = new float[][] { { 15, 0, 10, 10, 0, 15 }, { 0, -5, 1, 1, -5, 0 }, { 10, 1, 1, 1, 1, 10 },
					{ 10, 1, 1, 1, 1, 10 }, { 0, -5, 1, 1, -5, 0 }, { 15, 0, 10, 10, 0, 15 }, };
		}

		List<Move> myMoves = board.findLegalMoves(myColor);
		List<Move> oppMoves = board.findLegalMoves(myColor.flipped());
		int lb = myMoves.size();
		int lw = oppMoves.size();

		int nb = 0, nw = 0;
		for (int i = 0; i < LENGTH; i++) {
			var c = board.get(i);
			if (c == myColor) nb++;
			else if (c == myColor.flipped()) nw++;
		}


		float w1 = 1.0f; // 盤面の重み
		float w2 = 2.0f; // 自分の合法手の数
		float w3 = -2.0f; // 相手の合法手の数
		float w4 = 0.0f; // 自分の石の数
		float w5 = 0.0f; // 相手の石の数

		if (myColor == BLACK) {
			if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 2.0f; // 盤面の重み
				w2 = 3.0f; // 自分の合法手の数
				w3 = -5.0f; // 相手の合法手の数
				w4 = 1.0f; // 自分の石の数
				w5 = -1.0f; // 相手の石の数
			} else if (nb + nw < 25) {
				w1 = 1.0f; // 盤面の重み
				w2 = 2.0f; // 自分の合法手の数
				w3 = -2.0f; // 相手の合法手の数
				w4 = 3.0f; // 自分の石の数
				w5 = -3.0f; // 相手の石の数
			}
		} else {
			if (nb + nw >= 4 && nb + nw < 10) {
				w1 = 0.0f;
				w2 = -2.0f;
				w3 = 1.0f;
				w4 = 1.0f;
				w5 = -1.0f;
			} else if (nb + nw >= 10 && nb + nw < 12) {
				w1 = 10.0f; // 盤面の重み
				w2 = 0.0f; // 自分の合法手の数
				w3 = -0.0f; // 相手の合法手の数
				w4 = 0.0f; // 自分の石の数
				w5 = -0.0f; // 相手の石の数
			} else if (nb + nw >= 12 && nb + nw < 13) {
				w1 = 0.0f; // 盤面の重み
				w2 = 3.0f; // 自分の合法手の数
				w3 = -3.0f; // 相手の合法手の数
				w4 = 1.0f; // 自分の石の数
				w5 = -1.0f; // 相手の石の数
			} else if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 1.5f; // 盤面の重み
				w2 = 0.0f; // 自分の合法手の数
				w3 = -3.0f; // 相手の合法手の数
				w4 = 2.0f; // 自分の石の数
				w5 = -2.0f; // 相手の石の数
			} else if (nb + nw >= 25 && nb + nw < 36) {
				w1 = 2.0f; // 盤面の重み
				w2 = 1.0f; // 自分の合法手の数
				w3 = -1.0f; // 相手の合法手の数
				w4 = 5.0f; // 自分の石の数
				w5 = -5.0f; // 相手の石の数
			}
		}

		return w1 * positionalValue(board, M) + w2 * lb + w3 * lw + w4 * nb + w5 * nw;
	}

	private float positionalValue(Board board, float[][] M) {
		return (float) IntStream.range(0, LENGTH).mapToDouble(k -> score(board, k, M)).reduce(Double::sum).orElse(0);
	}

	private float score(Board board, int k, float[][] M) {// インデックス k のマスのスコアを計算。
		return M[k / SIZE][k % SIZE] * board.get(k).getValue();
	}
}

public class OurPlayer extends ap25.Player {
	/*
	 *
	 * MyEval eval: 評価関数。 int depthLimit: 探索の深さ制限。 Move move: 現在選んでいる手。 MyBoard
	 * board: 内部で保持する盤面。
	 *
	 */
	static final String MY_NAME = "2513";
	MyEval eval;
	int depthLimit;
	Move move;
	OurBoard board;

	public OurPlayer(Color color) {// デフォルト名 "2513"、評価関数、深さ2で初期化。
		this(MY_NAME, color, new MyEval(color), 2);
	}

	public OurPlayer(String name, Color color, MyEval eval, int depthLimit) {// 名前、色、評価関数、探索深さを指定して初期化。
		super(name, color);
		this.eval = eval;
		this.depthLimit = depthLimit;
		this.board = new OurBoard();
	}

	public OurPlayer(String name, Color color, int depthLimit) {
		this(name, color, new MyEval(color), depthLimit);
	}

	public void setBoard(Board board) {// 外部から渡された盤面を内部の OurBoard にコピー。
		for (var i = 0; i < LENGTH; i++) {
			this.board.set(i, board.get(i));
		}
		this.board.move = board.getMove(); // 最後の手もコピー
	}

	boolean isBlack() {
		return getColor() == BLACK;
	}

	public Move think(Board board) {
		this.board = this.board.placed(board.getMove());

		if (this.board.findNoPassLegalIndexes(getColor()).size() == 0) {
			this.move = Move.ofPass(getColor());
		} else {
			var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
			this.move = null;

      var legals = this.board.findNoPassLegalIndexes(getColor());
      
			maxSearch(newBoard, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0);

			this.move = this.move.colored(getColor());
			if (legals.contains(this.move.getIndex()) == false) {
		        System.out.println("**************");
		        System.out.println(legals);
		        System.out.println(this.move);
		        System.out.println(this.move.getIndex());
		        System.out.println(this.board);
		        System.out.println(newBoard);
		        System.exit(0);
		        maxSearch(newBoard, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0);
		      }
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

	class ScoredMove {
		Move move;
		float score;

		ScoredMove(Move move, float score) {
			this.move = move;
			this.score = score;
		}
	}

	List<Move> order(List<Move> moves) {
		List<ScoredMove> scoredMoves = new ArrayList<>();
		for (Move move : moves) {
			OurBoard nextBoard = board.clone().placed(move);
			float value = eval.value(nextBoard);
			scoredMoves.add(new ScoredMove(move, value));
		}

		// 評価値の高い順にソート（降順）
		scoredMoves.sort((a, b) -> Float.compare(b.score, a.score));

		// Move だけを取り出して返す
		List<Move> ordered = new ArrayList<>();
		for (ScoredMove sm : scoredMoves) {
			ordered.add(sm.move);
		}
		return ordered;
	}

}

