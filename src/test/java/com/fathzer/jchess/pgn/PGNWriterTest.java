package com.fathzer.jchess.pgn;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.generic.StandardChessRules;

class PGNWriterTest {

	@Test
	void test() {
		final ChessRules rules = StandardChessRules.INSTANCE;
		final Board<Move> board = rules.newGame();
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final GameHistory history = new GameHistory(rules, board);
		history.add(new SimpleMove(cs, "e2", "e4"));
		history.add(new SimpleMove(cs, "e7", "e5"));
		history.add(new SimpleMove(cs, "d1", "h5"));
		history.add(new SimpleMove(cs, "d7", "d6"));
		history.add(new SimpleMove(cs, "f1", "c4"));
		history.add(new SimpleMove(cs, "b8", "c6"));
		history.add(new SimpleMove(cs, "h5", "f7"));
		
		final PGNWriter writer = new PGNWriter();
		writer.getPGN(new PGNHeaders.Builder().build(), history).forEach(System.out::println);
	}
}
