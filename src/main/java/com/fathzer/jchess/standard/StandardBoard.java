package com.fathzer.jchess.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.generic.ChessBoard;
import com.fathzer.jchess.generic.MovesBuilder;

public class StandardBoard extends com.fathzer.jchess.generic.ChessBoard {
	
	public StandardBoard(List<PieceWithPosition> pieces, Color activeColor, Collection<Castling> castlings, int enPassant, int halfMoveCount, int moveNumber) {
		super(Dimension.STANDARD, pieces, activeColor, castlings, enPassant, halfMoveCount, moveNumber);
		castlings.forEach(this::checkCastling);
	}
	
	@Override
	protected MovesBuilder buildMovesBuilder() {
		return new MovesBuilder(this);
	}

	@Override
	protected ChessBoard create() {
		return new StandardBoard(Collections.emptyList(), Color.WHITE, Collections.emptyList(), -1, 0, 1);
	}
	
	private void checkCastling(Castling castling) {
		final int kingPosition = getCoordinatesSystem().getIndex(Color.BLACK.equals(castling.getColor()) ? 0 : getDimension().getHeight()-1, 4);
		if (getKingPosition(castling.getColor())!=kingPosition) {
			throw new IllegalArgumentException("Invalid castling: King is not at its initial position");
		}
		final int rookPosition = kingPosition + (Castling.BLACK_KING_SIDE.equals(castling) || Castling.WHITE_KING_SIDE.equals(castling) ? 3 : -4);
		final Piece rook = Color.BLACK.equals(castling.getColor()) ? Piece.BLACK_ROOK : Piece.WHITE_ROOK;
		if (!rook.equals(getPiece(rookPosition))) {
			throw new IllegalArgumentException("Invalid castling: Rook is not at its initial position");
		}
	}
}
