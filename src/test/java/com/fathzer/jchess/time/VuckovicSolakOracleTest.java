package com.fathzer.jchess.time;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.time.RemainingMoveCountPredictor;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

class VuckovicSolakOracleTest {

	@Test
	void test() {
		final RemainingMoveCountPredictor<Board<Move>> oracle = new VuckovicSolakOracle();
		Board<Move> mg = FENUtils.from("4k3/6p1/3q4/8/8/8/8/1N2K3 w - - 0 1");
		assertEquals (23, oracle.getRemainingHalfMoves(mg));
		mg =FENUtils.from("4k3/6p1/3q4/8/8/8/5Q2/1N2K3 w - - 0 1");
		assertEquals (30, oracle.getRemainingHalfMoves(mg));
		mg =FENUtils.from("4k3/r4bpr/2nqnp2/8/8/P4B1N/3R1QPP/RN2K3 w Q - 0 1");
		assertEquals (46, oracle.getRemainingHalfMoves(mg));
	}
}
