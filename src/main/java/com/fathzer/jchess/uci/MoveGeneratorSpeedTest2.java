package com.fathzer.jchess.uci;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.perft.Divide;
import com.fathzer.games.perft.PerfT;
import com.fathzer.games.perft.PerfTResult;
import com.fathzer.games.perft.PerfTTestData;

class MoveGeneratorSpeedTest2 {
	private List<PerfTTestData> tests;
	private boolean cancelled;
	
	private static class ContextThread<T> extends Thread {
		private T context;

		public ContextThread(Runnable target) {
			super(target);
		}
	}
	
	public static class ThreadedMoveGenerators<T> implements Closeable {
		private final ExecutorService exec;
		private final List<ContextThread<T>> threads;
		private Supplier<T> contextSupplier;
		
		public ThreadedMoveGenerators(int parallelism) {
			this.threads = new LinkedList<>();
			this.exec = Executors.newFixedThreadPool(parallelism, r -> {
				final ContextThread<T> contextThread = new ContextThread<>(r);
				contextThread.context = contextSupplier.get();
				threads.add(contextThread);
				return contextThread;
			});
		}
		
		public void setContext(Supplier<T> contextSupplier) {
			this.contextSupplier = contextSupplier;
			threads.forEach(t -> t.context = contextSupplier.get());
		}

		public <V> List<Future<V>> invokeAll(List<Callable<V>> tasks) throws InterruptedException {
			return exec.invokeAll(tasks);
		}

		public T getMoveGenerator() {
			return ((ContextThread<T>)Thread.currentThread()).context;
		}

		@Override
		public void close() {
			exec.shutdown();
			System.out.println("ThreadGenerators shutdown");
		}
	}
	
	private static class PerfT<M> {
		private boolean playLeaves;
		private boolean interrupted;
		private ThreadedMoveGenerators<MoveGenerator<M>> exec;
		
		public PerfT(ThreadedMoveGenerators<MoveGenerator<M>> exec) {
			this.exec = exec;
			this.playLeaves = false;
		}
		
		public boolean isPlayLeaves() {
			return playLeaves;
		}

		public void setPlayLeaves(boolean playLeaves) {
			this.playLeaves = playLeaves;
		}
		
		public PerfTResult<M> divide(final int depth, MoveGenerator<M> generator) {
			if (depth <= 0) {
	            throw new IllegalArgumentException("Search depth MUST be > 0");
			}
			final GameState<M> moves = generator.getState();
			final PerfTResult<M> result = new PerfTResult<>();
			result.addMovesFound(moves.size());
	        final IntStream stream = IntStream.range(0, moves.size());
			List<Callable<Divide<M>>> tasks = stream.mapToObj(m -> 
				new Callable<Divide<M>>() {
					@Override
					public Divide<M> call() throws Exception {
						return getPrfT(moves.get(m), depth - 1, result);
					}
				
			}).collect(Collectors.toList());
			try {
				final List<Future<Divide<M>>> results = exec.invokeAll(tasks);
				for (Future<Divide<M>> f : results) {
					result.add(f.get());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				result.setInterrupted(true);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
			return result;
		}
		
		private Divide<M> getPrfT(M move, int depth, PerfTResult<M> result) {
			final long leaves;
			if (depth==0 && !playLeaves) {
				leaves = 1;
			} else {
				final MoveGenerator<M> moveGenerator = exec.getMoveGenerator();
				moveGenerator.makeMove(move);
				result.addMoveMade();
				leaves = get(depth, result);
				moveGenerator.unmakeMove();
			}
			return new Divide<>(move, leaves);
		}
		
	    private long get (final int depth, PerfTResult<M> result) {
			if (isInterrupted()) {
				result.setInterrupted(true);
				return 1;
			}
	    	if (depth==0) {
	    		return 1;
	    	}
	    	final MoveGenerator<M> generator = exec.getMoveGenerator();
			final GameState<M> state = generator.getState();
			result.addMovesFound(state.size());
			if (depth==1 && !playLeaves) {
				return state.size();
			}
			long count = 0;
			for (int i = 0; i < state.size(); i++) {
	            generator.makeMove(state.get(i));
	            result.addMoveMade();
	            count += get(depth-1, result);
	            generator.unmakeMove();
			}
	        return count;
	    }
		
		public boolean isInterrupted() {
			return interrupted;
		}
	}
	
	protected MoveGeneratorSpeedTest2(List<PerfTTestData> tests) {
		this.tests = tests;
	}
	
	public <M> long run(int depth, int parallelism, Engine engine) {
		this.cancelled = false;
		long count = 0;
		try (ThreadedMoveGenerators<MoveGenerator<M>> threads = new ThreadedMoveGenerators<>(parallelism)) {
			for (PerfTTestData test : tests) {
				if (test.getSize()>=depth) {
					try {
						engine.setFEN(test.getStartPosition()+" 0 1");
						// TODO Ugly cast
						threads.setContext(() -> ((UCIMoveGeneratorProvider<M>)engine).getMoveGenerator());
						count += doTest(test, depth, threads, ((UCIMoveGeneratorProvider<M>)engine).getMoveGenerator());
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
	
	
	private <M> long doTest(PerfTTestData test, int depth, ThreadedMoveGenerators<MoveGenerator<M>> threads, MoveGenerator<M> moveGenerator) {
		try {
			final long count = new PerfT<>(threads).divide(depth, moveGenerator).getNbLeaves();
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
		//TODO Do something to stop running PerfT
	}
}
