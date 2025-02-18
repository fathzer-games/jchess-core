package com.fathzer.jchess.chess960;

import static com.fathzer.games.Color.*;

import java.util.Collection;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.Variant;
import com.fathzer.jchess.generic.ChessBoard;
import com.fathzer.jchess.generic.MovesBuilder;

public class Chess960Board extends ChessBoard {
	private int[] initialRookPositions;
	
	private Chess960Board() {
		super(Dimension.STANDARD);
		this.initialRookPositions = new int[4];
	}
	
	/** Constructor.
	 * @param pieces the pieces with their positions.
	 * @param activeColor the color to play.
	 * @param castlings the possible castlings list.
	 * @param initialRookColumns the initial columns of the rooks involved in castling. Index 0 is for king side, 1 for queen side (-1 for no castling on that side).
	 * @param enPassant the en passant cell index.
	 * @param halfMoveCount the half move count.
	 * @param moveNumber the move number.
	 * @throws IllegalArgumentException if the initial rook positions are not consistent with the castlings or the pieces list or there is not exactly one king for each color.
	 */
	public Chess960Board(List<PieceWithPosition> pieces, Color activeColor, Collection<Castling> castlings, int[] initialRookColumns, int enPassant, int halfMoveCount, int moveNumber) {
		super(Dimension.STANDARD, pieces, activeColor, castlings, enPassant, halfMoveCount, moveNumber);
		if (initialRookColumns.length != 2) {
			throw new IllegalArgumentException("initialRookColumns must have 2 elements");
		}
		this.initialRookPositions = new int[]{-1,-1,-1,-1};
		for (Castling castling : castlings) {
			final int column = initialRookColumns[castling.getSide().ordinal()];
			if (column<0 || column>=getDimension().getWidth()) {
				throw new IllegalArgumentException(String.format("no valid initial rook column found for %s side castling ",castling.getSide()));
			}
			initialRookPositions[castling.ordinal()] = getCoordinatesSystem().getIndex(castling.getColor()==BLACK? 0 : getDimension().getHeight()-1, column);
			checkCastling(castling);
		}
	}

	@Override
	public Variant getVariant() {
		return Variant.CHESS960;
	}
	
	@Override
	protected MovesBuilder buildMovesBuilder() {
		return new Chess960MovesBuilder(this);
	}
	
	@Override
	protected ChessBoard create() {
		return new Chess960Board();
	}
	

	@Override
	public int getInitialRookPosition(Castling castling) {
		return initialRookPositions[castling.ordinal()];
	}
	
	@Override
	public Castling getCastling(int from, int to) {
		if (getCoordinatesSystem().getRow(from)!=getCoordinatesSystem().getRow(to)) {
			// Doesn't move horizontally
			return null;
		}
		final Castling castling = Castling.get(getActiveColor(), to>from);
		return to==getInitialRookPosition(castling) && hasCastling(castling) ? castling : null;
	}


	@Override
	protected void copy(Board<Move> other) {
		super.copy(other);
		System.arraycopy(((Chess960Board)other).initialRookPositions, 0, initialRookPositions, 0, initialRookPositions.length);
	}
}
