package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

class AttackDetectorTest {

	@Test
	void test() {
		final Board<Move> board = FENUtils.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		AttackDetector explorer = new AttackDetector(board.getDirectionExplorer(-1));
		assertTrue(explorer.isAttacked(cs.getIndex("e8"), Color.WHITE), "d6 knight is a threat");
		assertTrue(explorer.isAttacked(cs.getIndex("d1"), Color.BLACK), "c2 pawn is a threat");
		assertTrue(explorer.isAttacked(cs.getIndex("b1"), Color.BLACK), "c2 pawn is a threat");
		assertFalse(explorer.isAttacked(cs.getIndex("c1"), Color.BLACK), "no threat is expected");
		assertTrue(explorer.isAttacked(cs.getIndex("e7"), Color.WHITE), "e4 queen is a threat");
		assertTrue(explorer.isAttacked(cs.getIndex("c5"), Color.WHITE), "a3 bishop is a threat");
		assertFalse(explorer.isAttacked(cs.getIndex("b6"), Color.BLACK), "no threat is expected");
	}
	
//	@Test
	void speedTest() {
		final Board<Move> board = FENUtils.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		AttackDetector explorer = new AttackDetector(board.getDirectionExplorer(-1));
		for (int i=0;i<100000;i++) {
			explorer.isAttacked(cs.getIndex("e8"), Color.WHITE);
			explorer.isAttacked(cs.getIndex("d1"), Color.BLACK);
			explorer.isAttacked(cs.getIndex("c1"), Color.BLACK);
			explorer.isAttacked(cs.getIndex("c5"), Color.WHITE);
			explorer.isAttacked(cs.getIndex("b6"), Color.BLACK);
		}
		long start = System.currentTimeMillis();
		int nb = 200000;
		for (int i=0;i<200000;i++) {
			explorer.isAttacked(cs.getIndex("e8"), Color.WHITE);
			explorer.isAttacked(cs.getIndex("d1"), Color.BLACK);
			explorer.isAttacked(cs.getIndex("c1"), Color.BLACK);
			explorer.isAttacked(cs.getIndex("c5"), Color.WHITE);
			explorer.isAttacked(cs.getIndex("b6"), Color.BLACK);
		}
		System.out.println(String.format("Speed= %d checks per second",nb*5000/(System.currentTimeMillis()-start)));
	}

}
