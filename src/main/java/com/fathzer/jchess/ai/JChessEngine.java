package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.AbstractAI;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random(); 
	private final ChessEvaluator evaluator;
	@Setter
	private int depth;
	@Setter
	private int parallelism;
	private Collection<AbstractAI<Move>> sessions;
	private Function<Board<Move>, Move> openingLibrary;
	
	public JChessEngine(ChessEvaluator evaluator, int depth) {
		this.depth = depth;
		this.evaluator = evaluator;
		this.sessions = new LinkedList<>();
		this.parallelism = 1;
		this.openingLibrary = null;
	}
	
	/** Sets the opening library of this engine.
	 * @param openingLibrary The opening library. It returns null if the library does not known what to play here.
	 * @return This updated chess engine
	 */
	public JChessEngine setOpenings(Function<Board<Move>, Move> openingLibrary) {
		this.openingLibrary = openingLibrary;
		return this;
	}
	
	public synchronized void interrupt() {
		this.sessions.forEach(AbstractAI::interrupt);
	}

	@Override
	public Move apply(Board<Move> board) {
		Move move = openingLibrary==null ? null : openingLibrary.apply(board);
		if (move==null) {
			List<Evaluation<Move>> bestMoves = getBestMoves(board, 1, 9);
			move = bestMoves.get(RND.nextInt(bestMoves.size())).getContent();
			log.debug("Move choosed :{}",move);
		}
		return move;
	}

	public List<Evaluation<Move>> getBestMoves(Board<Move> board, int size, int accuracy) {
		final Stat stat = new Stat();
		evaluator.setViewPoint(board.getActiveColor());
		try (ContextualizedExecutor<MoveGenerator<Move>> exec = new ContextualizedExecutor<>(parallelism)) {
			final Supplier<MoveGenerator<Move>> supplier = () -> {
				Board<Move> b = board.create();
				b.copy(board);
				return new InstrumentedMoveGenerator(b, stat);
			};
			final AbstractAI<Move> internal = new Negamax<>(supplier, exec) {
				@Override
				public int evaluate() {
					return evaluator.evaluate(((InstrumentedMoveGenerator)getMoveGenerator()).getBoard());
				}
			};

			synchronized (this) {
				this.sessions.add(internal);
			}
			log.debug("--- Start evaluation for {} ---",FENParser.to(board));
			final long start = System.currentTimeMillis();
			List<Evaluation<Move>> bestMoves = internal.getBestMoves(depth, size, accuracy);
			sessions.remove(internal);
			final long duration = System.currentTimeMillis()-start;
			log.trace("{} move generations, {} moves generated, {} moves played, {} evaluations ({} duplicated) for {} moves at depth {} by {} threads in {}ms -> {}",
					stat.moveGenerations.get(), stat.generatedMoves.get(), stat.movesPlayed.get(), stat.evalCount.get(), stat.evalAgainCount, bestMoves.size(),
					depth, parallelism, duration, bestMoves.get(0).getValue());
			log.debug(bestMoves.toString());
			return bestMoves;
		}
	}
	
	private static class InstrumentedMoveGenerator implements MoveGenerator<Move> {
		@Getter
		private final Board<Move> board;
		private final Stat stat;
		private final Comparator<Move> cmp;

		public InstrumentedMoveGenerator(Board<Move> board, Stat stat) {
			this.board = board;
			this.stat = stat;
			this.cmp = new BasicMoveComparator(board);
		}

		@Override
		public void makeMove(Move move) {
			stat.movesPlayed.incrementAndGet();
			board.makeMove(move);
		}

		@Override
		public List<Move> getMoves() {
			stat.moveGenerations.incrementAndGet();
			final List<Move> moves = board.getMoves();
//TODO Incredible, sorting makes it very, very slow
//			moves.sort(cmp);
			stat.generatedMoves.addAndGet(moves.size());
			return moves;
		}

		@Override
		public void unmakeMove() {
			board.unmakeMove();
		}

		@Override
		public Status getStatus() {
			return board.getStatus();
		}
	}
	
	
	private static class Stat {
		private final AtomicLong evalCount = new AtomicLong();
		private final AtomicLong moveGenerations = new AtomicLong();
		private final AtomicLong generatedMoves = new AtomicLong();
		private final AtomicLong movesPlayed = new AtomicLong();
		private long evalAgainCount = 0;
//		private Set<String> previous = new HashSet<>();
	}
}
