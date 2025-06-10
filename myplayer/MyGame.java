package myplayer;

import ap25.*;
import static ap25.Color.*;
import java.util.*;
import java.util.stream.*;
//コンパイル
//cnt14026@cs-t120:~/proouyou/ap25unit1 > javac -d . ap25/*.java myplayer/*.java
//cnt14026@cs-t120:~/proouyou/ap25unit1 > java myplayer.MyGame

public class MyGame {
	public static void main(String args[]) {
		// ゲームのエントリーポイント。黒プレイヤーに MyPlayer（AI）、白プレイヤーに RandomPlayer を設定。MyBoard
		// を初期化し、MyGame インスタンスを作成して play() を実行。
		//先攻
		for(int i=0;i<100;i++) {
			var player1 = new myplayer.MyPlayer(BLACK);
			var player2 = new myplayer.MyPlayer(WHITE);
			var board = new BitBoard();
			var game = new MyGame(board, player1, player2);
			game.play(1);
		}
		//後攻
		/*for(int i=0;i<100;i++) {
			var player1 = new myplayer.MyPlayerO(BLACK);
			var player2 = new myplayer.MyPlayer(WHITE);
			var board = new MyBoard();
			var game = new MyGame(board, player1, player2);
			game.play(2);
		}*/
		
		System.out.println(count);
		System.out.println(count2);

	}

	// TIME_LIMIT_SECONDS: 各プレイヤーの持ち時間（秒）。
	// board: 現在の盤面。
	// black, white: 各プレイヤー。
	// players: 色とプレイヤーの対応マップ。
	// moves: ゲーム中のすべての手の履歴。
	// times: 各プレイヤーの累積思考時間。

	static final float TIME_LIMIT_SECONDS = 60;
	static int count=0;
	static int count2=0;
	Player player1;
	Player player2;
	Board board;
	Player black;
	Player white;
	Map<Color, Player> players;
	List<Move> moves = new ArrayList<>();
	Map<Color, Float> times = new HashMap<>(Map.of(BLACK, 0f, WHITE, 0f));

	public MyGame(Board board, Player black, Player white) {// ゲームの初期化。盤面をコピーし、プレイヤーを登録。

		this.board = board.clone();
		this.black = black;
		this.white = white;
		this.players = Map.of(BLACK, black, WHITE, white);
		this.player1 = black; 
	}

	/*
	 * ゲームのメインループ。 終了条件（board.isEnd()）まで繰り返し： 現在の手番のプレイヤーを取得。 think() を呼び出して手を取得。
	 * 実行時間を計測。 check() で手の妥当性を確認。 合法手なら盤面を更新、不正なら反則負け。 盤面を表示。 最後に printResult()
	 * で結果を表示。
	 * 
	 */
	public void play(int i) {

		this.players.values().forEach(p -> p.setBoard(this.board.clone()));

		while (this.board.isEnd() == false) {
			var turn = this.board.getTurn();
			var player = this.players.get(turn);

			Error error = null;
			long t0 = System.currentTimeMillis();
			Move move;

			// play
			try {
				move = player.think(board.clone()).colored(turn);
			} catch (Error e) {
				error = e;
				move = Move.ofError(turn);
			}

			// record time
			long t1 = System.currentTimeMillis();
			final var t = (float) Math.max(t1 - t0, 1) / 1000.f;
			this.times.compute(turn, (k, v) -> v + t);

			// check
			move = check(turn, move, error);
			moves.add(move);

			// update board
			if (move.isLegal()) {
				board = board.placed(move);
			} else {
				board.foul(turn);
				break;
			}

			//System.out.println(board);
		}

		printResult(board, moves, i);
	}

	/*
	 * 
	 * プレイヤーの手が妥当かを検証。 エラーが発生した場合 → Move.ofError 時間切れ → Move.ofTimeout 合法手でない →
	 * Move.ofIllegal 妥当な手ならそのまま返す。
	 * 
	 */
	Move check(Color turn, Move move, Error error) {
		if (move.isError()) {
			System.err.printf("error: %s %s", turn, error);
			System.err.println(board);
			return move;
		}

		if (this.times.get(turn) > TIME_LIMIT_SECONDS) {
			System.err.printf("timeout: %s %.2f", turn, this.times.get(turn));
			System.err.println(board);
			return Move.ofTimeout(turn);
		}

		var legals = board.findLegalMoves(turn);
		if (move == null || legals.contains(move) == false) {
			System.err.printf("illegal move: %s %s", turn, move);
			System.err.println(board);
			return Move.ofIllegal(turn);
		}

		return move;
	}

	public Player getWinner(Board board) {// 勝者の色を取得し、対応するプレイヤーを返す。
		return this.players.get(board.winner());
	}

	/*
	 * 
	 * 勝敗とスコアを表示。 引き分けなら "draw"、勝者がいれば "○○ won by △△"。 対戦カードと手の履歴も表示。
	 * 
	 */
	public void printResult(Board board, List<Move> moves, int i) {
		var result = String.format("%5s%-9s", "", "draw");
		var score = Math.abs(board.score());
		if (score > 0)
			result = String.format("%-4s won by %-2d", getWinner(board), score);

		var s = toString() + " -> " + result + "\t| " + toString(moves);
		System.out.println(s);
		if(i==1) {
		if(getWinner(board)==player1) {
			count++;
		}else {
			count2++;
		}
		}else if(i==2) {
			if(getWinner(board)==player1) {
				count2++;
			}else {
				count++;
			}
		}
	}

	public String toString() {// "黒 vs 白" の形式でプレイヤー名を表示。
		return String.format("%4s vs %4s", this.black, this.white);
	}

	public static String toString(List<Move> moves) {// 手の履歴を文字列として連結（例：c4d3e3...）。
		return moves.stream().map(x -> x.toString()).collect(Collectors.joining());
	}
}
