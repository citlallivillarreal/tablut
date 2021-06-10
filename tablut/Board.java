package tablut;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Formatter;
import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Citlalli Villarreal
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        for (int i = 0; i < SIZE; i += 1) {
            for (int j = 0; j < SIZE; j += 1) {
                _board[i][j] = model.get(i, j);
            }
        }
        Deque<Square> copystacksq = new ArrayDeque<Square>();
        Deque<Piece> copystackpiece = new ArrayDeque<Piece>();
        Deque<Integer> copystackmvcount = new ArrayDeque<Integer>();
        Deque<Square> copystacksqcap = new ArrayDeque<Square>();
        Deque<Piece> copystackpiececap = new ArrayDeque<Piece>();
        Deque<Integer> copystackmvcountcap = new ArrayDeque<Integer>();
        copystacksq.addAll(_stacksq);
        copystackpiece.addAll(_stackpiece);
        copystackmvcount.addAll(_stackmvcount);
        copystacksqcap.addAll(_stacksqcap);
        copystackpiececap.addAll(_stackpiececap);
        copystackmvcountcap.addAll(_stackmvcountcap);
        _turn = model._turn; _winner = model._winner;
        _repeated = model._repeated; _moveCount = model._moveCount;
        _moveLimit = model._moveLimit; _stacksq = copystacksq;
        _stackpiece = copystackpiece; _stackmvcount = copystackmvcount;
        _stacksqcap = copystacksqcap; _stackpiececap = copystackpiececap;
        _stackmvcountcap = copystackmvcountcap;
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = BLACK;
        _winner = null;
        _repeated = false;
        _moveCount = 0;
        _moveLimit = Integer.MAX_VALUE;
        _board = new Piece [SIZE][SIZE];
        _states = new ArrayList<String>();
        _stacksq = new ArrayDeque<Square>();
        _stackpiece = new ArrayDeque<Piece>();
        _stackmvcount = new ArrayDeque<Integer>();
        _stacksqcap = new ArrayDeque<Square>();
        _stackpiececap = new ArrayDeque<Piece>();
        _stackmvcountcap = new ArrayDeque<Integer>();
        for (int i = 0; i < SIZE; i += 1) {
            for (int j = 0; j < SIZE; j += 1) {
                _board[i][j] = EMPTY;
            }
        }
        for (int i = 0; i < INITIAL_DEFENDERS.length; i += 1) {
            Square currsq = INITIAL_DEFENDERS[i];
            _board[currsq.col()][currsq.row()] = WHITE;
        }
        for (int j = 0; j < INITIAL_ATTACKERS.length; j += 1) {
            Square currsq = INITIAL_ATTACKERS[j];
            _board[currsq.col()][currsq.row()] = BLACK;
        }
        _board[THRONE.col()][THRONE.row()] = KING;
        checkRepeated();
    }


    /** Set the move limit to N.  It is an error if 2*LIM <= moveCount(). */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException("Illegal move limit");
        } else {
            _moveLimit = n;
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String currstate = this.encodedBoard();
        if (_states.contains(currstate)) {
            _winner = _turn;
            _repeated = true;
        }
        _states.add(currstate);
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        Square kingsq = null;
        for (int i = 0; i < SIZE; i += 1) {
            for (int j = 0; j < SIZE; j += 1) {
                Piece currp = _board[i][j];
                if (currp.toString().equals("K")) {
                    kingsq = sq(i, j);
                }
            }
        }
        return kingsq;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        assert exists(s.col(), s.row());
        _board[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        put(p, s);
        _stacksq.push(s);
        _stackpiece.push(p);
        _stackmvcount.push(_moveCount);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        Boolean unblocked = true;
        if (!exists(from.col(), from.row()) && !exists(to.col(), to.row())) {
            unblocked = false;
            return unblocked;
        } else if (!isLegal(from)) {
            unblocked = false;
            return unblocked;
        } else if (!from.isRookMove(to)) {
            unblocked = false;
            return unblocked;
        } else {
            int fcol = from.col(); int frow = from.row();
            int tcol = to.col(); int trow = to.row();
            int distrow = frow - trow;
            int distcol = fcol - tcol;
            if (distcol != 0 && distrow == 0) {
                if (distcol > 0) {
                    while (distcol != 0) {
                        fcol = fcol - 1;
                        distcol = distcol - 1;
                        if (get(fcol, frow) != EMPTY) {
                            unblocked = false;
                            return unblocked;
                        }
                    }
                } else {
                    while (distcol != 0)  {
                        fcol = fcol + 1;
                        distcol = distcol + 1;
                        if (get(fcol, frow) != EMPTY) {
                            unblocked = false;
                            return unblocked;
                        }
                    }
                }
            } else if (distcol == 0 && distrow != 0) {
                if (distrow > 0) {
                    while (distrow != 0) {
                        frow = frow - 1;
                        distrow = distrow - 1;
                        if (get(fcol, frow) != EMPTY) {
                            unblocked = false;
                            return unblocked;
                        }
                    }
                } else {
                    while (distrow != 0) {
                        frow = frow + 1;
                        distrow = distrow + 1;
                        if (get(fcol, frow) != EMPTY) {
                            unblocked = false;
                            return unblocked;
                        }
                    }
                }
            }
        }
        return unblocked;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        boolean legal = false;
        if (isUnblockedMove(from, to)) {
            legal = true;
        }
        if (get(from.col(), from.row()) != KING && to == THRONE) {
            legal = false;
        }
        return legal;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        revPut(get(from), to); revPut(EMPTY, from);
        _moveCount = _moveCount + 1;
        if (to == kingPosition() && to.isEdge()) {
            _winner = WHITE;
        }
        Square sqN = to.rookMove(0, 2); Square sqE = to.rookMove(1, 2);
        Square sqS = to.rookMove(2, 2); Square sqW = to.rookMove(3, 2);
        if (sqN != null && (get(sqN) == _turn || (get(sqN) == KING
                && get(sqN) == _turn))) {
            capture(to, sqN);
        }
        if (sqE != null && (get(sqE) == _turn || (get(sqE) == KING
                && get(sqE) == _turn))) {
            capture(to, sqE);
        }
        if (sqS != null && (get(sqS) == _turn || (get(sqS) == KING
                && get(sqS) == _turn))) {
            capture(to, sqS);
        }
        if (sqW != null && (get(sqW) == _turn || (get(sqW) == KING
                && get(sqW) == _turn))) {
            capture(to, sqW);
        }
        if (sqN == THRONE || sqE == THRONE || sqS == THRONE || sqW == THRONE) {
            if (get(THRONE) == EMPTY) {
                capture(to, THRONE);
            } else {
                if (WHITE == _turn) {
                    capture(to, THRONE);
                } else if (BLACK == _turn) {
                    if (get(NTHRONE) != BLACK && get(STHRONE) == BLACK
                            && get(WTHRONE) == BLACK &&  get(ETHRONE) == BLACK
                            || get(NTHRONE) == BLACK && get(STHRONE) != BLACK
                            && get(WTHRONE) == BLACK &&  get(ETHRONE) == BLACK
                            || get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                            && get(WTHRONE) != BLACK &&  get(ETHRONE) == BLACK
                            || get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                            && get(WTHRONE) == BLACK && get(ETHRONE) != BLACK) {
                        capture(to, THRONE);
                    }
                }
            }
        }
        _turn = _turn.opponent(); checkRepeated();
        if (_moveCount == _moveLimit) {
            _winner = WHITE;
        }
    }
    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square btw = sq0.between(sq2);
        if (get(btw) != KING && get(btw) != EMPTY && get(btw) != _turn) {
            _stacksqcap.push(btw);
            _stackpiececap.push(get(btw));
            _stackmvcountcap.push(_moveCount);
            put(EMPTY, btw);
        } else if (btw == kingPosition() && kingPosition() != NTHRONE
                && kingPosition() != STHRONE
                && kingPosition() != WTHRONE && kingPosition() != ETHRONE
                && kingPosition() != THRONE) {
            _stacksqcap.push(btw);
            _stackpiececap.push(get(btw));
            _stackmvcountcap.push(_moveCount);
            put(EMPTY, btw);
            _winner = _turn;
        } else if (btw == kingPosition()) {
            Square sqN = kingPosition().rookMove(0, 1);
            Square sqE = kingPosition().rookMove(1, 1);
            Square sqS = kingPosition().rookMove(2, 1);
            Square sqW = kingPosition().rookMove(3, 1);
            if (kingPosition() == NTHRONE || kingPosition() == STHRONE
                || kingPosition() == WTHRONE || kingPosition() == ETHRONE
                    || kingPosition() == THRONE) {
                if (get(sqN) == _turn && get(sqW) == _turn
                        && get(sqE) == _turn && get(sqS) == _turn) {
                    _stacksqcap.push(btw);
                    _stackpiececap.push(get(btw));
                    _stackmvcountcap.push(_moveCount);
                    put(EMPTY, btw);
                    _winner = _turn;
                } else if (kingPosition() == NTHRONE
                        || kingPosition() == STHRONE
                    || kingPosition() == WTHRONE
                        || kingPosition() == ETHRONE) {
                    if (get(sqN) != _turn && get(sqW) == _turn
                            && get(sqE) == _turn && get(sqS) == _turn
                            || get(sqN) == _turn && get(sqW) != _turn
                            && get(sqE) == _turn && get(sqS) == _turn
                            || get(sqN) == _turn && get(sqW) == _turn
                            && get(sqE) != _turn && get(sqS) == _turn
                            || get(sqN) == _turn && get(sqW) == _turn
                            && get(sqE) == _turn && get(sqS) != _turn) {
                        _stacksqcap.push(btw);
                        _stackpiececap.push(get(btw));
                        _stackmvcountcap.push(_moveCount);
                        put(EMPTY, btw);
                        _winner = _turn;
                    }
                }
            }
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            int count = 0;
            int countcap = 0;
            if (_stackpiece.size() != 0 && _stacksq.size() != 0
                    && _stackmvcount.size() != 0) {
                Square currsq = _stacksq.pop();
                Piece currpiece = _stackpiece.pop();
                Square nextsq = _stacksq.pop();
                Piece nextpiece = _stackpiece.pop();
                count = _stackmvcount.pop();
                count = _stackmvcount.pop();
                put(nextpiece, currsq);
                put(currpiece, nextsq);
            }
            if (_stackmvcountcap.size() != 0) {
                countcap = _stackmvcountcap.pop();
                while (countcap >= count) {
                    if (_stacksqcap.size() != 0 && _stackpiececap.size() != 0) {
                        Square currsqcap = _stacksqcap.pop();
                        Piece currpcap = _stackpiececap.pop();
                        put(currpcap, currsqcap);
                    }
                    if (_stackmvcountcap.size() != 0) {
                        countcap = _stackmvcountcap.pop();
                    }
                }
            }
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (moveCount() > 1 || !_repeated) {
            String currstate = this.encodedBoard();
            for (int i = 0; i < _states.size(); i += 1) {
                String astate = _states.get(i);
                if (currstate.equals(astate)) {
                    _states.remove(i);
                    break;
                }
            }
        }
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        _stacksq = new ArrayDeque<Square>();
        _stackpiece = new ArrayDeque<Piece>();
        _stackmvcount = new ArrayDeque<Integer>();
        _stacksqcap = new ArrayDeque<Square>();
        _stackpiececap = new ArrayDeque<Piece>();
        _stackmvcountcap = new ArrayDeque<Integer>();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        boolean eq = true;
        if (_turn != side) {
            eq = false;
            _turn = side;
        }
        List<Move> moves = new ArrayList<Move>();
        HashSet<Square> sqloc = pieceLocations(side);
        Iterator<Square> sqiter = sqloc.iterator();
        while (sqiter.hasNext()) {
            Square currsqs = sqiter.next();
            for (int i = 0; i < SIZE; i += 1) {
                Square sqN = currsqs.rookMove(0, i);
                Square sqE = currsqs.rookMove(1, i);
                Square sqS = currsqs.rookMove(2, i);
                Square sqW = currsqs.rookMove(3, i);
                if (sqN != null && isLegal(currsqs, sqN)) {
                    Move currmv = mv(currsqs, sqN);
                    moves.add(currmv);
                }
                if (sqE != null && isLegal(currsqs, sqE)) {
                    Move currmv = mv(currsqs, sqE);
                    moves.add(currmv);
                }
                if (sqS != null && isLegal(currsqs, sqS)) {
                    Move currmv = mv(currsqs, sqS);
                    moves.add(currmv);
                }
                if (sqW != null && isLegal(currsqs, sqW)) {
                    Move currmv = mv(currsqs, sqW);
                    moves.add(currmv);
                }
            }
        }
        if (!eq) {
            _turn = side.opponent();
        }
        return moves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        boolean moves = false;
        if (legalMoves(side).size() > 0) {
            moves = true;
        }
        return moves;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> setploc = new HashSet<Square>();
        for (int i = 0; i < _board.length; i += 1) {
            for (int j = 0; j < _board.length; j += 1) {
                Piece currp = _board[i][j];
                if (currp == side || currp == KING) {
                    Square currsq = sq(i, j);
                    setploc.add(currsq);
                }
            }
        }
        return setploc;
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLoc(Piece side) {
        return pieceLocations(side);
    }


    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or null if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** A Piece 2D array representation of the board. */
    private Piece [][] _board;
    /** Integer representing the limit of moves in a game. */
    private int _moveLimit;
    /** A Stack of squares. */
    private Deque<Square> _stacksq;
    /** A Stack of pieces. */
    private Deque<Piece> _stackpiece;
    /** A Stack of number of (still undone) moves since initial position.. */
    private Deque<Integer> _stackmvcount;
    /** List of a string representation of the board and it's turn attribute. */
    private List<String> _states;
    /** A stack of squares where pieces were captured. */
    private Deque<Square> _stacksqcap;
    /** A stack of pieces that were captured. */
    private Deque<Piece> _stackpiececap;
    /** A stack of numbers representing the number of (still undone)
     *  moves engaging in a capture since initial position.
     */
    private Deque<Integer> _stackmvcountcap;
}
