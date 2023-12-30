package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningSearch;
import com.fathzer.games.ai.moveselector.RandomMoveSelector;
import com.fathzer.games.ai.moveselector.StaticMoveSelector;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.util.SelectiveComparator;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.evaluator.BasicMoveComparator;
import com.fathzer.jchess.fen.FENUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine extends IterativeDeepeningEngine<Move, Board<Move>> {
	private Function<Board<Move>, SelectiveComparator<Move>> moveComparatorSupplier;
	
	public JChessEngine(Supplier<Evaluator<Move, Board<Move>>> evaluatorSupplier, int maxDepth) {
		super(maxDepth, new TT(16, SizeUnit.MB), evaluatorSupplier);
		setDeepeningPolicy(new JChessDeepeningPolicy(maxDepth));
		moveComparatorSupplier = BasicMoveComparator::new;
		setMoveSelectorBuilder(b -> {
			final BasicMoveComparator c = new BasicMoveComparator(b);
			final RandomMoveSelector<Move, IterativeDeepeningSearch<Move>> rnd = new RandomMoveSelector<>();
			final StaticMoveSelector<Move, IterativeDeepeningSearch<Move>> stmv = new StaticMoveSelector<>(c::evaluate);
			return new LoggedSelector(b).setNext(stmv.setNext(rnd));
		});
		setLogger(new DefaultEventLogger());
	}
	
	public void setMoveComparatorSupplier(Function<Board<Move>, SelectiveComparator<Move>> moveComparatorSupplier) {
		this.moveComparatorSupplier = moveComparatorSupplier;
	}
	
	public Function<Board<Move>, SelectiveComparator<Move>> getMoveComparatorSupplier() {
		return moveComparatorSupplier;
	}

	@Override
	protected ExecutionContext<SearchContext<Move, Board<Move>>> buildExecutionContext(Board<Move> board) {
		board.setMoveComparatorBuilder(moveComparatorSupplier); //TODO Not the right place?
		return super.buildExecutionContext(board);
	}

	@Override
	protected IterativeDeepeningSearch<Move> search(Board<Move> board) {
		final EventLogger<Move, Board<Move>> logger = getLogger();
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

	private class DefaultEventLogger implements EventLogger<Move, Board<Move>> {
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

		@Override
		public void logLibraryMove(Board<Move> board, Move move) {
			log.info("Move from libray:{}", move.toString(board.getCoordinatesSystem()));
		}

		@Override
		public void logMoveChosen(Board<Move> board, EvaluatedMove<Move> evaluatedMove) {
			log.info("Move chosen :{}", evaluatedMove.getContent().toString(board.getCoordinatesSystem()));
			final List<Move> pv = evaluatedMove.getPrincipalVariation();
			log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		}
	}
}
