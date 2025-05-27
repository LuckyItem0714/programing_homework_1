package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ap25.*;

public class MyBoard implements Board, Cloneable {
  Color board[];//各マスの状態
  Move move = Move.ofPass(NONE);//最後に行われた手

  public MyBoard() {
    this.board = Stream.generate(() -> NONE).limit(LENGTH).toArray(Color[]::new);
    init();
    //盤面をすべてNONEで初期化し、initで中央４マスに初期配置
  }

  MyBoard(Color board[], Move move) {
    this.board = Arrays.copyOf(board, board.length);
    this.move = move;
    //盤面のコピー用
  }

  public MyBoard clone() {
    return new MyBoard(this.board, this.move);//ボードのコピー
  }

  void init() {//オセロの初期配置
    set(Move.parseIndex("c3"), BLACK);
    set(Move.parseIndex("d4"), BLACK);
    set(Move.parseIndex("d3"), WHITE);
    set(Move.parseIndex("c4"), WHITE);
  }

  public Color get(int k) { return this.board[k]; }//ｋのマスの色を取得
  public Move getMove() { return this.move; }//最後に行われた手

  public Color getTurn() {//現在の手番を返す　最初の手(None)なら黒、その後は前回の手を反転
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k, Color color) {
    this.board[k] = color;//ボードのkの色を設定、黒か白
  }

  public boolean equals(Object otherObj) {//ボードの状態が等しいか判定
    if (otherObj instanceof MyBoard) {
      var other = (MyBoard) otherObj;
      return Arrays.equals(this.board, other.board);
    }
    return false;
  }

  public String toString() {
    return MyBoardFormatter.format(this);//盤面を文字列に変換
  }

  public int count(Color color) {//指定した色の数をカウント
    return countAll().getOrDefault(color, 0L).intValue();
  }

  public boolean isEnd() {//お互いのプレイヤーに打てる手がなければ終了
    var lbs = findNoPassLegalIndexes(BLACK);
    var lws = findNoPassLegalIndexes(WHITE);
    return lbs.size() == 0 && lws.size() == 0;
  }

  public Color winner() {//勝者の色を返す。引き分け、未終了ならNONE
    var v = score();
    if (isEnd() == false || v == 0 ) return NONE;
    return v > 0 ? BLACK : WHITE;
  }

  public void foul(Color color) {//指定した色が反則した場合、相手の勝ちとして盤面をすべて相手の色に
    var winner = color.flipped();
    IntStream.range(0, LENGTH).forEach(k -> this.board[k] = winner);
  }

  public int score() {//黒と白の石の数の差を返す。両者0個の場合は空白マス数を加味してスコア調整。
    var cs = countAll();
    var bs = cs.getOrDefault(BLACK, 0L);
    var ws = cs.getOrDefault(WHITE, 0L);
    var ns = LENGTH - bs - ws;
    int score = (int) (bs - ws);

    if (bs == 0 || ws == 0)
        score += Integer.signum(score) * ns;

    return score;
  }

  Map<Color, Long> countAll() {//それぞれｎ色の石の数をマップで返す。
    return Arrays.stream(this.board).collect(
        Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }

  public List<Move> findLegalMoves(Color color) {//打てる手をリストで返す
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  List<Integer> findLegalIndexes(Color color) {//打てる手をインデックスで返す。打てる手がなければパスを追加
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0) moves.add(Move.PASS);
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {//パス以外の打てる手のインデックスを返す。各空白マスに対して、相手の石を挟めるかをチェック。
    var moves = new ArrayList<Integer>();
    for (int k = 0; k < LENGTH; k++) {
      var c = this.board[k];
      if (c != NONE) continue;
      for (var line : lines(k)) {
        var outflanking = outflanked(line, color);
        if (outflanking.size() > 0){//変更点。kがLegalなことがわかれば残りのチェックを飛ばす
          moves.add(k);
          break;
        }
      }
    }
    return moves;
  }

  List<List<Integer>> lines(int k) {//指定マス k から8方向に伸びるラインを取得。
    var lines = new ArrayList<List<Integer>>();
    for (int dir = 0; dir < 8; dir++) {
      var line = Move.line(k, dir);
      lines.add(line);
    }
    return lines;
  }

  List<Integer> outflanked(List<Integer> line, Color color) {//指定ライン上で相手の石を挟めるかを判定。挟める場合はその石の位置を Move として返す。
//変更点。Moveのリストを返していたのを,Integerのリストを返すようにした。
    if (line.size() <= 1) return new ArrayList<Integer>();
    var flippables = new ArrayList<Integer>();
    for (int k: line) {
      var c = get(k);
      if (c == NONE || c == BLOCK) break;
      if (c == color) return flippables;
      flippables.add(new Move(k, color));
    }
    return new ArrayList<Integer>();
  }

  public MyBoard placed(Move move) {//指定した手を適用した新しい盤面を返す。挟める石をすべて反転させ、手を記録
    var b = clone();
    b.move = move;

    if (move.isPass() | move.isNone())
      return b;

    var k = move.getIndex();
    var color = move.getColor();
    var lines = b.lines(k);
    for (var line: lines) {
      for (var p: outflanked(line, color)) {
        b.board[p] = color;//変更点。outflankedの変更の影響
      }
    }
    b.set(k, color);

    return b;
  }

  public MyBoard flipped() {//盤面と手番の色を反転した新しい盤面を返す。
    var b = clone();
    IntStream.range(0, LENGTH).forEach(k -> b.board[k] = b.board[k].flipped());
    b.move = this.move.flipped();
    return b;
  }
}
