package p25x13;

import static ap25.Color.BLACK;
import static ap25.Color.BLOCK;
import static ap25.Color.NONE;
import static ap25.Color.WHITE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ap25.Board;
import ap25.Color;
import ap25.Move;

public class OurBoard implements Board, Cloneable {
  Color board[];
  Move move = Move.ofPass(NONE);

  public OurBoard() {
    this.board = Stream.generate(() -> NONE).limit(LENGTH).toArray(Color[]::new);
    init();
  }

  OurBoard(Color board[], Move move) {
    this.board = Arrays.copyOf(board, board.length);
    this.move = move;
  }

  public OurBoard clone() {
    return new OurBoard(this.board, this.move);
  }

  void init() {
    set(Move.parseIndex("c3"), BLACK);
    set(Move.parseIndex("d4"), BLACK);
    set(Move.parseIndex("d3"), WHITE);
    set(Move.parseIndex("c4"), WHITE);
  }

  public Color get(int k) { return this.board[k]; }
  public Move getMove() { return this.move; }

  public Color getTurn() {
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k, Color color) {
    this.board[k] = color;
  }

  public boolean equals(Object otherObj) {
    if (otherObj instanceof OurBoard) {
      var other = (OurBoard) otherObj;
      return Arrays.equals(this.board, other.board);
    }
    return false;
  }

  public String toString() {
    return OurBoardFormatter.format(this);
  }

  public int count(Color color) {
    return countAll().getOrDefault(color, 0L).intValue();
  }

  public boolean isEnd() {
    var lbs = findNoPassLegalIndexes(BLACK);
    var lws = findNoPassLegalIndexes(WHITE);
    return lbs.size() == 0 && lws.size() == 0;
  }

  public Color winner() {
    var v = score();
    if (isEnd() == false || v == 0 ) return NONE;
    return v > 0 ? BLACK : WHITE;
  }

  public void foul(Color color) {
    var winner = color.flipped();
    IntStream.range(0, LENGTH).forEach(k -> this.board[k] = winner);
  }

  public int score() {
    var cs = countAll();
    var bs = cs.getOrDefault(BLACK, 0L);
    var ws = cs.getOrDefault(WHITE, 0L);
    var ns = LENGTH - bs - ws;
    int score = (int) (bs - ws);

    if (bs == 0 || ws == 0)
        score += Integer.signum(score) * ns;

    return score;
  }

  Map<Color, Long> countAll() {
    return Arrays.stream(this.board).collect(
        Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }

  public List<Move> findLegalMoves(Color color) {
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  List<Integer> findLegalIndexes(Color color) {
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0) moves.add(Move.PASS);
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {
    var moves = new ArrayList<Integer>();
    for (int k = 0; k < LENGTH; k++) {
      var c = this.board[k];
      if (c != NONE) continue;
      for (var line : lines(k)) {
        var outflanking = outflanked(line, color);
        if (outflanking.size() > 0) moves.add(k);
      }
    }
    return moves;
  }

  List<List<Integer>> lines(int k) {
    var lines = new ArrayList<List<Integer>>();
    for (int dir = 0; dir < 8; dir++) {
      var line = Move.line(k, dir);
      lines.add(line);
    }
    return lines;
  }

  List<Move> outflanked(List<Integer> line, Color color) {
    if (line.size() <= 1) return new ArrayList<>();
    var flippables = new ArrayList<Move>();
    for (int k: line) {
      var c = get(k);
      if (c == NONE || c == BLOCK) break;
      if (c == color) return flippables;
      flippables.add(new Move(k, color));
    }
    return new ArrayList<>();
  }

  public OurBoard placed(Move move) {
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    var lines = b.lines(k);
    for (var line: lines) {
      for (var p: outflanked(line, color)) {
        b.board[p.getIndex()] = color;
      }
    }
    b.set(k, color);

    return b;
  }

  public OurBoard flipped() {
    var b = clone();
    IntStream.range(0, LENGTH).forEach(k -> b.board[k] = b.board[k].flipped());
    b.move = this.move.flipped();
    return b;
  }
}
