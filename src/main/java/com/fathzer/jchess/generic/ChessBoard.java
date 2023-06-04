package com.fathzer.jchess.generic;

import java.util.Collection;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Castling.Side;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Dimension.Explorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.standard.CompactMoveList;

import lombok.Getter;

public abstract class ChessBoard implements Board<Move> {
	
	@Getter
	private final Dimension dimension;
	@Getter
	private final CoordinatesSystem coordinatesSystem;

	private final Piece[] pieces;
	@Getter
	private Color activeColor;
	private int castlings;
	@Getter
	private int enPassant;
	@Getter
	private int halfMoveCount;
	@Getter
	private int moveNumber;
	@Getter
	private long key;
	private final int[] kingPositions=new int[2];
	private final int[] kingPositionBackup=new int[2];
	private final Piece[] backup;
	
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
		this.dimension = dimension;
		this.coordinatesSystem = new DefaultCoordinatesSystem(dimension);
		if (activeColor==null) {
			throw new NullPointerException();
		}
		this.pieces = new Piece[dimension.getSize()];
		this.backup = new Piece[this.pieces.length];
		for (PieceWithPosition p : pieces) {
			final int dest = coordinatesSystem.getIndex(p.getRow(), p.getColumn());
			if (this.pieces[dest]!=null) {
				throw new IllegalArgumentException ("More than one piece at "+dest+": "+this.pieces[dest]+"/"+p.getPiece());
			}
			this.pieces[dest]=p.getPiece();
			if (PieceKind.KING.equals(p.getPiece().getKind())) {
				this.kingPositions[p.getPiece().getColor().ordinal()] = dest;
			}
		}
		this.activeColor = activeColor;
		this.castlings = castlings==null ? 0 : Castling.toInt(castlings);
		this.enPassant = -1;
		if (enPassantColumn>=0) {
			final int enPassantIndex = coordinatesSystem.getIndex(getEnPassantRow(activeColor), enPassantColumn);
			checkEnPassant(activeColor, enPassantIndex);
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
		this.key = dimension.getZobristKeyBuilder().get(this);
	}
	
	private int getEnPassantRow(Color activeColor) {
		return Color.WHITE.equals(activeColor) ? 2 : dimension.getHeight()-3;
	}
	
	private void checkEnPassant(Color activeColor, int enPassant) {
		if (this.pieces[enPassant]!=null) {
			throw new IllegalArgumentException("EnPassant cell is not empty");
		}
		final int pawnCell = enPassant + (Color.WHITE.equals(activeColor) ? dimension.getWidth() : -dimension.getWidth());
		if (!(Color.WHITE.equals(activeColor)?Piece.BLACK_PAWN:Piece.WHITE_PAWN).equals(this.pieces[pawnCell])) {
			throw new IllegalArgumentException("Attacked enPassant pawn is missing");
		}
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
		Piece movedPiece = this.pieces[from];
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
			final Piece erasedPiece = this.pieces[to];
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
		key ^= dimension.getZobristKeyBuilder().getTurnKey();
	}
	
