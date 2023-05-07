package com.fathzer.jchess.standard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

class DefenderDetectorTest {
	@Test
	void test() {
		Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		DefenderDetector dd = new DefenderDetector(board, Color.WHITE);
		assertFalse(IntStream.range(0, 64).anyMatch(dd));
		
		
		board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n2/pP6/1b6/B4P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		dd = new DefenderDetector(board, Color.WHITE);
		assertArrayEquals(new int[] {51,52}, IntStream.range(0, 64).filter(dd::test).sorted().toArray());
	}
}
