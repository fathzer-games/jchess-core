package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.jchess.ai.evaluator.simple.Phase.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.fen.FENParser;

class SimpleEvaluatorTest {

	@Test
	void test() {
		final SimpleEvaluator ev = new SimpleEvaluator();
		// Queen + rook => middle game
		assertEquals(MIDDLE_GAME, ev.getPhase(FENParser.from("r2qk3/8/8/8/8/8/8/4K3 w q - 0 1").getExplorer()));
		// Queen + bishop vs two rooks => end game
		assertEquals(END_GAME, ev.getPhase(FENParser.from("3qk3/7b/8/8/8/8/8/R3K2R w - - 0 1").getExplorer()));
		// 2 Queens => middle game
		assertEquals(MIDDLE_GAME, ev.getPhase(FENParser.from("3qk3/8/8/8/8/7Q/8/4K3 w - - 0 1").getExplorer()));
	}
}
