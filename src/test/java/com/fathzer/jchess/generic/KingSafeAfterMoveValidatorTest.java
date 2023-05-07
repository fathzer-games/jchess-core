package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.standard.Coord;
import com.fathzer.jchess.util.BiIntPredicate;

class KingSafeAfterMoveValidatorTest {

	@Test
	void test() {
		final Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final BiIntPredicate v = new KingSafeAfterMoveValidator(board);
		assertFalse(v.test(Coord.toIndex("a5"), Coord.toIndex("a4")));
		assertFalse(v.test(Coord.toIndex("e7"), Coord.toIndex("d6")));
		assertFalse(v.test(Coord.toIndex("e7"), Coord.toIndex("e4")));
		assertTrue(v.test(Coord.toIndex("e8"), Coord.toIndex("d8")));
		assertTrue(v.test(Coord.toIndex("e8"), Coord.toIndex("f8")));
	}

}
