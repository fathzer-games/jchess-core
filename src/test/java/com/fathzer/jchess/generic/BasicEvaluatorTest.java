package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

class BasicEvaluatorTest {

	@Test
	void test() {
		Board<Move> board = FENParser.from(FENParser.NEW_STANDARD_GAME);
		BasicEvaluator ev = new BasicEvaluator();
//		MoveList list = new CompactMoveList();
//		list.add(Coord.toIndex("a2"), Coord.toIndex("a4"));
		
		assertEquals(0, ev.evaluate(board));
	}

}
