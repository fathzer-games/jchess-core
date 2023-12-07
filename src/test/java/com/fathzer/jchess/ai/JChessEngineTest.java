package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.evaluator.simple.SimpleEvaluator;
import com.fathzer.jchess.fen.FENUtils;

class JChessEngineTest {

	@Test
	void test() {
		Board<Move> board = FENUtils.from("2kr1r2/ppp2p2/6p1/2Np4/3P2b1/P7/1PPq2PP/1RK1R2B w B - 0 22");
		System.out.println(board.getLegalMoves().size());
		JChessEngine engine = new JChessEngine(SimpleEvaluator::new, 10);
		engine.getDeepeningPolicy().setDeepenOnForced(false);
		System.out.println(engine.apply(board));
	}

}
