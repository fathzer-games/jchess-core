package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.Piece.*;
import static com.fathzer.jchess.PieceKind.*;
import static com.fathzer.jchess.Direction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.fathzer.games.Color;
import com.fathzer.games.util.SelectiveComparator;
import com.fathzer.games.util.Stack;
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
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.fast.FastBoardRepresentation;

public abstract class ChessBoard implements Board<Move> {
	private final BoardRepresentation board;
	private final int[] kingPositions;
	private final InsufficientMaterialDetector insufficientMaterialDetector;
	private final MovesBuilder movesBuilder;
	private final DirectionExplorer exp;
	private final AttackDetector attackDetector;
	private int enPassant;
	private int enPassantDeletePawnIndex;
	private Color activeColor;
	private int castlings;
	private int halfMoveCount;
	private int moveNumber;
	private long key;
	private PinnedDetector pinnedDetector;
	private final List<Long> keyHistory;
	private final Stack<ChessBoardState> undoData;
	private Function<Board<Move>, SelectiveComparator<Move>> moveComparatorBuilder;
	
	/**
	 * Constructor of an empty board.
	 * <br>It is intended to be used by subclasses that override {@link #create()}.
	 * @param dimension The chess board dimension.
	 */
	protected ChessBoard(Dimension dimension) {
		this.board = new FastBoardRepresentation(dimension);
		this.kingPositions = new int[2];
		this.exp = getDirectionExplorer(-1);
		this.movesBuilder = buildMovesBuilder();
		this.attackDetector = new AttackDetector(board.getDirectionExplorer(-1));
		this.keyHistory = new ArrayList<>();
		this.undoData = new Stack<>(() -> new ChessBoardState(this));
		this.insufficientMaterialDetector = new InsufficientMaterialDetector();
		this.pinnedDetector = new PinnedDetector(this);
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
		this(dimension);
		if (activeColor==null) {
			throw new NullPointerException();
		}
		this.board.fill(pieces);
		this.activeColor = activeColor;
		this.castlings = castlings==null ? 0 : Castling.toInt(castlings);
		this.enPassant = -1;
		if (enPassantColumn>=0) {
			checkedSetEnPassant(activeColor, enPassantColumn);
		}
		Arrays.fill(kingPositions, -1);

		for (PieceWithPosition p : pieces) {
			if (KING==p.getPiece().getKind()) {
				final int index = p.getPiece().getColor().ordinal();
				if (this.kingPositions[index]>=0) {
					throw new IllegalArgumentException("More than one "+p.getPiece().getColor()+"king");
				}
				final int dest = board.getCoordinatesSystem().getIndex(p.getRow(), p.getColumn());
				this.kingPositions[index] = dest;
			} else {
				insufficientMaterialDetector.add(p.getPiece());
			}
		}
		for (Color color : Color.values()) {
			if (this.kingPositions[color.ordinal()]<0) {
				throw new IllegalArgumentException("No "+color+" king");
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
		this.pinnedDetector = new PinnedDetector(this);
	}

	private void checkedSetEnPassant(Color activeColor, int enPassantColumn) {
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

	protected void checkCastling(Castling castling) {
		final CoordinatesSystem cs = getCoordinatesSystem();
		// Check there's a rook at its initial position
		final int initialRookPosition = getInitialRookPosition(castling);
		final Piece expectedPiece = castling.getColor()==WHITE ? WHITE_ROOK : BLACK_ROOK;
		final Piece rook = getPiece(initialRookPosition);
		if (expectedPiece!=rook) {
			throw new IllegalArgumentException(String.format("Piece at rook's initial position of %s is not a rook",castling));
		}
		// Check the king is at its starting row
		final int kingPosition = getKingPosition(castling.getColor());
		if (cs.getRow(kingPosition)!=(castling.getColor()==BLACK ? 0 : board.getDimension().getHeight()-1)) {
			throw new IllegalArgumentException(String.format("King of color %s is not at its starting row",castling.getColor()));
		}
		// Check the rook is at the right side of the king
		if (castling.getSide()==Side.QUEEN) {
			if (cs.getColumn(initialRookPosition)>=cs.getColumn(kingPosition)) {
				throw new IllegalArgumentException(String.format("Rook of %s is not at the left side of the king",castling));
			}
		} else {
			if (cs.getColumn(initialRookPosition)<=cs.getColumn(kingPosition)) {
				throw new IllegalArgumentException(String.format("Rook of %s is not at the right side of the king",castling));
			}
		}
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
		return movesBuilder.getPseudoLegalMoves();
	}
	
	@Override
	public List<Move> getLegalMoves() {
		return movesBuilder.getLegalMoves();
	}

	@Override
	public Status getStatus() {
		return movesBuilder.getStatus();
	}

	/** Makes a move.
	 * @param move a Move.
	 * @param confidence The confidence we can have in the move.
	 * <br>WARNING, this method does not verify the move is valid if confidence is not {@link MoveConfidence#UNSAFE}.
	 * <br>In such a case, passing a invalid move may have unpredictable results. It can leave the board in an inconsistent state or throw an exception.
	 */
	@Override
	public boolean makeMove(Move move, MoveConfidence confidence) {
		if (confidence==MoveConfidence.UNSAFE && !movesBuilder.isLegal(move)) {
			return false;
		}
		final ChessBoardState backup = undoData.get();
		this.save(backup);
		final MoveHelperHolder mhh = backup.moveHelperHolder;
		final int from = move.getFrom();
		final int to = move.getTo();
		final Piece movedPiece = board.getPiece(from);
		if (PAWN==movedPiece.getKind()) {
			pawnMove(from, to, move.getPromotion(), mhh);
		} else {
			if (KING==movedPiece.getKind()) {
				if (onKingMove(movedPiece, from, to, mhh, confidence==MoveConfidence.PSEUDO_LEGAL)) {
					return false;
				} else {
					this.clearEnPassant();
				}
			} else {
				this.clearEnPassant();
				mhh.setSimple(movedPiece, from, getPiece(to), to);
				if (castlings!=0 && ROOK==movedPiece.getKind()) {
					// Erase castling if needed when rook moves
					onRookEvent(from);
				}
			}
		}
		final MoveHelper helper = mhh.get();
		if (!helper.isCastling()) {
			final Piece captured = helper.getCaptured();
			if (castlings!=0 && captured!=null && ROOK==captured.getKind()) {
				// Erase castling if needed when rook is captured
				onRookEvent(to);
			}
			move(from, to, false);
			if (captured!=null) {
				insufficientMaterialDetector.remove(captured);
			}
		}
		if (confidence==MoveConfidence.PSEUDO_LEGAL && helper.isKingSafetyTestRequired() &&
				attackDetector.isAttacked(getKingPosition(activeColor), activeColor.opposite())) {
			this.enPassant = backup.enPassant;
			this.enPassantDeletePawnIndex = backup.enPassantDeletePawnIndex;
			this.castlings = backup.castlings;
			this.insufficientMaterialDetector.copy(backup.insufficientMaterialDetector);
			this.key = backup.key;
			helper.unmakePieces(board.pieces);
			helper.unmakeKingPosition(kingPositions, activeColor.ordinal());
			return false;
		}
		
		if (helper.shouldIncHalfMoveCount()) {
			halfMoveCount++;
		} else {
			halfMoveCount = 0;
		}
		if (Color.BLACK.equals(activeColor)) {
			moveNumber++;
		}
		activeColor = movedPiece.getColor().opposite();
		
		key ^= board.getZobrist().getTurnKey();
		keyHistory.add(backup.key);
		undoData.next();
		final ChessBoardState stateAfterMove = undoData.get();
		pinnedDetector = stateAfterMove.pinnedDetector;
		pinnedDetector.invalidate();
		movesBuilder.restoreFrom(stateAfterMove);
		movesBuilder.invalidate();
		return true;
	}
	
	private void save(ChessBoardState state) {
		state.enPassant = this.enPassant;
		state.enPassantDeletePawnIndex = this.enPassantDeletePawnIndex;
		state.castlings = this.castlings;
		state.moveNumber = this.moveNumber;
		state.halfMoveCount = this.halfMoveCount;
		state.insufficientMaterialDetector.copy(this.insufficientMaterialDetector);
		state.pinnedDetector = pinnedDetector;
		state.key = this.key;
		this.movesBuilder.saveTo(state);
	}
	
	@Override
	public void unmakeMove() {
		this.keyHistory.remove(keyHistory.size()-1);
		undoData.previous();
		final ChessBoardState state = undoData.get();
		this.restore(state);
		this.activeColor = this.activeColor.opposite();
		final MoveHelperHolder holder = state.moveHelperHolder;
		final MoveHelper helper = holder.get();
		helper.unmakePieces(getBoard().pieces);
		helper.unmakeKingPosition(kingPositions, activeColor.ordinal());
	}
	
	private void restore(ChessBoardState state) {
		this.enPassant = state.enPassant;
		this.enPassantDeletePawnIndex = state.enPassantDeletePawnIndex;
		this.castlings = state.castlings;
		this.insufficientMaterialDetector.copy(state.insufficientMaterialDetector);
		this.key = state.key;
		this.moveNumber = state.moveNumber;
		this.halfMoveCount = state.halfMoveCount;
		this.pinnedDetector = state.pinnedDetector;
		this.movesBuilder.restoreFrom(state); 
	}

	private int moveOnlyCells (int from, int to) {
		final MoveHelperHolder mhh = undoData.get().moveHelperHolder;
		undoData.next();
		final Piece p = board.getPiece(from);
		final Color playingColor = p.getColor();
		if (KING==p.getKind()) {
			return fastKingMove(from, to, mhh);
		}
		if (PAWN==p.getKind()) {
			if (to==enPassant) {
				// en-passant catch => delete adverse's pawn
				board.setPiece(enPassantDeletePawnIndex, null);
				mhh.setEnPassant(p, from, to, enPassantDeletePawnIndex);
			} else {
				mhh.setSimple(p, from, getPiece(to), to);
			}
		} else {
			mhh.setSimple(p, from, getPiece(to), to);
		}
		move(from, to, true);
		return getKingPosition(playingColor);
	}

	private int fastKingMove(int from, int to, MoveHelperHolder mhh) {
		final Castling castling = getCastling(from, to);
		if (castling!=null) {
			// Castling => Get the correct king's destination
			to = getKingDestination(castling); 
			// Move the rook too
			final int rookDest = to + castling.getSide().getRookOffset();
			final int initialRookPosition = getInitialRookPosition(castling);
			castlePieces(from, to, initialRookPosition, rookDest, true);
			mhh.setCastling(castling, from, to, initialRookPosition, rookDest);
		} else {
			mhh.setSimple(getPiece(from), from, getPiece(to), to);
			move(from,to, true);
		}
		return to;
	}

	private boolean onKingMove(Piece movedPiece, int from, int to, MoveHelperHolder mhh, boolean checkIsValid) {
		final Castling castling = getCastling(from, to);
		if (castling!=null) {
			// Castling => Get the correct king's destination
			to = getKingDestination(castling); 
			final int initialRookPosition = getInitialRookPosition(castling);
			if (checkIsValid && !movesBuilder.areCastlingCellsSafe(activeColor.opposite(), from, to, initialRookPosition)) {
				return true;
			}
			// Move the rook too
			final int rookDest = to + castling.getSide().getRookOffset();
			mhh.setCastling(castling, from, to, initialRookPosition, rookDest);
			castlePieces(from, to, initialRookPosition, rookDest, false);
		} else if (checkIsValid && attackDetector.isAttacked(to, activeColor.opposite())) {
			return true;
		} else {
			mhh.setSimple(movedPiece, from, getPiece(to), to);
		}
		eraseActiveColorCastlings();
		kingPositions[activeColor.ordinal()] = to;
		return false;
	}
	
	private void eraseActiveColorCastlings() {
		if (castlings!=0) {
			if (WHITE==activeColor) {
				clearCastling(Castling.WHITE_KING_SIDE);
				clearCastling(Castling.WHITE_QUEEN_SIDE);
			} else {
				clearCastling(Castling.BLACK_KING_SIDE);
				clearCastling(Castling.BLACK_QUEEN_SIDE);
			}
		}
	}
	
	private void castlePieces(int from1, int to1, int from2, int to2, boolean cellsOnly) {
		final Piece moved1 = board.getPiece(from1);
		final Piece moved2 = board.getPiece(from2);
		if (!cellsOnly) {
			// Update zobristKey
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
	
	/** Called when a rook moves or is captured.
	 * <br>It allows the board to erase the corresponding castling
	 * @param cell The cell that contains the rook.
	 */
	private void onRookEvent(int cell) {
		for (Castling castling : Castling.ALL) {
			if (cell==getInitialRookPosition(castling)) {
				clearCastling(castling);
				break;
			}
		}
	}
	
	/** {@inheritDoc}
	 * <br>This generic implementation returns the corner that contains the rook in standard chess
	 */
	@Override
	public int getInitialRookPosition(Castling castling) {
		//TODO Possible to optimize using same structure as in chess960
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

	private void pawnMove(int from, int to, Piece promotion, MoveHelperHolder mhh) {
		final Piece pawn = board.getPiece(from);
		if (promotion!=null) {
			board.setPiece(from, promotion);
			// Promotion => update key as if pawn is replaced by the promotion
			key ^= board.getZobrist().getKey(from, promotion);
			key ^= board.getZobrist().getKey(from, pawn);
		}
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			mhh.setEnPassant(pawn, from, to, enPassantDeletePawnIndex);
			final int pos = enPassantDeletePawnIndex;
			key ^= board.getZobrist().getKey(pos, mhh.get().getCaptured());
			board.setPiece(pos, null);
		} else {
			mhh.setSimple(pawn, from, board.getPiece(to), to);
		}
		clearEnPassant();
		
		final int rowOffset = board.getCoordinatesSystem().getRow(to) - board.getCoordinatesSystem().getRow(from);
		if (Math.abs(rowOffset)==2) {
			// Make en-passant available for opponent
			final boolean whiteMove = WHITE == pawn.getColor();
			setEnPassant(whiteMove ? board.getCoordinatesSystem().nextRow(to) : board.getCoordinatesSystem().previousRow(to), pawn.getColor().opposite(), to);
		}
	}
	
	/** Clears a castling.
	 * @param castling The castling to be erased.
	 */
	public void clearCastling(Castling castling) {
		if ((this.castlings & castling.getMask()) != 0) {
			key ^= board.getZobrist().getKey(castling);
			this.castlings -= castling.getMask();
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
	
	public boolean hasCastling() {
		return this.castlings!=0;
	}
	
	/** Copy another board in this.
	 * <br>WARNING: board history is not copied, it is not possible to undo moves after this method is called.
	 * @param other The other board
	 */
	protected void copy(Board<Move> other) {
		if (!getDimension().equals(other.getDimension())) {
			throw new IllegalArgumentException("Can't copy board with different dimension");
		}
		if (other instanceof ChessBoard o) {
			this.activeColor = o.activeColor;
			this.enPassant = o.getEnPassant();
			this.enPassantDeletePawnIndex = o.enPassantDeletePawnIndex;
			this.halfMoveCount = other.getHalfMoveCount();
			this.moveNumber = other.getMoveNumber();
			this.castlings = o.castlings;
			this.board.copy(o.board);
			this.key = other.getHashKey();
			this.keyHistory.clear();
			this.keyHistory.addAll(o.keyHistory);
			System.arraycopy(o.kingPositions, 0, kingPositions, 0, kingPositions.length);
			undoData.clear();
			this.insufficientMaterialDetector.copy(o.insufficientMaterialDetector);
			this.pinnedDetector.invalidate();
			this.setMoveComparatorBuilder(other.getMoveComparatorBuilder());
			this.movesBuilder.invalidate();
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public int getKingPosition(Color color) {
		return kingPositions[color.ordinal()];
	}

	@Override
	public boolean isWhiteToMove() {
		return activeColor == WHITE;
	}
	
	public Color getActiveColor() {
		return activeColor;
	}

	@Override
	public int getEnPassant() {
		return this.enPassant;
	}
	
	@Override
	public int getEnPassantTarget() {
		return enPassantDeletePawnIndex;
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
	public boolean isInsufficientMaterial() {
		return insufficientMaterialDetector.isInsufficient();
	}
	
	@Override
	public boolean isDrawByRepetition() {
		final int size = Math.min(keyHistory.size(), getHalfMoveCount());
		if (size<6) {
			return false;
		}
		int repetition = 0;
		for (int i = keyHistory.size()-2; i>=keyHistory.size()-size; i -= 2) {
			if (keyHistory.get(i).equals(key)) {
				repetition++;
				if (repetition==2) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isCheck() {
		return getPinnedDetector().getCheckCount()>0;
	}

	public boolean isDoubleCheck() {
		return getPinnedDetector().getCheckCount()>1;
	}

	BoardRepresentation getBoard() {
		return board;
	}

	@Override
	public Function<Board<Move>, SelectiveComparator<Move>> getMoveComparatorBuilder() {
		return moveComparatorBuilder;
	}

	@Override
	public void setMoveComparatorBuilder(Function<Board<Move>, SelectiveComparator<Move>> moveComparatorBuilder) {
		this.moveComparatorBuilder = moveComparatorBuilder;
		if (moveComparatorBuilder==null) {
			movesBuilder.setMoveComparator(null);
		} else {
			movesBuilder.setMoveComparator(moveComparatorBuilder.apply(this));
		}
	}

	@Override
	public Status getContextualStatus() {
		return getHalfMoveCount()>=100 || isInsufficientMaterial() || isDrawByRepetition() ? Status.DRAW : Status.PLAYING;
	}

	@Override
	public Status getEndGameStatus() {
		if (isCheck()) {
			return activeColor==WHITE ? Status.BLACK_WON : Status.WHITE_WON;
		} else {
			return Status.DRAW;
		}
	}
	
	public boolean isAttacked(int position, Color color) {
		return this.attackDetector.isAttacked(position, color);
	}

	public PinnedDetector getPinnedDetector() {
		this.pinnedDetector.load();
		return this.pinnedDetector;
	}
	
	AttackDetector getAttackDetector() {
		return this.attackDetector;
	}
	
	DirectionExplorer getDirectionExplorer() {
		return this.exp;
	}
	
	/** Checks whether the king will be safe after a move is played.
	 * @param source the index of the piece's cell to move.
	 * @param dest the index of destination cell
	 */
	public boolean isKingSafeAfterMove(int source, int dest) {
		final int kingPosition = moveOnlyCells(source, dest);
		final boolean result = !isAttacked(kingPosition, activeColor.opposite());
		undoData.previous();
		final MoveHelperHolder holder = undoData.get().moveHelperHolder;
		holder.get().unmakePieces(getBoard().pieces);
		return result;
	}

	
	/** Creates a copy of this board of the same class as this.
	 * @return a new Board.
	 */
	protected abstract ChessBoard create();
	
	@Override
	public Board<Move> fork() {
		final ChessBoard result = create();
		result.copy(this);
		return result;
	}
}