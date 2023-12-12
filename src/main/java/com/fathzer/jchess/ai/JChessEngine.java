package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningSearch;
import com.fathzer.games.ai.moveSelector.RandomMoveSelector;
import com.fathzer.games.ai.moveSelector.StaticMoveSelector;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
import com.fathzer.games.util.exec.SingleThreadContext;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.evaluator.BasicMoveComparator;
import com.fathzer.jchess.fen.FENUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine extends IterativeDeepeningEngine<Move, Board<Move>> {
	private Function<Board<Move>, Move> openingLibrary;
	private Function<Board<Move>, Comparator<Move>> moveComparatorSupplier;
	private Function<Board<Move>, Evaluator<Move, Board<Move>>> evaluatorSupplier;
	
	public JChessEngine(Function<Board<Move>, Evaluator<Move, Board<Move>>> evaluatorSupplier, int maxDepth) {
		super(maxDepth, new TT(16, SizeUnit.MB));
		setDeepeningPolicy(new JChessDeepeningPolicy(maxDepth));
		moveComparatorSupplier = BasicMoveComparator::new;
		this.evaluatorSupplier = evaluatorSupplier;
		setLogger(new DefaultEventLogger());
	}
	
	/** Sets the opening library of this engine.
	 * @param openingLibrary The opening library or null to play without such library.
	 * <br>An openings library is a function that should return null if the library does not known what to play here.
	 * @return This updated chess engine
	 */
	public JChessEngine setOpenings(Function<Board<Move>, Move> openingLibrary) {
		this.openingLibrary = openingLibrary;
		return this;
	}
	
	public void setEvaluatorSupplier(Function<Board<Move>, Evaluator<Move, Board<Move>>> evaluatorSupplier) {
		this.evaluatorSupplier = evaluatorSupplier;
	}
	
	@Override
	protected ExecutionContext<SearchContext<Move, Board<Move>>> buildExecutionContext(Board<Move> board) {
		board.setMoveComparatorBuilder(moveComparatorSupplier);
		final SearchContext<Move, Board<Move>> context = SearchContextBuilder.get(evaluatorSupplier, board);
		if (getParallelism()==1) {
			return new SingleThreadContext<>(context);
		} else {
			final ContextualizedExecutor<SearchContext<Move, Board<Move>>> contextualizedExecutor = new ContextualizedExecutor<>(getParallelism());
			return new MultiThreadsContext<>(context, contextualizedExecutor);
		}
	}

	public void setMoveComparatorSupplier(Function<Board<Move>, Comparator<Move>> moveComparatorSupplier) {
		this.moveComparatorSupplier = moveComparatorSupplier;
	}
	
	public Function<Board<Move>, Comparator<Move>> getMoveComparatorSupplier() {
		return moveComparatorSupplier;
	}

	@Override
	public Move apply(Board<Move> board) {
		Move move = openingLibrary==null ? null : openingLibrary.apply(board);
		if (move==null) {
			final BasicMoveComparator c = new BasicMoveComparator(board);
			super.setMoveSelector(new LoggedSelector(board).setNext(new StaticMoveSelector<Move,IterativeDeepeningSearch<Move>>(c::getValue).setNext(new RandomMoveSelector<>())));
			final IterativeDeepeningSearch<Move> search = search(board);
			final List<EvaluatedMove<Move>> bestMoves = this.getMoveSelector().select(search, search.getBestMoves());
			final EvaluatedMove<Move> evaluatedMove = bestMoves.get(0);
			move = evaluatedMove.getContent();
			log.info("Move chosen :{}", move.toString(board.getCoordinatesSystem()));
			final List<Move> pv = evaluatedMove.getPrincipalVariation();
			log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		} else {
			log.info("Move from libray:{}", move.toString(board.getCoordinatesSystem()));
		}
		return move;
	}

	@Override
	protected IterativeDeepeningSearch<Move> search(Board<Move> board) {
		final EventLogger<Move> logger = getLogger();
		if (logger instanceof DefaultEventLogger) {
			((DefaultEventLogger)logger).cs = board.getCoordinatesSystem();
		}
		log.info("--- Start evaluation for {} with size={}, accuracy={}, maxDepth={}, maxTime={} ---", FENUtils.to(board), getDeepeningPolicy().getSize(), getDeepeningPolicy().getAccuracy(), getDeepeningPolicy().getDepth(), getDeepeningPolicy().getMaxTime());
		IterativeDeepeningSearch<Move> search = super.search(board);
		log.info("--- End of iterative evaluation returns: {}", toString(search.getBestMoves(), board.getCoordinatesSystem()));
		return search;
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

		public DefaultEventLogger() {
			super();
		}

		@Override
		public void logSearch(int depth, SearchStatistics stat, SearchResult<Move> bestMoves) {
			final long duration = stat.getDurationMs();
			final List<EvaluatedMove<Move>> cut = bestMoves.getCut();
			log.info("{} move generations, {} moves generated, {} moves played({} from TT), {} evaluations for {} first level moves at depth {} by {} threads in {}ms -> {}",
					stat.getMoveGenerationCount(), stat.getGeneratedMoveCount(), stat.getMovePlayedCount(), stat.getMoveFromTTPlayedCount(),
					stat.getEvaluationCount(), bestMoves.getList().size(),
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
}
