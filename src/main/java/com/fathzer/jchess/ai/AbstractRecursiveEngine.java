package com.fathzer.jchess.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.transposition.TranspositionTable;
import com.fathzer.games.util.Evaluation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRecursiveEngine<M, B extends MoveGenerator<M>> implements Function<B, M> {
	private static final Random RND = new Random(); 
	protected final Evaluator<B> evaluator;
	@Setter
	protected int maxDepth;
	@Setter
	protected long maxTime = Long.MAX_VALUE;
	@Getter
	private TranspositionTable<M> transpositionTable;
	@Setter
	@Getter
	private int parallelism;
	
	protected AbstractRecursiveEngine(Evaluator<B> evaluator, int maxDepth, TranspositionTable<M> tt) {
		this.parallelism = 1;
		this.maxDepth = maxDepth;
		this.evaluator = evaluator;
		this.transpositionTable = tt;
	}
	
	public synchronized void interrupt() {
		//FIXME
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public M apply(B board) {
		// FIXME accuracy should be a parameter
		final List<Evaluation<M>> bestMoves = getBestMoves(board, 1, 9);
		return bestMoves.get(RND.nextInt(bestMoves.size())).getContent();
	}

	public List<Evaluation<M>> getBestMoves(B board, int size, int accuracy) {
		final long start = System.currentTimeMillis();
		setViewPoint(evaluator, board);
		// TODO Test if it is really a new position
		transpositionTable.newPosition();
		SearchResult<M> bestMoves;
		try (ExecutionContext<M> context = buildExecutionContext(board)) {
			final Negamax<M> internal = buildNegaMax(context);
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
			final List<Evaluation<M>> ended = new ArrayList<>(bestMoves.getList().size());
			for (int depth=initalDepth+2; depth<=maxDepth;depth=depth+2) {
				// Re-use best moves order to speedup next search
				final List<M> moves = getMovesToDeepen(bestMoves.getList(), ended);
				if (!moves.isEmpty()) {
					final SearchResult<M> deeper = doDepth(board, moves, size, accuracy, internal, depth);
					if (!internal.isInterrupted()) {
						bestMoves = deeper;
					} else {
						for (Evaluation<M> ev:deeper.getList()) {
							bestMoves.update(ev.getContent(), ev.getValue());
						}
					}
				}
				if (internal.isInterrupted() || moves.isEmpty()) {
					break;
				}
			}
			timer.cancel();
			final List<Evaluation<M>> result = bestMoves.getCut();
			result.addAll(ended);
			return result;
		}
	}
	
	private List<M> getMovesToDeepen(List<Evaluation<M>> evaluations, List<Evaluation<M>> ended) {
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
	
	private boolean isEndOfGame(Evaluation<M> mv) {
		return evaluator.getNbMovesToWin(mv.getValue()) <= maxDepth;
	}

	protected SearchResult<M> doDepth(B board, List<M> moves, int size, int accuracy, final Negamax<M> ai, int depth) {
		ai.getStatistics().clear();
		return moves==null ? ai.getBestMoves(depth, size, accuracy) : ai.getBestMoves(moves, depth, size, accuracy);
	}
	
	protected abstract ExecutionContext<M> buildExecutionContext(B board);
	
	protected abstract Negamax<M> buildNegaMax(ExecutionContext<M> context);
	
	protected abstract void setViewPoint(Evaluator<B> evaluator, B board);
}
