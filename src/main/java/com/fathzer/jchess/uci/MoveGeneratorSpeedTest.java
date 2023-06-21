package com.fathzer.jchess.uci;

import java.util.List;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.perft.PerfT;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.games.util.ContextualizedExecutor;

class MoveGeneratorSpeedTest {
	private List<PerfTTestData> tests;
	private boolean cancelled;
	
	protected MoveGeneratorSpeedTest(List<PerfTTestData> tests) {
		this.tests = tests;
	}
	
	public <M> long run(int depth, int parallelism, Engine engine) {
		this.cancelled = false;
		long count = 0;
		try (ContextualizedExecutor<MoveGenerator<M>> threads = new ContextualizedExecutor<>(parallelism)) {
			for (PerfTTestData test : tests) {
				if (test.getSize()>=depth) {
					try {
						engine.setFEN(test.getStartPosition()+" 0 1");
						// TODO Ugly cast
						count += doTest(test, depth, threads, (Supplier<MoveGenerator<M>>)engine);
					} catch (RuntimeException e) {
						System.out.println("Exception for "+test.getStartPosition());
						e.printStackTrace();
						cancelled = true;
					}
				}
				if (cancelled) {
					break;
				}
			}
		}
		return count;
	}
	
	
	private <M> long doTest(PerfTTestData test, int depth, ContextualizedExecutor<MoveGenerator<M>> threads, Supplier<MoveGenerator<M>> moveGeneratorSupplier) {
		try {
			final long count = new PerfT<>(threads).divide(depth, moveGeneratorSupplier).getNbLeaves();
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
	
	public void cancel() {
		this.cancelled = true;
	}
}
