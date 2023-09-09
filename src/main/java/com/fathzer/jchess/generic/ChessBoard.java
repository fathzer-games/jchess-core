package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.Piece.*;
import static com.fathzer.jchess.Direction.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.fathzer.games.Color;
import com.fathzer.games.UndoMoveManager;
import com.fathzer.games.HashProvider;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Castling.Side;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.fast.FastBoardRepresentation;

public abstract class ChessBoard implements Board<Move>, HashProvider {
	private final DirectionExplorer exp;
	private final BoardRepresentation board;
	private final MovesBuilder movesBuilder;
	private int[] kingPositions;
	private int enPassant;
	private int enPassantDeletePawnIndex;
	private Color activeColor;
	private int castlings;
	private int halfMoveCount;
	private int moveNumber;
	private long key;
	private List<Long> keyHistory;
	private UndoMoveManager<ChessBoardState> undoManager;
	private Function<Board<Move>, Comparator<Move>> moveComparatorBuilder;
	
	protected ChessBoard(List<PieceWithPosition> pieces) {
		this(Dimension.STANDARD, pieces);
	}

	protected ChessBoard(Dimension dimension, List<PieceWithPosition> pieces) {
		this(dimension, pieces,Color.WHITE, Castling.ALL, -1, 0, 1);
	}
	
	private void save(ChessBoardState state) {
		state.enPassant = this.enPassant;
		state.enPassantDeletePawnIndex = this.enPassantDeletePawnIndex;
		state.castlings = this.castlings;
		state.moveNumber = this.moveNumber;
		state.halfMoveCount = this.halfMoveCount;
		state.key = this.key;
		System.arraycopy(kingPositions, 0, state.kingPositions, 0, kingPositions.length);
		System.arraycopy(board.getPieces(), 0, state.cells, 0, state.cells.length);
	}
	
	private void restore(ChessBoardState state) {
		this.enPassant = state.enPassant;
		this.enPassantDeletePawnIndex = state.enPassantDeletePawnIndex;
		this.castlings = state.castlings;
		this.moveNumber = state.moveNumber;
		this.halfMoveCount = state.halfMoveCount;
		this.key = state.key;
		System.arraycopy(state.kingPositions, 0, kingPositions, 0, kingPositions.length);
		System.arraycopy(state.cells, 0, board.getPieces(), 0, state.cells.length);
	}

	/** Constructor.
	 * @param dimension The chess board dimension (8x8 for a standard game)
	 * @param pieces The pieces to place on the board with their positions
	 * @param activeColor The color that will make next move
	 * @param castlings A list of possible castlings 
	 * @param enPassantColumn The "en passant" column or a negative number if no "en passant" is possible.
	 * @param halfMoveCount This is the number of half moves since the last capture or pawn advance.
	 * @param moveNumber The move number (1 at the beginning of the game)
	 */
	protected ChessBoard(Dimension dimension, List<PieceWithPosition> pieces, Color activeColor, Collection<Castling> castlings, int enPassantColumn, int halfMoveCount, int moveNumber) {
		if (activeColor==null) {
			throw new NullPointerException();
		}
		this.board = new FastBoardRepresentation(dimension, pieces);
		this.undoManager = new UndoMoveManager<>(() -> new ChessBoardState(this.board.getPieces().length), this::save, this::restore);
		this.exp = getDirectionExplorer(-1);
		this.activeColor = activeColor;
		this.castlings = castlings==null ? 0 : Castling.toInt(castlings);
		this.enPassant = -1;
		if (enPassantColumn>=0) {
			final int enPassantRow = Color.WHITE==activeColor ? 2 : board.getDimension().getHeight()-3;
			final CoordinatesSystem cs = board.getCoordinatesSystem();
			final int enPassantIndex = cs.getIndex(enPassantRow, enPassantColumn);
			if (board.getPiece(enPassantIndex)!=null) {
				throw new IllegalArgumentException("EnPassant cell is not empty");
			}
			final int pawnCell = WHITE==activeColor ? cs.nextRow(enPassantIndex) : cs.previousRow(enPassantIndex);
			final Piece expected = WHITE==activeColor?BLACK_PAWN:WHITE_PAWN;
			if (expected != board.getPiece(pawnCell)) {
				throw new IllegalArgumentException("Attacked enPassant pawn is missing");
			}
			setEnPassant(enPassantIndex, activeColor, pawnCell);
		}
		this.kingPositions = new int[2];
		for (PieceWithPosition p : pieces) {
			if (PieceKind.KING.equals(p.getPiece().getKind())) {
				final int dest = board.getCoordinatesSystem().getIndex(p.getRow(), p.getColumn());
				this.kingPositions[p.getPiece().getColor().ordinal()] = dest;
			}
		}
		if (halfMoveCount<0) {
			throw new IllegalArgumentException("Half move count can't be negative");
		}
		this.halfMoveCount = halfMoveCount;
		if (moveNumber<1) {
			throw new IllegalArgumentException("Move number should be strictly positive");
		}
		this.moveNumber = moveNumber;
		this.key = board.getZobrist().get(this);
		this.keyHistory = new ArrayList<>();
		this.movesBuilder = buildMovesBuilder();
	}
	
