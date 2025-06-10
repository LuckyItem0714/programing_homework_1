package p25x13;

import static ap25.Board.SIZE;
import static ap25.Color.BLACK;
import static ap25.Color.NONE;
import static ap25.Color.WHITE;
import ap25.Move;
import java.util.List;
import java.util.Map;

public class OurBoardFormatter {
  public static String format(OurBoard board) {
    var turn = board.getTurn();
    var move = board.getMove();
    var blacks = board.findNoPassLegalIndexes(BLACK);
    var whites = board.findNoPassLegalIndexes(WHITE);
    var legals = Map.of(BLACK, blacks, WHITE, whites);

    var buf = new StringBuilder("  ");
    for (int k = 0; k < SIZE; k++) buf.append(Move.toColString(k));
    buf.append("\n");

    for (int k = 0; k < SIZE * SIZE; k++) {
      int col = k % SIZE;
      int row = k / SIZE;

      if (col == 0) buf.append((row + 1) + "|");

      if (board.get(k) == NONE) {
        boolean legal = false;
        var b = blacks.contains(k);
        var w = whites.contains(k);
        if (turn == BLACK && b) legal = true;
        if (turn == WHITE && w) legal = true;
        buf.append(legal ? '.' : ' ');
      } else {
        var s = board.get(k).toString();
        if (move != null && k == move.getIndex()) s = s.toUpperCase();
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
    return moves.stream().map(k -> Move.toIndexString(k)).toList();
  }
}
