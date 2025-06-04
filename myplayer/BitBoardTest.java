package myplayer;

import ap25.*;
import static ap25.Color.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class BitBoardTest {

    @Test
    public void constructorTest() {
        BitBoard b = new BitBoard();
        long black = (1L << 27) | (1L << 36);
        long white = (1L << 28) | (1L << 35);
        assertEquals(black, b.getBlack());
        assertEquals(white, b.getWhite());
    }

    @Test
    public void cloneTest() {
        BitBoard b1 = new BitBoard();
        BitBoard b2 = b1.clone();
        assertEquals(b1.getBlack(), b2.getBlack());
        assertEquals(b1.getWhite(), b2.getWhite());
        assertNotSame(b1, b2);
    }

    @Test
    public void getSetTest() {
        BitBoard b = new BitBoard();
        b.set(10, BLACK);
        assertEquals(BLACK, b.get(10));
        b.set(11, WHITE);
        assertEquals(WHITE, b.get(11));
    }

    @Test
    public void countTest() {
        BitBoard b = new BitBoard();
        assertEquals(2, b.count(BLACK));
        assertEquals(2, b.count(WHITE));
    }

    @Test
    public void getTurnTest() {
        BitBoard b = new BitBoard();
        assertEquals(BLACK, b.getTurn());
    }

    @Test
    public void equalsAndHashCodeTest() {
        BitBoard b1 = new BitBoard();
        BitBoard b2 = new BitBoard();
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    public void flippedTest() {
        BitBoard b = new BitBoard();
        BitBoard f = b.flipped();
        assertEquals(b.getBlack(), f.getWhite());
        assertEquals(b.getWhite(), f.getBlack());
    }

    @Test
    public void legalMovesTest() {
        BitBoard b = new BitBoard();
        List<Move> moves = b.findLegalMoves(BLACK);
        assertFalse(moves.isEmpty());
    }



    @Test
    public void isEndAndWinnerTest() {
        BitBoard b = new BitBoard();
        assertFalse(b.isEnd());
        assertEquals(NONE, b.winner());
    }

    @Test
    public void foulTest() {
        BitBoard b = new BitBoard();
        b.foul(BLACK);
        assertEquals(0L, b.getBlack());
        assertEquals((0x3F << 9) | (0x3F << 17) | (0x3F << 25) | (0x3F << 33) | (0x3F << 41) | (0x3F << 49), b.getWhite());
    }

    @Test
    public void getLegalMovesTest(){//修正中
        BitBoard b = new BitBoard();
        long l = b.getLegalMoves(BLACK);
        System.out.println("着手可能位置" + l);
        long l2 = (1L << 29) | (1L << 20) | (1L << 34) | (1L << 43);
        assertEquals(l, l2);
    }

    @Test
    public void placedTest() {//修正中
        BitBoard b = new BitBoard();
        Move m = new Move(29, BLACK);
        BitBoard b2 = b.placed(m);
        assertEquals(b2.getWhite(), (1L << 35));
    }
}
