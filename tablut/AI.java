package tablut;

import java.util.HashSet;
import java.util.List;

import static java.lang.Math.*;

import static tablut.Board.*;
import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Citlalli Villarreal
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        findMove();
        Move move = _lastFoundMove;
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;

        if (b.turn() == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        }
        if (b.turn() == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }

        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        Board btest = new Board(board);
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            Move bestmovesofar = null; int bestvalsofar = -INFTY;
            List<Move> lgmoves = btest.legalMoves(WHITE);
            for (Move m : lgmoves) {
                btest = new Board(board);
                btest.makeMove(m);
                if (btest.kingPosition() != null
                        && btest.kingPosition().isEdge()) {
                    _lastFoundMove = m;
                    return WINNING_VALUE;
                }
                int score = findMove(btest, depth - 1,
                        saveMove, -1, alpha, beta);
                if (score > bestvalsofar) {
                    bestmovesofar = m; bestvalsofar = score;
                    alpha = max(alpha, bestvalsofar);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = bestmovesofar;
            }
            return bestvalsofar;
        }
        if (sense == -1) {
            Move bestmovesofar = null; int bestvalsofar = INFTY;
            List<Move> lgmoves = btest.legalMoves(BLACK);
            for (Move m : lgmoves) {
                btest = new Board(board);
                btest.makeMove(m);
                if (btest.kingPosition() == null) {
                    _lastFoundMove = m;
                    return -WINNING_VALUE;
                }
                int score = findMove(btest, depth - 1,
                        saveMove, 1, alpha, beta);
                if (score < bestvalsofar) {
                    bestmovesofar = m; bestvalsofar = score;
                    beta = min(beta, bestvalsofar);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = bestmovesofar;
            }
            return bestvalsofar;
        }
        return 0;
    }


    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        return 4;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        int whitewins = 0;
        int vlcapkingthrone = 0;
        int vlcapkingnonthrone = 0;
        int valuekingtoedge = 0;
        int valueatedge = 0;
        int valuefromedge = 0;
        int valueb = 0;
        int valuew = 0;

        if (board.kingPosition() != null) {
            if (board.kingPosition().isEdge()) {
                valueatedge = WINNING_VALUE;
            }
        }

        if (board.kingPosition() != null) {
            if (!board.kingPosition().isEdge()) {
                Square currkg = board.kingPosition();
                int distrowed8 = (SIZE - 1) - currkg.row();
                int distcoled8 = (SIZE - 1) - currkg.col();
                int distrowed0 = currkg.row();
                int distcoled0 = currkg.col();
                int minrowed = min(distrowed0, distrowed8);
                int mincoled = min(distcoled0, distcoled8);
                int minrowcoled = min(minrowed, mincoled);
                int valuek = NUMBER - minrowcoled;
                valuefromedge = valuek;
            }
        }
        HashSet<Square> hashsetsqsB = board.pieceLoc(BLACK);
        int bpieces = hashsetsqsB.size();
        int valuebp = -bpieces;
        valueb = valuebp;


        HashSet<Square> hashsetsqsW = board.pieceLoc(WHITE);
        int wpieces = hashsetsqsW.size();
        int valuewp = wpieces;
        valuew = valuewp;

        return valuewp + valuebp + valuefromedge;
    }
    /**Constant for maximum King at edge score. */
    static final int NUMBER = 50;
}
