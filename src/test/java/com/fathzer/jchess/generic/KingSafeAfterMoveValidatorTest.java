package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.util.BiIntPredicate;

class KingSafeAfterMoveValidatorTest {

	@Test
	void test() {
		final Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R b KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final BiIntPredicate v = new KingSafeAfterMoveValidator(board, new AttackDetector(board.getDirectionExplorer(-1)));
		assertFalse(v.test(cs.getIndex("a5"), cs.getIndex("a4")));
		assertFalse(v.test(cs.getIndex("e7"), cs.getIndex("d6")));
		assertFalse(v.test(cs.getIndex("e7"), cs.getIndex("e4")));
		assertTrue(v.test(cs.getIndex("e8"), cs.getIndex("d8")));
		assertTrue(v.test(cs.getIndex("e8"), cs.getIndex("f8")));
	}

}