	protected abstract MovesBuilder buildMovesBuilder();
	
	@Override
	public Dimension getDimension() {
		return board.getDimension();
	}
	
	@Override
	public CoordinatesSystem getCoordinatesSystem() {
		return board.getCoordinatesSystem();
	}
	
	@Override
	public BoardExplorer getExplorer() {
		return board.getExplorer();
	}

	@Override
	public DirectionExplorer getDirectionExplorer(int index) {
		return board.getDirectionExplorer(index);
	}
	
	@Override
	public List<Move> getMoves() {
		return movesBuilder.getMoves();
	}

	@Override
	public Status getStatus() {
		return movesBuilder.getStatus();
	}

	/** Makes a move.
	 * <br>WARNING, this method does not verify the move is valid.
	 * @param move a Move.
	 * @throws IllegalArgumentException if there's no piece at move.getFrom().
	 */
	@Override
	public boolean makeMove(Move move) {
		final int from = move.getFrom();
		int to = move.getTo();
		Piece movedPiece = board.getPiece(from);
		if (movedPiece==null) {
			throw new IllegalArgumentException("No piece at "+from);
		}
		keyHistory.add(key);
		this.undoManager.beforeMove();
		Castling castling = null;
		if (PieceKind.PAWN.equals(movedPiece.getKind())) {
			pawnMove(from, to, move.getPromotion());
		} else {
			this.clearEnPassant();
			if (PieceKind.KING.equals(movedPiece.getKind())) {
				castling = onKingMove(from, to);
			} else if (castlings!=0 && PieceKind.ROOK.equals(movedPiece.getKind())) {
				// Erase castling if needed when rook moves
				onRookEvent(from);
			}
		}
		final boolean incHalfCount;
		if (castling==null) {
			final Piece erasedPiece = board.getPiece(to);
			if (erasedPiece!=null && castlings!=0 && PieceKind.ROOK.equals(erasedPiece.getKind())) {
				// Erase castling if needed when rook is captured
				onRookEvent(to);
			}
			move(from, to, false);
			incHalfCount = erasedPiece==null && !PieceKind.PAWN.equals(movedPiece.getKind());
		} else {
			incHalfCount = true;
		}
		
		if (incHalfCount) {
			halfMoveCount++;
		} else {
			halfMoveCount = 0;
		}
		if (Color.BLACK.equals(activeColor)) {
			moveNumber++;
		}
		activeColor = movedPiece.getColor().opposite();
		key ^= board.getZobrist().getTurnKey();
		movesBuilder.clear();
		return true;
	}
	
	@Override
	public void unmakeMove() {
		this.keyHistory.remove(keyHistory.size()-1);
		this.undoManager.undo();
		this.activeColor = this.activeColor.opposite();
		this.movesBuilder.clear();
	}
	
	void saveCells() {
		board.save();
	}
	
	int moveOnlyCells (int from, int to) {
		final Piece p = board.getPiece(from);
		final Color playingColor = p.getColor();
		if (PieceKind.KING.equals(p.getKind())) {
			return fastKingMove(from, to);
		}
		if (PieceKind.PAWN.equals(p.getKind())) {
			fastPawnMove(to, playingColor);
		}
		move(from,to, true);
		return getKingPosition(playingColor);
	}

	private int fastKingMove(int from, int to) {
		final Castling castling = getCastling(from, to);
		if (castling!=null) {
			// Castling => Get the correct king's destination
			to = getKingDestination(castling); 
			// Move the rook too
			final int rookDest = to + castling.getSide().getRookOffset();
			final int initialRookPosition = getInitialRookPosition(castling);
			movePieces(from, to, initialRookPosition, rookDest, true);
		} else {
			move(from,to, true);
		}
		return to;
	}

