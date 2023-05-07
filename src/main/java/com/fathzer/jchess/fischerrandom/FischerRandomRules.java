package com.fathzer.jchess.fischerrandom;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.generic.StandardChessRules;

public class FischerRandomRules extends StandardChessRules {
	public static final FischerRandomRules INSTANCE = new FischerRandomRules();
	
	protected FischerRandomRules() {
		super();
	}
	
	@Override
	public Board<Move> newGame() {
		return new ChessBoard(StartPositionGenerator.INSTANCE.get());
	}

	@Override
	protected void addCastling(ChessGameState moves, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		moves.add(kingPosition, rookPosition);
	}
}
