package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.fen.FENParser;

class PawnPositionEvaluatorTest {

	@Test
	void test() {
		final CoordinatesSystem cs = FENParser.from(FENParser.NEW_STANDARD_GAME).getCoordinatesSystem();
		PositionEvaluator ev = new PawnPositionEvaluator();
		
		int index = cs.getIndex("d5");
		ev.add(cs.getRow(index), cs.getColumn(index), Color.WHITE);
		assertEquals(25, ev.getValue());
		
		index = cs.getIndex("e5");
		ev.add(cs.getRow(index), cs.getColumn(index), Color.BLACK);
		assertEquals(5, ev.getValue());
	}

}
