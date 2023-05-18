package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.generic.StandardChessRules;

class GameHistoryTest {

	@Test
	void test() {
		final ChessRules rules = StandardChessRules.INSTANCE;
		final GameHistory history = new GameHistory(rules);
		history.add(new SimpleMove("e2", "e4"));
		history.add(new SimpleMove("e7", "e5"));
		history.add(new SimpleMove("d1", "h5"));
		history.add(new SimpleMove("d7", "d6"));
		history.add(new SimpleMove("f1", "c4"));
		history.add(new SimpleMove("b8", "c6"));
		history.add(new SimpleMove("h5", "f7"));
		
		history.getPGN().forEach(System.out::println);
	}
}
