package p25x13;

import static ap25.Board.*;
import static ap25.Color.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import ap25.*;

class MyEval {// 盤面の評価を行うクラス。各マスに重みを与え、石の配置に基づいてスコアを計算します。
	// 6×6 の評価行列。角や端が高得点、中央は低得点、辺の隅に近い部分はマイナス評価。

	private Color myColor;

	public MyEval(Color myColor) {
		this.myColor = myColor;
	}

	// 確定石の数をカウントするメソッドを追加
	private int countStableDiscs(Board board, Color color) {
		int count = 0;
		for (int i = 0; i < LENGTH; i++) {
			if (isStable(board, i, color)) {
				count++;
			}
		}
		return count;
	}

	// 簡易的な安定石判定（角とその周辺の連続石のみを対象）
	private boolean isStable(Board board, int index, Color color) {
		int row = index / SIZE;
		int col = index % SIZE;

		// 角の位置
		if ((row == 0 || row == SIZE - 1) && (col == 0 || col == SIZE - 1)) {
			return board.get(index) == color;
		}

		// より高度な安定石判定はここに追加可能
		return false;
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

		int stableMy = countStableDiscs(board, myColor);
		int stableOpp = countStableDiscs(board, myColor.flipped());

		int nb = 0, nw = 0;
		for (int i = 0; i < LENGTH; i++) {
			var c = board.get(i);
			if (c == myColor)
				nb++;
			else if (c == myColor.flipped())
				nw++;
		}

		float w1 = 1.0f; // 盤面の重み
		float w2 = 2.0f; // 自分の合法手の数
		float w3 = -2.0f; // 相手の合法手の数
		float w4 = 0.0f; // 自分の石の数
		float w5 = 0.0f; // 相手の石の数

		float w6 = 10.0f; // 確定石の重み（調整可能）
		float w7 = -10.0f;

		if (myColor == BLACK) {
			if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 2.0f; // 盤面の重み
				w2 = 3.0f; // 自分の合法手の数
				w3 = -5.0f; // 相手の合法手の数
				w4 = 1.0f; // 自分の石の数
				w5 = -1.0f; // 相手の石の数
				w6 = 0;
				w7 = 0;
			} else if (nb + nw < 25) {
				w1 = 1.0f; // 盤面の重み
				w2 = 2.0f; // 自分の合法手の数
				w3 = -2.0f; // 相手の合法手の数
				w4 = 3.0f; // 自分の石の数
				w5 = -3.0f; // 相手の石の数
				w6 = 3;
				w7 = -3;
			}
		} else {
			if (nb + nw >= 4 && nb + nw < 10) {
				w1 = 0.0f;
				w2 = -2.0f;
				w3 = 1.0f;
				w4 = 1.0f;
				w5 = -1.0f;
				w6 = -0.0f;
				w7 = -0.0f;
			} else if (nb + nw >= 10 && nb + nw < 12) {
				w1 = 2.0f; // 盤面の重み
				w2 = 0.0f; // 自分の合法手の数
				w3 = -0.0f; // 相手の合法手の数
				w4 = 0.0f; // 自分の石の数
				w5 = -0.0f; // 相手の石の数
				w6 = 0;
				w7 = 0;
			} else if (nb + nw >= 12 && nb + nw < 13) {
				w1 = 1.0f; // 盤面の重み
				w2 = 3.0f; // 自分の合法手の数
				w3 = -4.0f; // 相手の合法手の数
				w4 = 1.0f; // 自分の石の数
				w5 = -1.0f; // 相手の石の数
				w6 = 1;
				w7 = -1;
			} else if (nb + nw >= 13 && nb + nw < 25) {
				w1 = 1.0f; // 盤面の重み
				w2 = 0.0f; // 自分の合法手の数
				w3 = -3.0f; // 相手の合法手の数
				w4 = 2.0f; // 自分の石の数
				w5 = -3.0f; // 相手の石の数
				w6 = 1;
				w7 = -1;
			} else if (nb + nw >= 25 && nb + nw < 36) {
				w1 = 1.0f; // 盤面の重み
				w2 = 1.0f; // 自分の合法手の数
				w3 = -1.0f; // 相手の合法手の数
				w4 = 5.0f; // 自分の石の数
				w5 = -5.0f; // 相手の石の数
				w6 = 0;
				w7 = 0;
			}
		}

