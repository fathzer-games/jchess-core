package com.fathzer.jchess.generic;

import static com.fathzer.jchess.SimpleMove.*;

import com.fathzer.games.MoveGenerator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;


class MovesCheckerTest extends GenericMovesCheckerTest<Move> {
	private static final Board<Move> BOARD = FENUtils.from(FENUtils.NEW_STANDARD_GAME);

	public MoveGenerator<Move> fromFEN(String fen) {
		return FENUtils.from(fen);
	}
	protected String toString(Move move) {
		return move.toString(BOARD.getCoordinatesSystem());
	}
	public Move toMove(String from, String to) {
		return get(BOARD, from, to);
	}
	public Move toMove(String from, String to, String promotion) {
		Piece piece = Piece.valueOf(promotion);
		return get(BOARD, from, to, piece);
	}
}
