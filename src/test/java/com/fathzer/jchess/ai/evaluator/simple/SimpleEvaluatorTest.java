package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.fen.FENParser;

class SimpleEvaluatorTest {

	@Test
	void test() {
		final SimpleEvaluator ev = new SimpleEvaluator();
		assertEquals(0, ev.getPoints(FENParser.from(FENParser.NEW_STANDARD_GAME)));
		assertEquals(5, ev.getPoints(FENParser.from("3k4/8/8/3Pp3/8/8/8/4K3 w - - 0 1")));
	}
}
