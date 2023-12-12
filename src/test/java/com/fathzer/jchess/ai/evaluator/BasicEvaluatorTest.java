package com.fathzer.jchess.ai.evaluator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

class BasicEvaluatorTest {

	@Test
	void test() {
		Board<Move> board = FENUtils.from(FENUtils.NEW_STANDARD_GAME);
		assertEquals(0, new BasicEvaluator(board).evaluate(board));
		board = FENUtils.from("8/8/8/7R/k7/p1R1p3/4r3/1K2b3 b - - 0 7");
		assertEquals(0, new BasicEvaluator(board).evaluate(board));
	}
}
