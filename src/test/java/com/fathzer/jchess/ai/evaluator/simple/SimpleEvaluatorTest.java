package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

class SimpleEvaluatorTest {

	@Test
	void test() {
		Board<Move> board = FENUtils.from(FENUtils.NEW_STANDARD_GAME);
		assertEquals(0, new SimpleEvaluator(board).getPoints(board));
		board = FENUtils.from("3k4/8/8/3Pp3/8/8/8/4K3 w - - 0 1");
		assertEquals(5, new SimpleEvaluator(board).getPoints(board));
	}
}
