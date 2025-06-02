package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.List;
import java.util.Map;

import ap25.*;

public class MyBoardFormatter {
	public static String format(MyBoard board) {
		var turn = board.getTurn();
		var move = board.getMove();
		var blacks = board.findNoPassLegalIndexes(BLACK);
		var whites = board.findNoPassLegalIndexes(WHITE);
		var legals = Map.of(BLACK, blacks, WHITE, whites);
		// 現在の手番（turn）と最後の手（move）を取得。
		// 黒と白の合法手（パスを除く）を取得。
		// legals に色ごとの合法手をマップとして格納
		var buf = new StringBuilder("  ");
		for (int k = 0; k < SIZE; k++)
			buf.append(Move.toColString(k));
		buf.append("\n");//盤面の上部に列ラベル（a〜f）を表示。

		for (int k = 0; k < SIZE * SIZE; k++) {//各マス（0〜35）を走査。行・列を計算。
			int col = k % SIZE;
			int row = k / SIZE;

			if (col == 0)//行の先頭に行番号（1〜6）を表示。
				buf.append((row + 1) + "|");

			if (board.get(k) == NONE) {//マスの内容を表示。空白マスなら、合法手なら .、そうでなければ空白。石がある場合は "o"（黒）や "x"（白）を表示。最後の手の位置は大文字（例："O"）で強調。
				boolean legal = false;
				var b = blacks.contains(k);
				var w = whites.contains(k);
				if (turn == BLACK && b)
					legal = true;
				if (turn == WHITE && w)
					legal = true;
				buf.append(legal ? '.' : ' ');
			} else {
				var s = board.get(k).toString();
				if (move != null && k == move.getIndex())
					s = s.toUpperCase();
				buf.append(s);
			}

			if (col == SIZE - 1) {//1行目の下に最後の手（例：c4）を表示。2行目の下に現在の手番と合法手一覧を表示。
				buf.append("| ");
				if (row == 0 && move != null) {
					buf.append(move);
				} else if (row == 1) {
					buf.append(turn + ": " + toString(legals.get(turn)));
				}
				buf.append("\n");
			}
		}

		buf.setLength(buf.length() - 1);//最後の余分な改行を削除して、整形済み文字列を返す。
		return buf.toString();
	}

	static List<String> toString(List<Integer> moves) {
		return moves.stream().map(k -> Move.toIndexString(k)).toList();//インデックスのリストを "a1" 形式の文字列リストに変換。
	}
}
