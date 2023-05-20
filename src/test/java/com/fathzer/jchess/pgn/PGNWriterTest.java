package com.fathzer.jchess.pgn;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.generic.StandardChessRules;

class PGNWriterTest {

	@Test
	void test() {
		final ChessRules rules = StandardChessRules.INSTANCE;
		final GameHistory history = new GameHistory(rules, rules.newGame());
		history.add(new SimpleMove("e2", "e4"));
		history.add(new SimpleMove("e7", "e5"));
		history.add(new SimpleMove("d1", "h5"));
		history.add(new SimpleMove("d7", "d6"));
		history.add(new SimpleMove("f1", "c4"));
		history.add(new SimpleMove("b8", "c6"));
		history.add(new SimpleMove("h5", "f7"));
		
		final PGNWriter writer = new PGNWriter();
		writer.getPGN(new PGNHeaders.Builder().build(), history).forEach(System.out::println);
	}
}