	@Override
	public void moveCellsOnly (int from, int to) {
		System.arraycopy(this.pieces, 0, backup, 0, pieces.length);
		System.arraycopy(this.kingPositions, 0, this.kingPositionBackup, 0, kingPositions.length);
		Piece p = this.pieces[from];
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
		System.arraycopy(backup, 0, this.pieces, 0, pieces.length);
		System.arraycopy(kingPositionBackup, 0, this.kingPositions, 0, kingPositions.length);
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
			final boolean whitePlaying = Color.WHITE.equals(playingColor);
			eraseCastlings(whitePlaying ? Castling.WHITE_KING_SIDE : Castling.BLACK_KING_SIDE, 
					whitePlaying ? Castling.WHITE_QUEEN_SIDE : Castling.BLACK_QUEEN_SIDE);

		}
		kingPositions[playingColor.ordinal()] = to;
		return castling;
	}
	
	private void movePieces(int from1, int to1, int from2, int to2, boolean cellsOnly) {
		final Piece moved1 = this.pieces[from1];
		final Piece moved2 = this.pieces[from2];
		if (!cellsOnly) {
			// Update zobristKey
			if (this.pieces[to1]!=null) {
				key ^= dimension.getZobristKeyBuilder().getKey(to1, this.pieces[to1]);
			}
			if (this.pieces[to2]!=null) {
				key ^= dimension.getZobristKeyBuilder().getKey(to2, this.pieces[to2]);
			}
			key ^= dimension.getZobristKeyBuilder().getKey(from1, moved1);
			key ^= dimension.getZobristKeyBuilder().getKey(to1, moved1);
			key ^= dimension.getZobristKeyBuilder().getKey(from2, moved2);
			key ^= dimension.getZobristKeyBuilder().getKey(to2, moved2);
		}
		this.pieces[from1] = null;
		this.pieces[from2] = null;
		this.pieces[to1] = moved1;
		this.pieces[to2] = moved2;
	}
	
	private void move(int from, int to, boolean cellsOnly) {
		final Piece moved = this.pieces[from];
		if (!cellsOnly) {
			// Update zobristKey
			final Piece erased = this.pieces[to];
			if (erased!=null) {
				key ^= dimension.getZobristKeyBuilder().getKey(to, erased);
			}
			key ^= dimension.getZobristKeyBuilder().getKey(from, moved);
			key ^= dimension.getZobristKeyBuilder().getKey(to, moved);
		}
		this.pieces[to] = moved;
		this.pieces[from] = null;
	}
	
	private void fastPawnMove(int to, Color playingColor) {
		final boolean whiteMove = Color.WHITE.equals(playingColor);
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			pieces[enPassant+(whiteMove ? 1 : -1)*dimension.getWidth()] = null;
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
	public int getInitialRookPosition(Castling castling) {
		if (Castling.BLACK_QUEEN_SIDE.equals(castling)) {
			return 0;
		} else if (Castling.BLACK_KING_SIDE.equals(castling)) {
			return dimension.getWidth()-1;
		} else if (Castling.WHITE_KING_SIDE.equals(castling)) {
			return dimension.getSize()-1;
		} else if (Castling.WHITE_QUEEN_SIDE.equals(castling)) {
			return dimension.getSize() - dimension.getWidth();
		} else {
			return -1;
		}
	}
	
	@Override
	public int getKingDestination(Castling castling) {
		final int row = Color.WHITE==castling.getColor() ? dimension.getHeight()-1 : 0;
		final int column = Side.QUEEN==castling.getSide() ? 2 : dimension.getWidth()-2;
		return coordinatesSystem.getIndex(row, column);
	}

	private void pawnMove(int from, int to, Piece promotion) {
		final Piece pawn = pieces[from];
		if (promotion!=null) {
			pieces[from] = promotion;
			// Promotion => update key as if pawn is replaced by the promotion
			key ^= dimension.getZobristKeyBuilder().getKey(from, pieces[from]);
			key ^= dimension.getZobristKeyBuilder().getKey(from, pawn);
		}
		if (to==enPassant) {
			// en-passant catch => delete adverse's pawn
			final boolean whiteMove = Color.WHITE.equals(pawn.getColor());
			final int pos = enPassant+(whiteMove ? 1 : -1)*dimension.getWidth();
			key ^= dimension.getZobristKeyBuilder().getKey(pos, pieces[pos]);
			pieces[pos] = null;
		}
		
		final int rowOffset = coordinatesSystem.getRow(to) - coordinatesSystem.getRow(from);
		if (Math.abs(rowOffset)==2) {
			// Make en-passant available for opponent
			setEnPassant(from + rowOffset*dimension.getWidth()/2, pawn.getColor().opposite());
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
				key ^= dimension.getZobristKeyBuilder().getKey(castling);
				this.castlings -= castling.getMask();
			}
		}
	}
	
	private void setEnPassant(int pos, Color catchingColor) {
		if (enPassant>=0) {
			key ^= dimension.getZobristKeyBuilder().getKey(enPassant);
		}
		if (isCatcheableEnPassant(pos, catchingColor)) {
			this.enPassant = pos;
			key ^= dimension.getZobristKeyBuilder().getKey(enPassant);
		} else {
			clearEnPassant();
		}
	}
	
	private boolean isCatcheableEnPassant(int pos, Color catchingColor) {
		final Explorer exp = dimension.new Explorer(pos);
		final int rowIncrement = Color.WHITE.equals(catchingColor) ? 1:-1;
		exp.start(rowIncrement,-1);
		if (exp.hasNext() && isPawn(exp.next(), catchingColor)) {
			return true;
		}
		exp.start(rowIncrement,1);
		return exp.hasNext() && isPawn(exp.next(), catchingColor);
	}
	
	private boolean isPawn(int pos, Color color) {
		return (color.equals(Color.WHITE) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN).equals(pieces[pos]);
	}
	
	private void clearEnPassant() {
		if (enPassant>=0) {
			key ^= dimension.getZobristKeyBuilder().getKey(enPassant);
		}
		this.enPassant = -1;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < dimension.getSize() ; i++) {
			final boolean newLine = i%dimension.getWidth()==0;
			if (newLine) {
				if (i!=0) {
					b.append('\n');
				}
				newLine(b, dimension.getHeight() - i/dimension.getWidth());
			} else {
				b.append(' ');
			}
			b.append(getNotation(pieces[i]));
		}
		b.append(getLastLine());
		return b.toString();
	}
	
	private CharSequence getLastLine() {
		StringBuilder b = new StringBuilder("\n ");
		char coord = 'a';
		for (int j = 0; j < dimension.getWidth(); j++) {
			b.append(' ');
			b.append(coord);
			coord++;
		}
		return b;
	}

	protected void newLine(final StringBuilder b, int i) {
		b.append(i).append(' ');
	}
	
	@Override
	public Piece getPiece(int position) {
		return this.pieces[position];
	}
	
	private String getNotation(Piece p) {
		if (p==null) {
			return " ";
		} else {
			return p.getNotation();
		}
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
		if (!dimension.equals(other.getDimension())) {
			throw new IllegalArgumentException("Can't copy board with different dimension");
		}
		if (other instanceof ChessBoard) {
			this.activeColor = other.getActiveColor();
			this.enPassant = other.getEnPassant();
			this.halfMoveCount = other.getHalfMoveCount();
			this.moveNumber = other.getMoveNumber();
			this.castlings = ((ChessBoard)other).castlings;
			System.arraycopy(((ChessBoard)other).kingPositions, 0, kingPositions, 0, kingPositions.length);
			System.arraycopy(((ChessBoard)other).pieces, 0, pieces, 0, pieces.length);
			this.key = other.getKey();
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public int getKingPosition(Color color) {
		return kingPositions[color.ordinal()];
	}

	@Override
	public ChessGameState newMoveList() {
		return Dimension.STANDARD.equals(dimension) ? new CompactMoveList() : new BasicMoveList();
	}
}