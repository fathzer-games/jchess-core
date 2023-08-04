package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.GamePosition;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.games.ai.recursive.AbstractRecursiveEngine;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.BasicGamePosition.LongBuilder;
import com.fathzer.jchess.fen.FENParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine extends AbstractRecursiveEngine<Move, Board<Move>> {
	protected Collection<AI<Move>> sessions;
	private Function<Board<Move>, Move> openingLibrary;
	
	public JChessEngine(Evaluator<Board<Move>> evaluator, int maxDepth) {
		super(evaluator, maxDepth, new TT(512));
	}
	
	/** Sets the opening library of this engine.
	 * @param openingLibrary The opening library. It returns null if the library does not known what to play here.
	 * @return This updated chess engine
	 */
	public JChessEngine setOpenings(Function<Board<Move>, Move> openingLibrary) {
		this.openingLibrary = openingLibrary;
		return this;
	}
	
	@Override
	public synchronized void interrupt() {
		this.sessions.forEach(AI::interrupt);
	}
	
	@Override
	protected ExecutionContext<Move> buildExecutionContext(Board<Move> board) {
		final LongBuilder<Board<Move>> hash = b->b.getHashKey();
		if (getParallelism()==1) {
			return new SingleThreadContext<>(new BasicGamePosition<>(board, getEvaluator(), hash));
		} else {
			final Supplier<GamePosition<Move>> supplier = () -> {
				Board<Move> b = board.create();
				b.copy(board);
				return new BasicGamePosition<>(b, getEvaluator(), hash);
			};
			return new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(getParallelism()));
		}
	}

	@Override
	protected Negamax<Move> buildNegaMax(ExecutionContext<Move> context) {
		return new Negamax<>(context) {
			@Override
			public List<Move> sort(List<Move> moves) {
				final Board<Move> b = ((BasicGamePosition<Move, Board<Move>>)getGamePosition()).getBoard();
				final Comparator<Move> cmp = new BasicMoveComparator(b);
				moves.sort(cmp);
				return moves;
			}
		};
	}

	@Override
	protected void setViewPoint(Evaluator<Board<Move>> evaluator, Board<Move> board) {
		evaluator.setViewPoint(board.getActiveColor());
	}

	@Override
	public Move apply(Board<Move> board) {
		Move move = openingLibrary==null ? null : openingLibrary.apply(board);
		if (move==null) {
			move = super.apply(board);
			log.debug("Move choosed :{}",move);
		}
		return move;
	}

	@Override
	public List<Evaluation<Move>> getBestMoves(Board<Move> board) {
		setLogger(new EventLogger<>() {
			@Override
			public void logSearch(int depth, SearchStatistics stat, SearchResult<Move> bestMoves) {
				final long duration = stat.getDurationMs();
				final List<Evaluation<Move>> cut = bestMoves.getCut();
				log.info("{} move generations, {} moves generated, {} moves played, {} evaluations for {} moves at depth {} by {} threads in {}ms -> {}",
						stat.getMoveGenerationCount(), stat.getGeneratedMoveCount(), stat.getMovePlayedCount(), stat.getEvaluationCount(), bestMoves.getList().size(),
						depth, getParallelism(), duration, cut.isEmpty()?null:cut.get(0).getValue());
				log.info(Evaluation.toString(bestMoves.getCut(), m -> m.toString(board.getCoordinatesSystem())));
			}

			@Override
			public void logTimeOut(int depth) {
				log.info("Search interrupted by timeout at depth {}",depth);
			}

			@Override
			public void logEndDetected(int depth) {
				log.info("Search ended by imminent win/lose detection at depth {}", depth);
			}
		});
		log.info("--- Start evaluation at for {} with size={} and accuracy={}---", FENParser.to(board), getSearchParams().getSize(), getSearchParams().getAccuracy());
		List<Evaluation<Move>> bestMoves = super.getBestMoves(board);
		final List<Move> pv = getTranspositionTable().collectPV(board, bestMoves.get(0).getContent(), getSearchParams().getDepth());
		log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		return bestMoves;
	}


	
//	private SearchResult<Move> doSession(AI<Move> ai, List<Move> moves, int depth, int size, int accuracy) {
//		synchronized (this) {
//			this.sessions.add(ai);
//		}
//		try {
//			return moves==null ? ai.getBestMoves(depth, size, accuracy) : ai.getBestMoves(moves, depth, size, accuracy);
//		} finally {
//			sessions.remove(ai);
//		}
//	}
}