		return w1 * positionalValue(board, M) + w2 * lb + w3 * lw + w4 * nb + w5 * nw + w6 * stableMy + w7 * stableOpp;

	}

	private float positionalValue(Board board, float[][] M) {
		return (float) IntStream.range(0, LENGTH).mapToDouble(k -> score(board, k, M)).reduce(Double::sum).orElse(0);
	}

	private float score(Board board, int k, float[][] M) {// インデックス k のマスのスコアを計算。
		return M[k / SIZE][k % SIZE] * board.get(k).getValue();
	}
}

public class OurPlayer extends Player {
	static final String MY_NAME = "2513";
	MyEval eval;
	int depthLimit;
	Move move;
	BitBoard board;

	// Transposition table to cache board evaluations
	Map<String, Float> transpositionTable = new HashMap<>();

	public OurPlayer(Color color) {
		this(MY_NAME, color, new MyEval(color), 6);
	}

	public OurPlayer(String name, Color color, MyEval eval, int depthLimit) {
		super(name, color);
		this.eval = eval;
		this.depthLimit = depthLimit;
		this.board = new BitBoard();
	}

	public void setBoard(Board board) {
		this.board = new BitBoard(board);
	}

	boolean isBlack() {
		return getColor() == Color.BLACK;
	}

	public Move think(Board board) {
		this.board = this.board.placed(board.getMove());
		if (this.board.findNoPassLegalIndexes(getColor()).isEmpty()) {
			this.move = Move.ofPass(getColor());
		} else {
			var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
			this.move = null;
			maxSearchPVS(newBoard, -Float.MAX_VALUE, Float.MAX_VALUE, 0);
			this.move = this.move.colored(getColor());
		}
		this.board = this.board.placed(this.move);
		return this.move;
	}

	float maxSearchPVS(Board board, float alpha, float beta, int depth) {
		if (isTerminal(board, depth))
			return evaluate(board);
		var moves = order(board.findLegalMoves(Color.BLACK));
		boolean first = true;
		for (var move : moves) {
			var newBoard = board.placed(move);
			float score;
			if (first) {
				score = minSearchPVS(newBoard, alpha, beta, depth + 1);
				first = false;
			} else {
				score = minSearchPVS(newBoard, alpha, alpha + 1, depth + 1);
				if (score > alpha && score < beta) {
					score = minSearchPVS(newBoard, alpha, beta, depth + 1);
				}
			}
			if (score > alpha) {
				alpha = score;
				if (depth == 0)
					this.move = move;
			}
			if (alpha >= beta)
				break;
		}
		return alpha;
	}

	float minSearchPVS(Board board, float alpha, float beta, int depth) {
		if (isTerminal(board, depth))
			return evaluate(board);
		var moves = order(board.findLegalMoves(Color.WHITE));
		boolean first = true;
		for (var move : moves) {
			var newBoard = board.placed(move);
			float score;
			if (first) {
				score = maxSearchPVS(newBoard, alpha, beta, depth + 1);
				first = false;
			} else {
				score = maxSearchPVS(newBoard, beta - 1, beta, depth + 1);
				if (score > alpha && score < beta) {
					score = maxSearchPVS(newBoard, alpha, beta, depth + 1);
				}
			}
			if (score < beta) {
				beta = score;
			}
			if (alpha >= beta)
				break;
		}
		return beta;
	}

	boolean isTerminal(Board board, int depth) {
		return board.isEnd() || depth >= depthLimit;
	}

	float evaluate(Board board) {
		String key = hashBoard(board);
		if (transpositionTable.containsKey(key)) {
			return transpositionTable.get(key);  // float return
		}
		float value = eval.value(board);
		transpositionTable.put(key, value);  // float value
		return value;
	}

	String hashBoard(Board board) {
		return Arrays.toString(board.toString().getBytes());
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
			BitBoard nextBoard = board.clone().placed(move);
			float value = eval.value(nextBoard);
			scoredMoves.add(new ScoredMove(move, value));
		}
		scoredMoves.sort((a, b) -> Float.compare(b.score, a.score));
		return scoredMoves.stream().map(sm -> sm.move).collect(Collectors.toList());
	}
}