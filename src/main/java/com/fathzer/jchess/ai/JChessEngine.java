package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Rules;
import com.fathzer.games.ai.AbstractAI;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.CopyBasedMoveGenerator;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.lichess.DefaultOpenings;
import com.fathzer.jchess.standard.CompactMoveList;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random(); 
	private final ChessEvaluator evaluator;
	private final ChessRules rules;
	@Setter
	private int depth;
	@Setter
	private int parallelism;
	private Collection<AbstractAI<Move>> sessions;
	private Function<Board<Move>, Move> openingLibrary;
	
	public JChessEngine(ChessRules rules, ChessEvaluator evaluator, int depth) {
		this.depth = depth;
		this.rules = rules;
		this.evaluator = evaluator;
		this.sessions = new LinkedList<>();
		this.parallelism = 0;
		this.openingLibrary = DefaultOpenings.INSTANCE;
	}
	
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
//		evaluator.setViewPoint(null);
//		final AbstractAI<Move> internal = new AlphaBeta<>() {
//			@Override
//			public GameContext<Move> buildContext() {
//				return new Context(rules, evaluator::evaluate, board, stat);
//			}
//		};
		evaluator.setViewPoint(board.getActiveColor());
		final AbstractAI<Move> internal = new Negamax<>() {
			@Override
			public MoveGenerator<Move> buildMoveGenerator() {
				return new InstrumentedMoveGenerator(rules, board, stat);
			}
			@Override
			public int evaluate() {
				return evaluator.evaluate(((CopyBasedMoveGenerator<Move>)getMoveGenerator()).getBoard());
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
				depth, internal.getParallelism(), duration, bestMoves.get(0).getValue());
		log.debug(bestMoves.toString());
		return bestMoves;
	}
	
	private static class InstrumentedMoveGenerator extends CopyBasedMoveGenerator<Move> {
		private Stat stat;

		public InstrumentedMoveGenerator(Rules<Board<Move>, Move> rules, Board<Move> board, Stat stat) {
			super(rules, board);
			this.stat = stat;
		}

		@Override
		public void makeMove(Move move) {
			stat.movesPlayed.incrementAndGet();
			super.makeMove(move);
		}

		@Override
		public GameState<Move> getState() {
			stat.moveGenerations.incrementAndGet();
			final GameState<Move> state = super.getState();
			if (state instanceof CompactMoveList) {
				state.sort(i -> ((CompactMoveList)state).fastEvaluate(i, getBoard()));
			}
			stat.generatedMoves.addAndGet(state.size());
			return state;
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
