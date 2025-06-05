package myplayer;

import static ap25.Board.*;
import static ap25.Color.*;

import java.util.List;
import java.util.Map;

import ap25.*;

public class BitBoardFormatter {
  public static String format(BitBoard board) {
    var turn = board.getTurn();
    var move = board.getMove();
    var blacks = board.findNoPassLegalIndexes(BLACK);
    var whites = board.findNoPassLegalIndexes(WHITE);
    var legals = Map.of(BLACK, blacks, WHITE, whites);

    var buf = new StringBuilder("  ");
    for (int k6 = 0; k6 < SIZE; k6++) buf.append(Move.toColString(k6));
    buf.append("\n");

    for (int k6 = 0; k6 < SIZE * SIZE; k6++) {
      int col = k6 % SIZE;
      int row = k6 / SIZE;
      int k8 = 8 * (row + 1) + col + 1;

      if (col == 0) buf.append((row + 1) + "|");

      if (board.get(k6) == NONE) {
        boolean legal = false;
        var b = blacks.contains(k8);
        var w = whites.contains(k8);
        if (turn == BLACK && b) legal = true;
        if (turn == WHITE && w) legal = true;
        buf.append(legal ? '.' : ' ');
      } else {
        var s = board.get(k6).toString();
        if (move != null && k8 == move.getIndex()) s = s.toUpperCase();
        buf.append(s);
      }

      if (col == SIZE - 1) {
        buf.append("| ");
        if (row == 0 && move != null) {
          buf.append(move);
        } else if (row == 1) {
          buf.append(turn + ": " + toString(legals.get(turn)));
        }
        buf.append("\n");
      }
    }

    buf.setLength(buf.length() - 1);
    return buf.toString();
  }

  static List<String> toString(List<Integer> moves) {
    return moves.stream().map(k6 -> Move.toIndexString(k6)).toList();
  }
}