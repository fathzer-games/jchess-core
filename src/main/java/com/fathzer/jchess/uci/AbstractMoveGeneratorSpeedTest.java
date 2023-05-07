package com.fathzer.jchess.uci;

import java.util.List;

import com.fathzer.games.perft.PerfT;
import com.fathzer.games.perft.PerfTTestData;

class AbstractMoveGeneratorSpeedTest {
	private List<PerfTTestData> tests;
	private boolean cancelled;

	protected AbstractMoveGeneratorSpeedTest(List<PerfTTestData> tests) {
		this.tests = tests;
	}
	
	public long run(int depth, int parallelism, Engine engine) {
		this.cancelled = false;
		long count = 0;
		for (PerfTTestData test : tests) {
			count += doTest(test, depth, parallelism, engine);
			if (cancelled) {
				break;
			}
		}
		return count;
	}
	
	
	private long doTest(PerfTTestData test, int depth, int parallelism, Engine engine) {
		if (test.getSize()>=depth) {
			try {
				engine.setFEN(test.getStartPosition()+" 0 1");
				final long count = getCount(depth, parallelism, (UCIMoveGeneratorProvider<?>) engine);
				if (count != test.getCount(depth)) {
					System.out.println("Error for "+test.getStartPosition()+" expected "+test.getCount(depth)+" got "+count);
//				} else {
//					System.out.println("Ok for "+test.getFen());
				}
				return count;
			} catch (RuntimeException e) {
				System.out.println("Exception for "+test.getStartPosition());
				throw e;
			}
		}
		return 0;
	}
	
	private <M> long getCount(int depth, int parallelism, UCIMoveGeneratorProvider<M> engine) {
		final PerfT<M> perft = new PerfT<>(engine::getMoveGenerator);
		perft.setParallelism(parallelism);
		return perft.divide(depth).getNbLeaves();
	}
	
	public void cancel() {
		this.cancelled = true;
	}
}
