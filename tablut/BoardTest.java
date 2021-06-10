package tablut;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;


/** Tablut Game board testing.
 *  @author Citlalli Villarreal
 */
public class BoardTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    @Test
    public void testput() {
        Board board1 = new Board();
        board1.init();
        board1.put(Piece.KING, Square.sq(2, 8));
        assertEquals(Piece.KING, board1.get(2, 8));

        board1.put(Piece.BLACK, Square.sq(1, 1));
        assertEquals(Piece.BLACK, board1.get(1, 1));

        board1.put(Piece.WHITE, Square.sq(6, 0));
        assertEquals(Piece.WHITE, board1.get(6, 0));
    }

    @Test
    public void testcopy() {
        Board board1 = new Board();
        board1.init();
        board1.put(Piece.KING, Square.sq(2, 8));
        board1.put(Piece.BLACK, Square.sq(1, 1));
        board1.put(Piece.WHITE, Square.sq(6, 0));

        Board board2 = new Board();
        board2.init();
        board2.copy(board1);

        assertEquals(board2.encodedBoard(), board1.encodedBoard());
    }

    @Test
    public void testkingPosition() {
        Board board1 = new Board();
        board1.init();
        board1.put(Piece.KING, Square.sq(2, 8));
        board1.put(Piece.BLACK, Square.sq(4, 4));
        assertEquals(Square.sq(2, 8), board1.kingPosition());
    }

    @Test
    public void testget() {
        Board board1 = new Board();
        board1.init();
        board1.put(Piece.KING, Square.sq(2, 8));
        board1.put(Piece.BLACK, Square.sq(1, 1));
        board1.put(Piece.WHITE, Square.sq(6, 0));

        assertEquals(Piece.KING, board1.get(2, 8));
        assertEquals(Piece.BLACK, board1.get(1, 1));
        assertEquals(Piece.WHITE, board1.get(6, 0));
    }

    @Test
    public void testisunblockedmove() {
        Board board1 = new Board();
        board1.init();
        assertTrue(board1.isUnblockedMove(Square.sq("d9"), Square.sq("d6")));
        assertFalse(board1.isUnblockedMove(Square.sq("d9"), Square.sq("d5")));
        assertFalse(board1.isUnblockedMove(Square.sq("e9"), Square.sq("e7")));
        assertTrue(board1.isUnblockedMove(Square.sq("a4"), Square.sq("d4")));
        assertFalse(board1.isUnblockedMove(Square.sq("a5"), Square.sq("i5")));
        assertFalse(board1.isUnblockedMove(Square.sq("a4"), Square.sq("h4")));
        assertFalse(board1.isUnblockedMove(Square.sq("d9"), Square.sq("e9")));
        assertTrue(board1.isUnblockedMove(Square.sq("f9"), Square.sq("f8")));
        assertTrue(board1.isUnblockedMove(Square.sq("f9"), Square.sq("g9")));
    }
    @Test
    public void testislegal() {
        Board board1 = new Board();
        board1.init();
        assertTrue(board1.isLegal(Square.sq("a4"), Square.sq("c4")));
    }
    @Test
    public void testmakemove() {
        Board board1 = new Board();
        board1.init();
        board1.makeMove(Square.sq("h5"), Square.sq("h9"));
        board1.makeMove(Square.sq("e6"), Square.sq("b6"));
        board1.makeMove(Square.sq("i6"), Square.sq("e6"));
        board1.makeMove(Square.sq("f5"), Square.sq("f2"));
        board1.makeMove(Square.sq("i4"), Square.sq("f4"));
        board1.makeMove(Square.sq("g5"), Square.sq("g9"));
        board1.makeMove(Square.sq("e6"), Square.sq("f6"));
        board1.makeMove(Square.sq("e5"), Square.sq("f5"));
        board1.makeMove(Square.sq("i5"), Square.sq("g5"));
    }

}




