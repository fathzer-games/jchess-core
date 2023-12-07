package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.fen.FENUtils;

class SimpleEvaluatorTest {

	@Test
	void test() {
		SimpleEvaluator ev = new SimpleEvaluator(FENUtils.from(FENUtils.NEW_STANDARD_GAME));
		assertEquals(0, ev.getPoints());
		ev = new SimpleEvaluator(FENUtils.from("3k4/8/8/3Pp3/8/8/8/4K3 w - - 0 1"));
		assertEquals(5, ev.getPoints());
	}
}