	void restoreCells() {
		board.restore();
	}
	private Castling onKingMove(int from, int to) {
		final Castling castling = getCastling(from, to);
		if (castling!=null) {
			// Castling => Get the correct king's destination
			to = getKingDestination(castling); 
			// Move the rook too
			final int rookDest = to + castling.getSide().getRookOffset();
			final int initialRookPosition = getInitialRookPosition(castling);
			movePieces(from, to, initialRookPosition, rookDest, false);
		}
		final boolean whitePlaying = WHITE.equals(activeColor);
		eraseCastlings(whitePlaying ? Castling.WHITE_KING_SIDE : Castling.BLACK_KING_SIDE, 
				whitePlaying ? Castling.WHITE_QUEEN_SIDE : Castling.BLACK_QUEEN_SIDE);
		kingPositions[activeColor.ordinal()] = to;
		return castling;
	}
	
	private void movePieces(int from1, int to1, int from2, int to2, boolean cellsOnly) {
		final Piece moved1 = board.getPiece(from1);
		final Piece moved2 = board.getPiece(from2);
		if (!cellsOnly) {
			// Update zobristKey
			if (board.getPiece(to1)!=null) {
				key ^= board.getZobrist().getKey(to1, board.getPiece(to1));
			}
			if (board.getPiece(to2)!=null) {
				key ^= board.getZobrist().getKey(to2, board.getPiece(to2));
			}
			key ^= board.getZobrist().getKey(from1, moved1);
			key ^= board.getZobrist().getKey(to1, moved1);
			key ^= board.getZobrist().getKey(from2, moved2);
			key ^= board.getZobrist().getKey(to2, moved2);
		}
		board.setPiece(from1, null);
		board.setPiece(from2, null);
		board.setPiece(to1, moved1);
		board.setPiece(to2, moved2);
	}
	
	private void move(int from, int to, boolean cellsOnly) {
		final Piece moved = board.getPiece(from);
		if (!cellsOnly) {
			// Update zobristKey
			final Piece erased = board.getPiece(to);
			if (erased!=null) {
				key ^= board.getZobrist().getKey(to, erased);
			}
			key ^= board.getZobrist().getKey(from, moved);
			key ^= board.getZobrist().getKey(to, moved);
		}
		board.setPiece(to, moved);
		board.setPiece(from, null);
	}
	
