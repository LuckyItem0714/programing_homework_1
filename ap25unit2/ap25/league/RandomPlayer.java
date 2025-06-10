package ap25.league;

import java.util.Random;
import ap25.Board;
import ap25.Color;
import ap25.Move;
import ap25.Player;

public class RandomPlayer extends Player {
  Random rand = new Random();

  public RandomPlayer(Color color) {
    super("R", color);
  }

  public Move think(Board board) {
    var moves = board.findLegalMoves(getColor());
    var i = this.rand.nextInt(moves.size());
    return moves.get(i);
  }
}
