package com.fathzer.jchess.generic;


import com.fathzer.games.MoveGenerator;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.fen.FENUtils;


class MovesCheckerTest extends GenericMovesCheckerTest<Move> {
	private static final CoordinatesSystem CS = FENUtils.from(FENUtils.NEW_STANDARD_GAME).getCoordinatesSystem();

	public MoveGenerator<Move> fromFEN(String fen) {
		return FENUtils.from(fen);
	}
	protected String toString(Move move) {
		return move.toString(CS);
	}
	public Move toMove(String from, String to) {
		return new SimpleMove(CS, from, to);
	}
	public Move toMove(String from, String to, String promotion) {
		Piece piece = Piece.valueOf(promotion);
		return new SimpleMove(CS, from, to, piece);
	}
}
