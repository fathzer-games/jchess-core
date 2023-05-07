package com.fathzer.jchess.fischerrandom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.PieceWithPosition;

public class ChessBoard extends com.fathzer.jchess.generic.ChessBoard {
	private int[] initialRookPositions;
	
	public ChessBoard(List<PieceWithPosition> pieces) {
		super(Dimension.STANDARD, pieces);
		setInitialRookPositions(pieces);
	}

	public ChessBoard(List<PieceWithPosition> pieces, Color activeColor, Collection<Castling> castlings, int[] initialRookPositions, int enPassant, int halfMoveCount, int moveNumber) {
		super(Dimension.STANDARD, pieces, activeColor, castlings, enPassant, halfMoveCount, moveNumber);
		this.initialRookPositions = initialRookPositions;
	}
	
	@Override
	public Board<Move> create() {
		return new ChessBoard(Collections.emptyList(), Color.WHITE, Collections.emptyList(), new int[4], -1, 0, 1);
	}
	

	@Override
	public int getInitialRookPosition(Castling castling) {
		return initialRookPositions[castling.ordinal()];
	}

	private void setInitialRookPositions(List<PieceWithPosition> pieces) {
		this.initialRookPositions = new int[4];
		Arrays.fill(initialRookPositions, -1);
		pieces.stream().filter(p->PieceKind.ROOK.equals(p.getPiece().getKind())).forEach(this::fillRookPosition);
	}

	private void fillRookPosition(PieceWithPosition p) {
		final Color color = p.getPiece().getColor();
		final Castling castling = Castling.get(color, p.getPosition()>super.getKingPosition(color));
		if (super.hasCastling(castling)) {
			initialRookPositions[castling.ordinal()] = p.getPosition();
		}
	}

	@Override
	public void copy(Board<Move> other) {
		super.copy(other);
		System.arraycopy(((ChessBoard)other).initialRookPositions, 0, initialRookPositions, 0, initialRookPositions.length);
	}
}
