package com.fathzer.jchess.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.GamePosition;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.SearchStatistics;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.BasicGamePosition.LongBuilder;
import com.fathzer.jchess.fen.FENParser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JChessEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random(); 
	protected final Evaluator<Board<Move>> evaluator;
	@Setter
	protected int maxDepth;
	@Setter
	protected long maxTime = Long.MAX_VALUE;
	protected Collection<AI<Move>> sessions;
	private Function<Board<Move>, Move> openingLibrary;
	@Getter
	private TranspositionTable<Move> transpositionTable;
	@Setter
	@Getter
	private int parallelism;
	
	public JChessEngine(Evaluator<Board<Move>> evaluator, int maxDepth) {
		this.parallelism = 1;
		this.maxDepth = maxDepth;
		this.evaluator = evaluator;
		this.sessions = new LinkedList<>();
		this.openingLibrary = null;
		this.transpositionTable = new TT(512);
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
		this.sessions.forEach(AI::interrupt);
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
		final long start = System.currentTimeMillis();
		evaluator.setViewPoint(board.getActiveColor());
// TODO Test if it is really a new position
		transpositionTable.newPosition();
		SearchResult<Move> bestMoves;
		log.info("--- Start evaluation at for {} with size={} and accuracy={}---", FENParser.to(board), size, accuracy);
		try (ExecutionContext<Move> context = buildExecutionContext(board)) {
			final Negamax<Move> internal = new Negamax<>(context) {
				@Override
				public List<Move> sort(List<Move> moves) {
					final Board<Move> b = ((BasicGamePosition<Move, Board<Move>>)getGamePosition()).getBoard();
					final Comparator<Move> cmp = new BasicMoveComparator(b);
					moves.sort(cmp);
					return moves;
				}
			};
			internal.setTranspositonTable(transpositionTable);
			final int initalDepth = 2;
			bestMoves = doDepth(board, null, size, accuracy, internal, initalDepth);
			final Timer timer = new Timer(true);
			if (maxTime!=Long.MAX_VALUE) {
				timer.schedule(new TimerTask(){
					@Override
					public void run() {
						internal.interrupt();
						log.info("Search interrupted by timeout");
					}
				}, maxTime-(System.currentTimeMillis()-start));
			}
			final List<Evaluation<Move>> ended = new ArrayList<>(bestMoves.getList().size());
			for (int depth=initalDepth+2; depth<=maxDepth;depth=depth+2) {
				// Re-use best moves order to speedup next search
				final List<Move> moves = getMovesToDeepen(bestMoves.getList(), ended);
				if (!moves.isEmpty()) {
					final SearchResult<Move> deeper = doDepth(board, moves, size, accuracy, internal, depth);
					if (!internal.isInterrupted()) {
						bestMoves = deeper;
					} else {
						for (Evaluation<Move> ev:deeper.getList()) {
							bestMoves.update(ev.getContent(), ev.getValue());
						}
					}
				}
				if (internal.isInterrupted() || moves.isEmpty()) {
					break;
				}
			}
			timer.cancel();
			final List<Move> pv = transpositionTable.collectPV(board, bestMoves.getList().get(0).getContent(), maxDepth);
			log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
			final List<Evaluation<Move>> result = bestMoves.getCut();
			result.addAll(ended);
			return result;
		}
	}
	
	private <M> List<M> getMovesToDeepen(List<Evaluation<M>> evaluations, List<Evaluation<M>> ended) {
		if (isEndOfGame(evaluations.get(0))) {
			// if best move is a win/loose, continuing analysis is useless
			log.info("Search ended by imminent win/lose detection");
			return Collections.emptyList();
		}
		// Separate move that leads to loose (put in finished). These moves do not need to be deepened. Store others in toDeepen
		// We don't put 'finished' moves in ended directly to preserve the evaluation order 
		final List<M> toDeepen = new ArrayList<>(evaluations.size());
		final List<Evaluation<M>> finished = new ArrayList<>();
		evaluations.stream().forEach(e -> {
			if (isEndOfGame(e)) {
				finished.add(e);
			} else {
				toDeepen.add(e.getContent());
			}
		});
		ended.addAll(0, finished);
		return toDeepen;
	}
	
	private <M> boolean isEndOfGame(Evaluation<M> mv) {
		return evaluator.getNbMovesToWin(mv.getValue()) <= maxDepth;
	}

	private SearchResult<Move> doDepth(Board<Move> board, List<Move> moves, int size, int accuracy, final Negamax<Move> internal, int depth) {
		final SearchStatistics stat = internal.getStatistics();
		stat.clear();
		final SearchResult<Move> bestMoves = doSession(internal, moves, depth, size, accuracy);
		final long duration = stat.getDurationMs();
		final List<Evaluation<Move>> cut = bestMoves.getCut();
		log.info("{} move generations, {} moves generated, {} moves played, {} evaluations for {} moves at depth {} by {} threads in {}ms{} -> {}",
				stat.getMoveGenerationCount(), stat.getGeneratedMoveCount(), stat.getMovePlayedCount(), stat.getEvaluationCount(), bestMoves.getList().size(),
				depth, getParallelism(), duration, internal.isInterrupted()?"(search interrupted)":"", cut.isEmpty()?null:cut.get(0).getValue());
		log.info(Evaluation.toString(bestMoves.getCut(), m -> m.toString(board.getCoordinatesSystem())));
		return bestMoves;
	}
	
	private ExecutionContext<Move> buildExecutionContext(Board<Move> board) {
		final LongBuilder<Board<Move>> hash = b->b.getHashKey();
		if (parallelism==1) {
			return new SingleThreadContext<>(new BasicGamePosition<>(board, evaluator, hash));
		} else {
			final Supplier<GamePosition<Move>> supplier = () -> {
				Board<Move> b = board.create();
				b.copy(board);
				return new BasicGamePosition<>(b, evaluator, hash);
			};
			return new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(parallelism));
		}
	}
	
	private SearchResult<Move> doSession(AI<Move> ai, List<Move> moves, int depth, int size, int accuracy) {
		synchronized (this) {
			this.sessions.add(ai);
		}
		try {
			return moves==null ? ai.getBestMoves(depth, size, accuracy) : ai.getBestMoves(moves, depth, size, accuracy);
		} finally {
			sessions.remove(ai);
		}
	}
}
