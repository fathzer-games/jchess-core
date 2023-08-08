package com.fathzer.jchess.ai;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.Evaluator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.games.ai.experimental.Negamax3;
import com.fathzer.games.ai.recursive.AbstractRecursiveEngine;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine extends AbstractRecursiveEngine<Move, Board<Move>> {
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
		Negamax3<Move, Board<Move>> n= new Negamax3<>(context, evaluator) {
			@Override
			public List<Move> sort(List<Move> moves) {
				final Board<Move> b = getGamePosition();
				final Comparator<Move> cmp = new BasicMoveComparator(b);
				moves.sort(cmp);
				return moves;
			}
		};
		n.spy = new NegaMaxSpy(3420615620120923866L);
		return n;
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
		setLogger(getLogger(board));
		log.info("--- Start evaluation for {} with size={}, accuracy={}, maxDepth={}---", FENParser.to(board), getSearchParams().getSize(), getSearchParams().getAccuracy(), getSearchParams().getDepth());
		List<Evaluation<Move>> bestMoves = super.getBestMoves(board);
		final List<Move> pv = bestMoves.get(0).getPrincipalVariation();
		log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		showKeys(board, pv);//TODO
		return bestMoves;
	}
	
	private void showKeys(Board<Move> board, List<Move> moves) { //TODO
		for (Move mv : moves) {
			System.out.println (board.getHashKey()+" -> mv: "+mv.toString(board.getCoordinatesSystem()));
			board.makeMove(mv);
		}
		for (Move mv : moves) {
			board.unmakeMove();
		}
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
			final List<Evaluation<Move>> cut = bestMoves.getCut();
			log.info("{} move generations, {} moves generated, {} moves played, {} evaluations for {} moves at depth {} by {} threads in {}ms -> {}",
					stat.getMoveGenerationCount(), stat.getGeneratedMoveCount(), stat.getMovePlayedCount(), stat.getEvaluationCount(), bestMoves.getList().size(),
					depth, getParallelism(), duration, cut.isEmpty()?null:cut.get(0).getValue());
			log.info(Evaluation.toString(bestMoves.getCut(), m -> m.toString(cs)));
			log.info(toString(bestMoves.getCut().get(0), cs));
		}

		@Override
		public void logTimeOut(int depth) {
			log.info("Search interrupted by timeout at depth {}",depth);
		}

		@Override
		public void logEndDetected(int depth) {
			log.info("Search ended by imminent win/lose detection at depth {}", depth);
		}
		
		private String toString(Evaluation<Move> ev, CoordinatesSystem cs) {
			int matCount = getEvaluator().getNbHalfMovesToWin(ev.getValue());
			String value;
			if (matCount<=getSearchParams().getDepth()) {
				value="M"+(ev.getValue()<0?"-":"+")+(matCount+1)/2;
			} else {
				value = Integer.toString(ev.getValue());
			}
			return ev.getContent().toString(cs)+"("+value+")";
		}
	}

	protected EventLogger<Move> getLogger(Board<Move> board) {
		return new DefaultEventLogger(board.getCoordinatesSystem());
	}
}
