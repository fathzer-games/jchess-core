package com.fathzer.jchess.standard;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.fathzer.games.perft.Divide;
import com.fathzer.games.perft.PerfTBuilder;
import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTResult;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PerfTTest {
	@DisabledIfSystemProperty(named="perftDepth",matches = "0")
	@Test
	void test() throws IOException {
		final int depth = Integer.getInteger("perftDepth", 1);
		if (depth!=1) {
			log.info("PerfT test depth is set to {}",depth);
		}
		readTests().stream().parallel().forEach(test -> {
			try {
				doTest(test, depth);
			} catch (Exception e) {
				fail("Exception on "+test.getStartPosition(),e);
			}
		});
//		final List<PerfTTestData> iterator = readTests().iterator();
//		try (ContextualizedExecutor<MoveGenerator<Move>> exec =new ContextualizedExecutor<>(PhysicalCores.count())) {
//			while (iterator.hasNext()) {
//				final PerfTTestData test = iterator.next();
//				try {
//					doTest(exec, test, depth);
//				} catch (Exception e) {
//					fail("Exception on "+test.getStartPosition(),e);
//				}
//			}
//		}
	}
	
//	@Test
	void showDivide() {
		final Board<Move> board = FENUtils.from("8/8/6b1/k3p2N/8/b1PB4/K6p/8 b - - 0 1");
		final PerfTBuilder<Move> perfT = new PerfTBuilder<>();
		perfT.setPlayLeaves(false);
		final PerfTResult<Move> divide = perfT.build(board, 2).get();
		System.out.println("Leaves: "+ divide.getNbLeaves());
		System.out.println("Divide is "+toString(divide.getDivides(),board.getCoordinatesSystem()));
	}

	private void doTest(PerfTTestData test, int depth) {
		final Board<Move> board = FENUtils.from(test.getStartPosition()+" 0 1");
		final PerfTBuilder<Move> perfT = new PerfTBuilder<>();
		perfT.setPlayLeaves(false);
		if (test.getSize()>=depth) {
//			try {
				final PerfTResult<Move> divide = perfT.build(board, depth).get();
				assertEquals(test.getExpectedLeaveCount(depth), divide.getNbLeaves(), "Error for "+test.getStartPosition()+". Divide is "+toString(divide.getDivides(),board.getCoordinatesSystem()));
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
	
	private String toString(Collection<Divide<Move>> divides, CoordinatesSystem cs) {
		return divides.stream().map(d -> toString(d, cs)).collect(Collectors.joining(", ", "[", "]"));
		
	}
	
	private String toString(Divide<Move> d, CoordinatesSystem cs) {
		return d.getMove().toString(cs)+": "+d.getNbLeaves();
	}
}
