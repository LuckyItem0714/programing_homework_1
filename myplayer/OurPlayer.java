package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;

import ap25.*;

public class OurPlayer extends ap25.Player {
	/*
	 *
	 * MyEval eval: 評価関数。 int depthLimit: 探索の深さ制限。 Move move: 現在選んでいる手。 MyBoard
	 * board: 内部で保持する盤面。
	 *
	 */
	static final String MY_NAME = "MY24";
	Move move;
	BitBoard board;
	NegaScoutStrategy strategy;

	public OurPlayer(Color color) {// デフォルト名 "MY24"、評価関数、深さ2で初期化。
		this(MY_NAME, color);
	}

	public OurPlayer(String name, Color color) {// 名前、色、評価関数、探索深さを指定して初期化。
		super(name, color);
	}

	public void setBoard(Board board) {// 外部から渡された盤面を内部の BitBoard にコピー。
		this.board = new BitBoard(board);
        strategy = new NegaScoutStrategy();
        //AdaptiveNegaScoutが完成したら差し替え
		/*if(this.board.noBlock()){
			strategy = new AdaptiveNegaScout();
		}
		else{
			strategy = new NegaScoutStrategy();
		}*/
	}

	boolean isBlack() {
		return getColor() == BLACK;
	}

	public Move think(Board board) {
		this.board = this.board.placed(board.getMove());

		var newBoard = isBlack() ? this.board.clone() : this.board.flipped();
		this.move = strategy.search(newBoard);
		this.move = this.move.colored(getColor());

		this.board = this.board.placed(this.move);
		return this.move;
		/*
		 * プレイヤーの思考メソッド。次の手を決定します。 処理の流れ： 直前の手を placed() で盤面に反映。 合法手がなければ PASS を返す。
		 * 自分が白番なら盤面を反転（白視点で探索）。 maxSearch() を呼び出して最善手を探索。 結果の手を自分の色に戻して返す。
		 */
	}
}
