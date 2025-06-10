package ap25.league;

import static ap25.Color.BLACK;
import static ap25.Color.BLOCK;
import static ap25.Color.NONE;
import static ap25.Color.WHITE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ap25.Board;
import ap25.Color;
import ap25.Move;

public class OfficialBoard implements Board, Cloneable {
  Color board[];
  Move move = new Move(Move.PASS, NONE);
  Set<Integer> nones = new TreeSet<Integer>();

  public OfficialBoard() {
    this.board = Stream.generate(() -> NONE).limit(LENGTH).toArray(Color[]::new);
    this.nones.addAll(IntStream.range(0, LENGTH).boxed().toList());
    init();
  }

  OfficialBoard(Color board[], Move move, Set<Integer> nones) {
    this.board = Arrays.copyOf(board, board.length);
    this.move = move;
    this.nones = new TreeSet<Integer>(nones);
  }

  public OfficialBoard clone() {
    return new OfficialBoard(this.board, this.move, this.nones);
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

  void set(int k, Color color) {
    this.board[k] = color;
    this.nones.remove(k);
  }

  void setAll(Color color) {
    IntStream.range(0, LENGTH).forEach(k -> this.board[k] = color);
  }

  public boolean equals(Object otherObj) {
    if (otherObj instanceof OfficialBoard) {
      var other = (OfficialBoard) otherObj;
      return Arrays.equals(this.board, other.board);
    }
    return false;
  }

  public String toString() {
    return OfficialBoardFormatter.format(this);
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
    return this.nones.stream()
        .filter(k -> isLegal(k, color))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  boolean isLegal(int k, Color color) {
    return lines(k).stream()
        .anyMatch(line -> outflanked(line, color).size() > 0);
  }

  List<List<Integer>> lines(int k) {
    return IntStream.range(0, 8).boxed()
        .map(dir -> Move.line(k, dir)).toList();
  }

  List<Integer> outflanked(List<Integer> line, Color color) {
    if (line.size() > 1) {
      var flippables = new ArrayList<Integer>();
      for (var k: line) {
        var c = get(k);
        if (c == NONE || c == BLOCK) break;
        if (c == color) return flippables;
        flippables.add(k);
      }
    }
    return new ArrayList<Integer>();
  }

  public OfficialBoard placed(Move move) {
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    b.lines(k).forEach(line -> {
      outflanked(line, color).forEach(k1 -> b.board[k1] = color);
    });
    b.set(k, color);

    return b;
  }

  public OfficialBoard flipped() {
    var b = clone();
    IntStream.range(0, LENGTH).forEach(k -> b.board[k] = b.board[k].flipped());
    b.move = this.move.flipped();
    return b;
  }
}
