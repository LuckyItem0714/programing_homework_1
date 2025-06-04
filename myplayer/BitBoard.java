package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;


import ap25.*;

public class BitBoard implements Board, Cloneable {
  private static final long PLAYABLE_6x6 = //8x8盤面の中央に6x6盤面を埋め込む
    (0x3F << 9) | (0x3F << 17) | (0x3F << 25) | (0x3F << 33) | (0x3F << 41) | (0x3F << 49);
  private static final int[] DIRECTIONS = new int[]{1, 7, 8, 9};//それぞれ、(W, E), (NE, SW), (N, S), (NW, SE)を表す。
 
  private long black;
  private long white;
  private long occupied;
  private long empty;
  Move move = Move.ofPass(NONE);

  public BitBoard() {
    init();
  }

  private BitBoard(long black, long white, Move move) {
    this.black = black;
    this.white = white;
    this.occupied = black | white;
    this.empty = (~occupied) & PLAYABLE_6x6;
    this.move = move;
  }

  @Override
  public BitBoard clone() {
    return new BitBoard(this.black, this.white, this.move);
  }

  void init() {
    this.black = (1L << 27) | (1L << 36);
    this.white = (1L << 28) | (1L << 35);
  }

  public Color get(int k) {
    long mask = 1L << k;
    if((black & mask) != 0) return BLACK;
    if((white & mask) != 0) return WHITE;
    return NONE;
  }

  public Move getMove() { return this.move; }

  public long getBlack() { return this.black; }

  public long getWhite() { return this.white; }

  public long getBoard(Color color){
    if(color == BLACK) { return this.black; }
    if(color == WHITE) { return this.white; }
    else{ return 0L; }
  }

  public Color getTurn() {//次の手番を返す。黒が初手。以降は交互
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k, Color color) {
    long mask = 1L << k;
    if((occupied & mask) != 0) { return; }
    if(color == BLACK) { black |= mask; }
    if(color == WHITE) { white |= mask; }
  }

  public boolean equals(Object otherObj) {
      if (otherObj instanceof BitBoard) {//同値判定。盤面が同じなら、直前の手に関わらず同値とみなす
      var other = (BitBoard) otherObj;
      return black == other.getBlack() && white == other.getWhite();
    }
    return false;
  }

  @Override
  public int hashCode(){
    return Long.hashCode(black) * 31 + Long.hashCode(white);
  }

  public String toString() {
    return BitBoardFormatter.format(this);
  }

  public int count(Color color) {
    return Long.bitCount(getBoard(color));
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
    if(winner == BLACK){
      black = PLAYABLE_6x6;
      white = 0L;
    }
    else{
      black = 0L;
      white = PLAYABLE_6x6;
    }
  }

  public int score() {
    var bs = count(BLACK);
    var ws = count(WHITE);
    var ns = LENGTH - bs - ws;
    int score = (int) (bs - ws);

    return score;
  }

  /*Map<Color, Long> countAll() {
    return Arrays.stream(this.board).collect(
        Collectors.groupingBy(Function.identity(), Collectors.counting()));
  }*/

  public List<Move> findLegalMoves(Color color) {
    return findLegalIndexes(color).stream()
        .map(k -> new Move(k, color)).toList();
  }

  List<Integer> findLegalIndexes(Color color) {//可能な手を返す。なければPASS
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0) moves.add(Move.PASS);
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {
    return bitmaskToIndexes8(getLegalMoves(color));
  }
    

  long getLegalMoves(Color color){
    long legal = 0L;
    var player = getBoard(color);
    var opponent = getBoard(color.flipped());

    long mask;
    long acc;
    for(int d : DIRECTIONS){
      mask = (player >>> d) & opponent;//W, NE, N, NW
      acc = mask;
      while(mask != 0){
        mask = (mask >>> d) & opponent;
        acc = (mask << d) ^ acc ^ mask;
      }
      legal |= (acc >>> d);
      
      mask = (player << d) & opponent;//W, NE, N, NW
      acc = mask;
      while(mask != 0){
        mask = (mask << d) & opponent;
        acc = (mask >>> d) ^ acc ^ mask;
      }
      legal |= (acc << d);
    }

    return legal;
  }

  private List<Integer> bitmaskToIndexes8(long mask){
    List<Integer> list = new ArrayList<>();
    while(mask != 0){
      int idx = Long.numberOfTrailingZeros(mask);
      list.add(idx);
      mask &= (mask - 1);
    }
    return list;
  }

  public BitBoard placed(Move move) {
    var b = clone();
    b.move = move;

    if (move.isPass() || move.isNone())
      return b;

    int k = move.getIndex();
    Color color = move.getColor();
    long mask = 1L << k;

    if(color == BLACK){
      b.black |= mask;
    }
    else{
      b.white |= mask;
    }

    long player = b.getBoard(color);
    long opponent = b.getBoard(color.flipped());
    long flips = 0L;
    long flippsDir;
    long cursor;
    for(int d : DIRECTIONS){
      flippsDir = 0L;
      cursor = mask >>> d;
      while((cursor & opponent) != 0){
        flippsDir |= cursor;
        cursor = cursor >>> d;
      }
      if((cursor & player) != 0){
        flips |= flippsDir;
      }

      flippsDir = 0L;
      cursor = mask << d;
      while((cursor & opponent) != 0){
        flippsDir |= cursor;
        cursor = cursor << d;
      }
      if((cursor & player) != 0){
        flips |= flippsDir;
      }
    }

    if(color == BLACK){
      b.black |= flips;
      b.white &= ~flips;
    }
    else{
      b.black &= ~flips;
      b.white |= flips;
    }
    
    b.occupied = b.black | b.white;
    b.empty = (~b.occupied) & PLAYABLE_6x6;

    return b;
  }

  public BitBoard flipped() {
    var b = clone();
    b.black = this.white;
    b.white = this.black;
    return b;
  }
}
