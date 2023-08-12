package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine extends IterativeDeepeningEngine<Move, Board<Move>> {
	private Function<Board<Move>, Move> openingLibrary;
	
	public JChessEngine(Evaluator<Board<Move>> evaluator, int maxDepth) {
		super(evaluator, maxDepth, new TT(512));
		setDeepeningPolicyBuilder(() -> new JChessDeepeningPolicy(getMaxTime()));
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
	protected ExecutionContext<Move, Board<Move>> buildExecutionContext(Board<Move> board) {
		if (getParallelism()==1) {
			return new SingleThreadContext<>(board);
		} else {
			final Supplier<Board<Move>> supplier = () -> {
				Board<Move> b = board.create();
				b.copy(board);
				return b;
			};
			return new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(getParallelism()));
		}
	}

	@Override
	protected Negamax<Move, Board<Move>> buildNegaMax(ExecutionContext<Move, Board<Move>> context, Evaluator<Board<Move>> evaluator) {
		return new Negamax<>(context, evaluator) {
			@Override
			public List<Move> sort(List<Move> moves) {
				final Board<Move> b = getGamePosition();
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
			final List<EvaluatedMove<Move>> bestMoves = getBestMoves(board);
			move = bestMoves.get(RND.nextInt(bestMoves.size())).getContent();
			log.info("Move choosed :{}", move.toString(board.getCoordinatesSystem()));
		} else {
			log.info("Move from libray:{}", move.toString(board.getCoordinatesSystem()));
		}
		return move;
	}

	@Override
	public List<EvaluatedMove<Move>> getBestMoves(Board<Move> board) {
		setLogger(getLogger(board));
		log.info("--- Start evaluation for {} with size={}, accuracy={}, maxDepth={}---", FENParser.to(board), getSearchParams().getSize(), getSearchParams().getAccuracy(), getSearchParams().getDepth());
		List<EvaluatedMove<Move>> bestMoves = super.getBestMoves(board);
		final List<Move> pv = bestMoves.get(0).getPrincipalVariation();
		log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		log.info("--- End of iterative evaluation returns: {}", toString(bestMoves, board.getCoordinatesSystem()));
		return bestMoves;
	}
	
	public static String toString(Collection<EvaluatedMove<Move>> moves, CoordinatesSystem cs) {
		return moves.stream().map(em -> toString(em,cs)).collect(Collectors.joining(", ", "[", "]"));
	}

	public static String toString(EvaluatedMove<Move> ev, CoordinatesSystem cs) {
		final Type type = ev.getEvaluation().getType();
		String value;
		if (type==Type.EVAL) {
			value = Integer.toString(ev.getScore());
		} else {
			value="M"+(ev.getEvaluation().getType()==Type.LOOSE?"-":"+")+ev.getEvaluation().getCountToEnd();
		}
		return ev.getContent().toString(cs)+"("+value+")";
	}

	private class DefaultEventLogger implements EventLogger<Move> {
		private CoordinatesSystem cs;

		public DefaultEventLogger(CoordinatesSystem cs) {
			super();
			this.cs = cs;
		}
		@Override
		public void logSearch(int depth, SearchStatistics stat, SearchResult<Move> bestMoves) {
			final long duration = stat.getDurationMs();
			final List<EvaluatedMove<Move>> cut = bestMoves.getCut();
			log.info("{} move generations, {} moves generated, {} moves played, {} evaluations for {} moves at depth {} by {} threads in {}ms -> {}",
					stat.getMoveGenerationCount(), stat.getGeneratedMoveCount(), stat.getMovePlayedCount(), stat.getEvaluationCount(), bestMoves.getList().size(),
					depth, getParallelism(), duration, cut.isEmpty()?null:cut.get(0).getEvaluation());
			log.info("Search at depth {} returns: {}", depth, JChessEngine.toString(bestMoves.getCut(),cs));
		}

		@Override
		public void logTimeOut(int depth) {
			log.info("Search interrupted by timeout at depth {}",depth);
		}

		@Override
		public void logEndedByPolicy(int depth) {
			log.info("Search ended by deepening policy at depth {}", depth);
		}
	}

	protected EventLogger<Move> getLogger(Board<Move> board) {
		return new DefaultEventLogger(board.getCoordinatesSystem());
	}
}
