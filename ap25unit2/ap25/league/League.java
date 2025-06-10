package ap25.league;

import ap25.Color;
import static ap25.Color.BLACK;
import static ap25.Color.BLOCK;
import static ap25.Color.WHITE;
import ap25.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class League {
  final int PARALLELISM = 4;

  int n;
  Player[] players;
  Function<Color, Player[]> builder;
  List<Match> matches;
  Match[][] matrix;
  List<OfficialBoard> boards;
  long timeLimit;

  ForkJoinPool pool = new ForkJoinPool(PARALLELISM);

  public League(int boardNum, Function<Color, Player[]> builder, long timeLimit) {
    this.builder = builder;
    this.players = this.builder.apply(Color.NONE);
    this.n = this.players.length;
    this.timeLimit = timeLimit;
    this.boards = new ArrayList<>();
    this.boards.add(new OfficialBoard());
    for (int i = 0; i < boardNum - 1; i++) {
      this.boards.add(makeBoard());
    }
    this.boards.forEach(b -> {
      System.out.println(b);
      System.out.println();
    });
  }

  OfficialBoard makeBoard() {
    var candidates = List.of(0, 1, 2, 3, 4, 5, 6, 12, 18, 24, 30);
    List<Integer> xs = new ArrayList<>(candidates);
    Collections.shuffle(xs);
    Random rand = new Random();
    var b = new OfficialBoard();

    for (var x: xs.subList(0, rand.nextInt(3) + 1)) {
      b.set(x, BLOCK);
    }

    return b;
  }

  public void run() {
    try {
      setup();
    } catch (Exception e) {
      System.err.println(e);
    }

    executeAsync();
    printResult();
  }

  void setup() throws Exception {
    this.matrix = new Match[this.n][this.n];
    this.matches = new ArrayList<>();

    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.n; j++) {
        if (i == j) continue;
        var black = this.builder.apply(BLACK)[i];
        var white = this.builder.apply(WHITE)[j];
        this.matrix[i][j] = new Match(this.boards, black, white, this.timeLimit);
        this.matches.add(this.matrix[i][j]);
      }
    }

    Collections.shuffle(this.matches);
  }

  void execute() {
    for (var match: matches) {
      match.play();
    }
  }

  void executeAsync() {
    try {
      this.pool.submit(() -> matches.parallelStream()
          .forEach(match -> match.play())).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void printResult() {
    var r = new Result(this.players, this.matrix);
    r.rank();
    System.out.println();
    System.out.println(r);
  }
}
