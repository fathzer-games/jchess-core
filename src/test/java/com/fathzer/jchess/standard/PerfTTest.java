package com.fathzer.jchess.standard;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.perft.PerfT;
import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTResult;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CopyBasedMoveGenerator;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.StandardChessRules;

class PerfTTest {
	@Test
	void test() throws IOException {
		final Iterator<PerfTTestData> iterator = readTests().iterator();
		while (iterator.hasNext()) {
			final PerfTTestData test = iterator.next();
			try {
				doTest(test);
			} catch (Exception e) {
				fail("Exception on "+test.getStartPosition(),e);
			}
		}
	}

	private void doTest(PerfTTestData test) {
		final int depth = 3;
		final Board<Move> board = FENParser.from(test.getStartPosition()+" 0 1");
		final PerfT<Move> perfT = new PerfT<>(() -> new CopyBasedMoveGenerator<>(StandardChessRules.PERFT, board));
		if (test.getSize()>=depth) {
//			try {
				final PerfTResult<Move> divide = perfT.divide(depth);
				assertEquals(test.getCount(depth), divide.getNbLeaves(), "Error for "+test.getStartPosition()+". Divide is "+divide);
//				if (count != test.getCount(depth)) {
//					System.out.println("Error for "+test.getFen()+" expected "+test.getCount(depth)+" got "+count);
//				} else {
//					System.out.println("Ok for "+test.getFen());
//				}
//			} catch (RuntimeException e) {
//				System.out.println("Exception for "+test.getFen());
//				throw e;
//			}
		}
	}

	private List<PerfTTestData> readTests() throws IOException {
		try (InputStream stream = getClass().getResourceAsStream("/Perft.txt")) {
			return new PerfTParser().withStartPositionPrefix("position fen").read(stream, StandardCharsets.UTF_8);
		}
	}
}
