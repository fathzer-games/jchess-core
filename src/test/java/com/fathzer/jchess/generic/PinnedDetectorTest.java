package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

class PinnedDetectorTest {
	@Test
	void test() {
		Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final PinnedDetector dd = new PinnedDetector(board);
		final BoardExplorer exp = board.getExplorer();
		do {
			assertNull(dd.apply(exp.getIndex()));
		} while (exp.next());
		
		
		board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n2/pP6/1b6/B4P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final PinnedDetector dd2 = new PinnedDetector(board);
		assertEquals(Direction.NORTH, dd2.apply(cs.getIndex("e2")));
		assertEquals(Direction.NORTH_WEST, dd2.apply(cs.getIndex("d2")));
		assertFalse(IntStream.range(0, 64).filter(p -> p!=51 && p!=52).anyMatch(p -> dd2.apply(p)!=null));
	}
}
