package myplayer;

import ap25.*;
import java.util.Random;

public class RandomPlayer extends Player {
  Random rand = new Random();//乱数を生成

  public RandomPlayer(Color color) {//プレイヤーの名前を "R" に設定。色（BLACK または WHITE）は引数で指定。
    super("R", color);
  }

  public Move think(Board board) {
    var moves = board.findLegalMoves(getColor());
    var i = this.rand.nextInt(moves.size());
    return moves.get(i);
    /*
     * 
	board.findLegalMoves(getColor())
	現在のプレイヤーの合法手をすべて取得。
	rand.nextInt(moves.size())
	合法手の数を上限としてランダムなインデックスを生成。
	moves.get(i)
	ランダムに選ばれた手を返す
     */
  }
}
