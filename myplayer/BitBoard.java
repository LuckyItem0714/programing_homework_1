package myplayer;

import static ap25.Color.*;

import java.util.ArrayList;
import java.util.List;

import ap25.*;

public class BitBoard implements Board, Cloneable {
//6x6盤で見たインデックスはk6, 8x8盤で見たインデックスはk8と表す。

  private static final int[] DIRECTIONS = new int[]{1, 7, 8, 9};//それぞれ、(W, E), (NE, SW), (N, S), (NW, SE)を表す。
 
  protected long black;//ビットボード。盤外は常に0を保つ
  protected long white;
  protected long occupied;//白または黒がおかれている場所を示す
  protected long empty;//空いている場所を示す
  private Move move = Move.ofPass(NONE);

  public BitBoard() {//単純な初期化
    this.black = 0x1008000000L;
    this.white = 0x810000000L;
    this.update();
  }

  private BitBoard(long black, long white, Move move) {//コピー用
    this.black = black;
    this.white = white;
    this.update();
    this.move = move;
  }

  @Override
  public BitBoard clone() {
    return new BitBoard(this.black, this.white, this.move);
  }

  protected void update(){//occuiedとemptyを更新
    this.occupied = black | white;
    this.empty = (~this.occupied) & BitBoardUtil.PLAYABLE_6x6;
  }

  public Color get(int k6) {//インデックスから色を取得
    long mask = 1L << BitBoardUtil.IDX_6_TO_8[k6];
    if((black & mask) != 0) return BLACK;
    if((white & mask) != 0) return WHITE;
    return NONE;
  }

  public Move getMove() { return this.move; }

  public long getBlack() { return this.black; }

  public long getWhite() { return this.white; }

  public long getBitBoard(Color color){//colorのビットボードを取得
    if(color == BLACK) { return this.black; }
    if(color == WHITE) { return this.white; }
    else{ return 0L; }
  }

  public Color getTurn() {//次の手番を返す。黒が初手。以降は交互
    return this.move.isNone() ? BLACK : this.move.getColor().flipped();
  }

  public void set(int k8, Color color) {
    //k8に指定された色の石を置く
    //引数k8は8x8盤から見たインデックスで受けとる
    long mask = 1L << k8;
    if((occupied & mask) != 0) { return; }
    if(color == BLACK) { black |= mask; }
    if(color == WHITE) { white |= mask; }
    update();
  }

  private static void applyFlips(BitBoard board, long flips, Color color) {//ビットボードのflipsをcolorに置き換え
    if (color == BLACK) {
        board.black |= flips;
        board.white &= ~flips;
    } else {
        board.black &= ~flips;
        board.white |= flips;
    }
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
    //盤面の状態からハッシュ値を生成
    // *31 は順序を考慮しつつ分布を広げるための慣用手法らしい
    return Long.hashCode(black) * 31 + Long.hashCode(white);
  }

  public String toString() {//盤面の表示をFormatterに投げる
    return BitBoardFormatter.format(this);
  }

  public int count(Color color) {//石のカウント
    return Long.bitCount(getBitBoard(color));
  }

  public boolean isEnd() {//終了判定
    var lbs = findNoPassLegalIndexes(BLACK);
    var lws = findNoPassLegalIndexes(WHITE);
    return lbs.size() == 0 && lws.size() == 0;
  }

  public Color winner() {//終了状態なら勝者のColorを返す。
    var v = score();
    if (isEnd() == false || v == 0 ) return NONE;
    return v > 0 ? BLACK : WHITE;
  }

  public void foul(Color color) {//反則処理
    var winner = color.flipped();
    applyFlips(this, BitBoardUtil.PLAYABLE_6x6, winner);
    update();
  }

  public int score() {//スコア(石差)を計算
    var bs = count(BLACK);
    var ws = count(WHITE);

    return (int) (bs - ws);
  }

  public List<Move> findLegalMoves(Color color) {//
    List<Move> moves = new ArrayList<>();//プリミティブ型にするほうが高速
    for (int k8 : findLegalIndexes(color)) {
        moves.add(new Move(k8 >= 0 ? BitBoardUtil.IDX_8_TO_6[k8] : k8, color));
    }
    return moves;
    /*return findLegalIndexes(color).stream()
        .map(k8 -> new Move(idx8to6(k8), color)).toList();*/
  }

  private List<Integer> findLegalIndexes(Color color){
    var moves = findNoPassLegalIndexes(color);
    if (moves.size() == 0) moves.add(Move.PASS);
    return moves;
  }

  List<Integer> findNoPassLegalIndexes(Color color) {
    return BitBoardUtil.bitmaskToIndices(findLegalMovesBitmask(color));//long石をおける場所をlongからリストに変換して返す。
  }
    
  long findLegalMovesBitmask(Color color){
    //着手可能位置をビット列で返す
    // TODO: 時間があればfor文の内部を共通化して、プロファイリングにより効率の良い実装を選定する
    long legal = 0L;
    var player = getBitBoard(color);
    var opponent = getBitBoard(color.flipped());

    for(int d : DIRECTIONS){
        //右シフトで(W, NE, N, NW)を確認
        long candidates = (player >>> d) & opponent;//dの方向でplayerとopponentの石が隣り合っている場所
        long temp = candidates;
        while (temp != 0) {
            temp = (temp >>> d) & opponent;
            candidates |= temp;//opponentの色の石のうち、dの逆方向にplayerの色の石がある者を保存
        }
        legal |= candidates >>> d;//さらに1マスシフトでopponetのd方向の隣接マスへ

        //左シフトで(E, SW, S, SE)を確認。動作は上と同様
        candidates = (player << d) & opponent;
        temp = candidates;
        while (temp != 0) {
            temp = (temp << d) & opponent;
            candidates |= temp;
        }
        legal |= candidates << d;
    }

    return legal & empty;//候補位置と空きマスの合併をとる
  }

  public BitBoard placed(Move move) {
    //Moveを実行した後の盤面を返す
    // TODO: 時間があればfor文の内部を共通化して、プロファイリングにより効率の良い実装を選定する
    var b = clone();
    b.move = move;

    if (move.isPass() || move.isNone())
      return b;

    Color color = move.getColor();
    long mask = 1L << BitBoardUtil.IDX_6_TO_8[move.getIndex()];

    long player = b.getBitBoard(color);
    long opponent = b.getBitBoard(color.flipped());
    long flips = mask;//最後に反転
    
    for(int d : DIRECTIONS){
        //左、上側の処理
        //
      long flipsDir = 0L;
      long cursor = mask >>> d;

      while((cursor & opponent) != 0){//maskのd方向の相手の石をflipsDirに積み上げる
        flipsDir |= cursor;
        cursor = cursor >>> d;
      }
      if((cursor & player) != 0){
        flips |= flipsDir;//反対側に自分の石があれば反転
      }
        //右、下側の処理。上と同様
      flipsDir = 0L;
      cursor = mask << d;
      while((cursor & opponent) != 0){
        flipsDir |= cursor;
        cursor = cursor << d;
      }
      if((cursor & player) != 0){
        flips |= flipsDir;
      }
    }

    applyFlips(b, flips, color);    
    b.update();

    return b;
  }

  public BitBoard flipped() {
    var b = clone();
    b.black = this.white;
    b.white = this.black;
    return b;
  }
}
