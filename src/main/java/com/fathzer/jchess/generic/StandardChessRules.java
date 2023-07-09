package com.fathzer.jchess.generic;

import com.fathzer.games.Rules;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

public class StandardChessRules implements Rules<Board<Move>> {
	public static final Rules<Board<Move>> INSTANCE = new StandardChessRules();
	
	protected StandardChessRules() {
		// Nothing to do
	}
	
	@Override
	public Board<Move> newGame() {
		return FENParser.from(FENParser.NEW_STANDARD_GAME);
	}
}
