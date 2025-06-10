package ap25.league;

import static ap25.Color.BLACK;
import static ap25.Color.WHITE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ap25.Board;
import ap25.Color;
import ap25.Move;
import ap25.Player;

public class Game {
  Board board;
  Player black;
  Player white;
  Map<Color, Player> players;
  List<Move> moves = new ArrayList<Move>();
  Map<Color, Float> times = new HashMap<>(Map.of(BLACK, 0f, WHITE, 0f));
  long timeLimit;

  public Game(Board board, Player black, Player white, long timeLimit) {
    this.board = board.clone();
    this.black = black;
    this.white = white;
    this.players = Map.of(BLACK, black, WHITE, white);
    this.timeLimit = timeLimit;
  }

  public void play() {
    this.players.entrySet().forEach(i -> setBoard(i, this.board.clone()));

    while (this.board.isEnd() == false) {
      var turn = this.board.getTurn();
      var player = this.players.get(turn);

      Throwable error = null;
      long tm = System.currentTimeMillis();
      Move move;

      // play
      try {
        move = player.think(this.board.clone()).colored(turn);
      } catch (Throwable e) {
        error = e;
        move = Move.ofError(turn);
      }

      // record time
      tm = System.currentTimeMillis() - tm;
      final var t = (float) Math.max(tm, 1) / 1000.f;
      this.times.compute(turn, (k, v) -> v + t);

      // check
      move = check(turn, move, error);
      moves.add(move);

      // update board
      if (move.isLegal()) {
        this.board = this.board.placed(move);
      } else {
        this.board.foul(turn);
        break;
      }
    }

    printResult(this.board, moves);
  }

  void setBoard(Map.Entry<Color, Player> entry, Board board) {
    var color = entry.getKey();
    var player = entry.getValue();
    long tm = System.currentTimeMillis();
    try {
      player.setBoard(board);
    } catch (Throwable e) {
      System.err.printf("setBoard failed: %s, %s\n", color, player);
      System.err.println(board);
    }
    tm = System.currentTimeMillis() - tm;
    final var t = (float) tm / 1000.f;
    this.times.compute(color, (k, v) -> v + t);
}

  Move check(Color turn, Move move, Throwable error) {
    if (move.isError()) {
      System.err.printf("error: %s %s\n", turn, error);
      System.err.println(board);
      return move;
    }

    if (this.times.get(turn) > this.timeLimit) {
      System.err.printf("timeout: %s %.2f\n", turn, this.times.get(turn));
      System.err.println(board);
      return Move.ofTimeout(turn);
    }

    var legals = board.findLegalMoves(turn);
    if (move == null || legals.contains(move) == false) {
      System.err.printf("illegal move: %s %s\n", turn, move);
      System.err.println(board);
      return Move.ofIllegal(turn);
    }

    return move;
  }

  public Player getWinner(Board board) {
    return this.players.get(board.winner());
  }

  public void printResult(Board board, List<Move> moves) {
    var result = String.format("%5s%-9s", "", "draw");
    var score = Math.abs(board.score());
    if (score > 0)
      result = String.format("%-4s won by %-2d", getWinner(board), score);

    var s = toString() + " -> " + result + "\t| " + toString(moves);
    System.out.println(s);
  }

  public String toString() {
    return String.format("%4s vs %4s", this.black, this.white);
  }

  public static String toString(List<Move> moves) {
    return moves.stream().map(x -> x.toString()).collect(Collectors.joining());
  }
}
