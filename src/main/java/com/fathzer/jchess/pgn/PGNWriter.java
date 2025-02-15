package com.fathzer.jchess.pgn;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Variant;
import com.fathzer.jchess.fen.FENUtils;

public class PGNWriter extends AbstractPGNWriter<Move, Board<Move>> {
	private static final MoveAlgebraicNotationBuilder AN = new MoveAlgebraicNotationBuilder().withPlayMove(true).withEnPassantSymbol("");
	
	@Override
	protected String getFEN(Board<Move> board) {
		return FENUtils.to(board);
	}

	@Override
	protected int getMoveNumber(Board<Move> board) {
		return board.getMoveNumber();
	}

	@Override
	protected String getAlgebraicNotation(Move move, Board<Move> board) {
		return AN.get(board, move);
	}

	@Override
	protected String getVariant(Board<Move> board) {
		return board.getVariant()==Variant.CHESS960 ? "Chess960" : null;
	}
}