	private void fastPawnMove(int to, Color playingColor) {
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			board.setPiece(enPassantDeletePawnIndex, null);
		}
	}
	
	/** Called when a rook moves or is captured.
	 * <br>It allows the board to erase the corresponding castling
	 * @param cell The cell that contains the rook.
	 */
	private void onRookEvent(int cell) {
		for (Castling castling : Castling.ALL) {
			if (cell==getInitialRookPosition(castling)) {
				eraseCastlings(castling);
				break;
			}
		}
	}
	
	/** {@inheritDoc}
	 * <br>This generic implementation returns the corner that contains the rook in standard chess
	 */
	@Override
	public int getInitialRookPosition(Castling castling) { //TODO Possible to optimize using same structure as in chess960
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		if (Castling.BLACK_QUEEN_SIDE.equals(castling)) {
			return cs.getIndex(0, 0);
		} else if (Castling.BLACK_KING_SIDE.equals(castling)) {
			return cs.getIndex(0,board.getDimension().getWidth()-1);
		} else if (Castling.WHITE_KING_SIDE.equals(castling)) {
			return cs.getIndex(board.getDimension().getHeight()-1,board.getDimension().getWidth()-1);
		} else if (Castling.WHITE_QUEEN_SIDE.equals(castling)) {
			return cs.getIndex(board.getDimension().getHeight()-1,0);
		} else {
			return -1;
		}
	}
	
	@Override
	public int getKingDestination(Castling castling) {
		final int row = Color.WHITE==castling.getColor() ? board.getDimension().getHeight()-1 : 0;
		final int column = Side.QUEEN==castling.getSide() ? 2 : board.getDimension().getWidth()-2;
		return board.getCoordinatesSystem().getIndex(row, column);
	}

	private void pawnMove(int from, int to, Piece promotion) {
		final Piece pawn = board.getPiece(from);
		if (promotion!=null) {
			board.setPiece(from, promotion);
			// Promotion => update key as if pawn is replaced by the promotion
			key ^= board.getZobrist().getKey(from, promotion);
			key ^= board.getZobrist().getKey(from, pawn);
		}
		if (enPassant>=0) {
			if (to==enPassant) {
				// en-passant catch => delete adverse's pawn
				final int pos = enPassantDeletePawnIndex;
				key ^= board.getZobrist().getKey(pos, board.getPiece(pos));
				board.setPiece(pos, null);
			}
			// Clear the en-passant key
			key ^= board.getZobrist().getKey(enPassant);
			enPassant = -1;
		}
		
		final int rowOffset = board.getCoordinatesSystem().getRow(to) - board.getCoordinatesSystem().getRow(from);
		if (Math.abs(rowOffset)==2) {
			// Make en-passant available for opponent
			final boolean whiteMove = WHITE == pawn.getColor();
			setEnPassant(whiteMove ? board.getCoordinatesSystem().nextRow(to) : board.getCoordinatesSystem().previousRow(to), pawn.getColor().opposite(), to);
		}
	}
	
	/** Erases castlings.
	 * @param castlings The castling to be erased.
	 */
	private void eraseCastlings(Castling... castlings) {
		for (Castling castling : castlings) {
			if ((this.castlings & castling.getMask()) != 0) {
				key ^= board.getZobrist().getKey(castling);
				this.castlings -= castling.getMask();
			}
		}
	}
	
	private void setEnPassant(int pos, Color catchingColor, int deletedPawn) {
		if (isCatcheableEnPassant(pos, catchingColor)) {
			this.enPassant = pos;
			enPassantDeletePawnIndex = deletedPawn;
			key ^= board.getZobrist().getKey(pos);
		}
	}
	
	private boolean isCatcheableEnPassant(int pos, Color catchingColor) {
		exp.reset(pos);
		if (Color.WHITE==catchingColor) {
			return isCatcheableEnPassant(exp, SOUTH_EAST, WHITE_PAWN) || isCatcheableEnPassant(exp, SOUTH_WEST, WHITE_PAWN);
		} else {
			return isCatcheableEnPassant(exp, NORTH_EAST, BLACK_PAWN) || isCatcheableEnPassant(exp, NORTH_WEST, BLACK_PAWN);
		}
	}

	private boolean isCatcheableEnPassant(final DirectionExplorer exp, final Direction direction, Piece catchingPawn) {
		exp.start(direction);
		return exp.next() && catchingPawn==exp.getPiece();
	}
	
	private void clearEnPassant() {
		if (enPassant>=0) {
			key ^= board.getZobrist().getKey(enPassant);
			this.enPassant = -1;
		}
	}

	@Override
	public String toString() {
		return board.toString();
	}
	
	@Override
	public Piece getPiece(int position) {
		return board.getPiece(position);
	}
	
	@Override
	public boolean hasCastling(Castling c) {
		return (c.getMask() & this.castlings) != 0;
	}
	
	/** Copy another board in this.
	 * @param other The other board
	 */
	@Override
	public void copy(Board<Move> other) {
		if (!getDimension().equals(other.getDimension())) {
			throw new IllegalArgumentException("Can't copy board with different dimension");
		}
		if (other instanceof ChessBoard) {
			this.activeColor = other.getActiveColor();
			this.enPassant = other.getEnPassant();
			this.enPassantDeletePawnIndex = ((ChessBoard)other).enPassantDeletePawnIndex;
			this.halfMoveCount = other.getHalfMoveCount();
			this.moveNumber = other.getMoveNumber();
			this.castlings = ((ChessBoard)other).castlings;
			this.board.copy(((ChessBoard)other).board);
			this.key = other.getHashKey();
			this.keyHistory.clear();
			this.keyHistory.addAll(((ChessBoard)other).keyHistory);
			System.arraycopy(((ChessBoard)other).kingPositions, 0, kingPositions, 0, kingPositions.length);
			this.buildMovesBuilder();
			this.setMoveComparatorBuilder(other.getMoveComparatorBuilder());
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public int getKingPosition(Color color) {
		return kingPositions[color.ordinal()];
	}
	
	@Override
	public Color getActiveColor() {
		return activeColor;
	}

	@Override
	public int getEnPassant() {
		return this.enPassant;
	}
	
	@Override
	public long getHashKey() {
		return key;
	}
	
	protected List<Long> getKeyHistory() {
		return this.keyHistory;
	}
	
	@Override
	public int getHalfMoveCount() {
		return halfMoveCount;
	}
	
	@Override
	public int getMoveNumber() {
		return moveNumber;
	}
	
	@Override
	public boolean isCheck() {
		Color color = getActiveColor();
		return new AttackDetector(board.getDirectionExplorer(-1)).isAttacked(getKingPosition(color), color.opposite());
	}

	BoardRepresentation getBoard() {
		return board;
	}

	@Override
	public Function<Board<Move>, Comparator<Move>> getMoveComparatorBuilder() {
		return moveComparatorBuilder;
	}

	@Override
	public void setMoveComparatorBuilder(Function<Board<Move>, Comparator<Move>> moveComparatorBuilder) {
		this.moveComparatorBuilder = moveComparatorBuilder;
		if (moveComparatorBuilder==null) {
			movesBuilder.setMoveComparator(null);
		} else {
			movesBuilder.setMoveComparator(moveComparatorBuilder.apply(this));
		}
	}
}