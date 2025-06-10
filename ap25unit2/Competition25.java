import ap25.*;
import static ap25.Color.*;
import ap25.league.*;
import java.util.function.*;

class Competition25 {
  final static long TIME_LIMIT_SECONDS = 60;

  public static void main(String args[]) {
    Function<Color, Player[]> builder = (Color color) -> {
      return new Player[] {
          new p25x13.OurPlayer(color),
          new ap25.league.RandomPlayer(color),
          new ap25.league.RandomPlayer(color),
          new ap25.league.RandomPlayer(color),
      };
    };

    var league = new League(5, builder, TIME_LIMIT_SECONDS);
    league.run();
  }

  public static void singleGame(String args[]) {
    var player1 = new p25x13.OurPlayer(BLACK);
    var player2 = new p25x13.OurPlayer(WHITE);
    var board = new OfficialBoard();
    var game = new Game(board, player1, player2, TIME_LIMIT_SECONDS);
    game.play();
  }
}
