package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;
import static com.fathzer.jchess.Piece.*;
import static com.fathzer.jchess.Direction.*;

import java.util.Collection;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Castling.Side;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.fast.FastBoardRepresentation;
import com.fathzer.jchess.standard.CompactMoveList;

import lombok.Getter;

public abstract class ChessBoard implements Board<Move> {
	@Getter
	final BoardRepresentation board;
	@Getter
	private int enPassant;
	@Getter
	private Color activeColor;
	private int castlings;
	@Getter
	private int halfMoveCount;
	@Getter
	private int moveNumber;
	@Getter
	private long key;
	
	protected ChessBoard(List<PieceWithPosition> pieces) {
		this(Dimension.STANDARD, pieces);
	}

	protected ChessBoard(Dimension dimension, List<PieceWithPosition> pieces) {
		this(dimension, pieces,Color.WHITE, Castling.ALL, -1, 0, 1);
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
			setEnPassant(enPassantIndex, activeColor);
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
	}
	
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

	/** Makes a move.
	 * <br>WARNING, this method does not verify the move is valid.
	 * @param move a Move.
	 * @throws IllegalArgumentException if there's no piece at move.getFrom().
	 */
	@Override
	public void move(Move move) {
		final int from = move.getFrom();
		int to = move.getTo();
		Piece movedPiece = board.getPiece(from);
		if (movedPiece==null) {
			throw new IllegalArgumentException("No piece at "+from);
		}
		Castling castling = null;
		if (PieceKind.PAWN.equals(movedPiece.getKind())) {
			pawnMove(from, to, move.promotedTo());
		} else {
			this.clearEnPassant();
			if (PieceKind.KING.equals(movedPiece.getKind())) {
				castling = onKingMove(from, to, movedPiece.getColor(), false);
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
	}
	
	@Override
	public void moveCellsOnly (int from, int to) {
		board.save();
		final Piece p = board.getPiece(from);
		final Color playingColor = p.getColor();
		Castling castling = null;
		if (PieceKind.PAWN.equals(p.getKind())) {
			fastPawnMove(to, playingColor);
		} else if (PieceKind.KING.equals(p.getKind())) {
			castling = onKingMove(from, to, playingColor, true);
		}
		if (castling==null) {
			move(from,to, true);
		}
	}
	
	@Override
	public void restoreMoveCellsOnly() {
		board.restore();
	}
	private Castling onKingMove(int from, int to, Color playingColor, boolean cellsOnly) {
		final Castling castling = getCastling(from, to, playingColor);
		if (castling!=null) {
			// Castling => Get the correct king's destination
			to = getKingDestination(castling); 
			// Move the rook too
			final int rookDest = to + castling.getSide().getRookOffset();
			final int initialRookPosition = getInitialRookPosition(castling);
			movePieces(from, to, initialRookPosition, rookDest, cellsOnly);
		}
		if (!cellsOnly) {
			final boolean whitePlaying = WHITE.equals(playingColor);
			eraseCastlings(whitePlaying ? Castling.WHITE_KING_SIDE : Castling.BLACK_KING_SIDE, 
					whitePlaying ? Castling.WHITE_QUEEN_SIDE : Castling.BLACK_QUEEN_SIDE);

		}
		board.updateKingPosition(playingColor, to);
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
		final boolean whiteMove = Color.WHITE.equals(playingColor);
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			final CoordinatesSystem cs = board.getCoordinatesSystem();
			board.setPiece(whiteMove ? cs.nextRow(enPassant) : cs.previousRow(to), null);
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
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			final boolean whiteMove = WHITE == pawn.getColor();
			final int pos = whiteMove ? board.getCoordinatesSystem().nextRow(enPassant) : board.getCoordinatesSystem().previousRow(enPassant);
			key ^= board.getZobrist().getKey(pos, board.getPiece(pos));
			board.setPiece(pos, null);
		}
		
		final int rowOffset = board.getCoordinatesSystem().getRow(to) - board.getCoordinatesSystem().getRow(from);
		if (Math.abs(rowOffset)==2) {
			// Make en-passant available for opponent
			final boolean whiteMove = WHITE == pawn.getColor();
			setEnPassant(whiteMove ? board.getCoordinatesSystem().nextRow(to) : board.getCoordinatesSystem().previousRow(to), pawn.getColor().opposite());
		} else {
			clearEnPassant();
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
	
	private void setEnPassant(int pos, Color catchingColor) {
		if (enPassant>=0) {
			// clear previous en passant key
			key ^= board.getZobrist().getKey(enPassant);
		}
		if (isCatcheableEnPassant(pos, catchingColor)) {
			this.enPassant = pos;
			key ^= board.getZobrist().getKey(enPassant);
		} else {
			clearEnPassant();
		}
	}
	
	private boolean isCatcheableEnPassant(int pos, Color catchingColor) {
		final DirectionExplorer exp = board.getDirectionExplorer(pos);
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
		}
		this.enPassant = -1;
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
			this.halfMoveCount = other.getHalfMoveCount();
			this.moveNumber = other.getMoveNumber();
			this.castlings = ((ChessBoard)other).castlings;
			this.board.copy(((ChessBoard)other).board);
			this.key = other.getKey();
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public int getKingPosition(Color color) {
		return board.getKingPosition(color);
	}

	@Override
	public ChessGameState newMoveList() {
		//TODO MoveList can represent move with bigger board
		return Dimension.STANDARD.equals(board.getDimension()) ? new CompactMoveList() : new BasicMoveList();
	}
}