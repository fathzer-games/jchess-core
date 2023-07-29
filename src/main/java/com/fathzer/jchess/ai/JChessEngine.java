package com.fathzer.jchess.ai;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.GamePosition;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.games.ai.transposition.TranspositionTable;
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
	protected final ChessEvaluator evaluator;
	@Setter
	protected int depth;
	protected Collection<AI<Move>> sessions;
	private Function<Board<Move>, Move> openingLibrary;
	@Getter
	private TranspositionTable<Move> transpositionTable;
	@Setter
	@Getter
	private int parallelism;
	
	public JChessEngine(ChessEvaluator evaluator, int depth) {
		this.parallelism = 1;
		this.depth = depth;
		this.evaluator = evaluator;
		this.sessions = new LinkedList<>();
		this.openingLibrary = null;
		this.transpositionTable = new TT(512);
		//TODO this.transpositionTable = new com.fathzer.games.ai.transposition.FakeTranspositionTable();
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
		final Stat stat = new Stat();
		evaluator.setViewPoint(board.getActiveColor());
// TODO Test if it is really a new position
		transpositionTable.newPosition();
		log.info("--- Start evaluation for {} ---",FENParser.to(board));
		final long start = System.currentTimeMillis();
		final List<Evaluation<Move>> bestMoves = getBestMoves(board, size, accuracy, stat);
		final long duration = System.currentTimeMillis()-start;
		List<Move> pv = transpositionTable.collectPV(board, depth);
		log.info("{} move generations, {} moves generated, {} moves played, {} evaluations for {} moves at depth {} by {} threads in {}ms -> {}",
				stat.moveGenerations.get(), stat.generatedMoves.get(), stat.movesPlayed.get(), stat.evalCount.get(), bestMoves.size(),
				depth, getParallelism(), duration, bestMoves.get(0).getValue());
		log.info("pv: {}", pv.stream().map(m -> m.toString(board.getCoordinatesSystem())).collect(Collectors.toList()));
		log.info(Evaluation.toString(bestMoves, m -> m.toString(board.getCoordinatesSystem())));
		return bestMoves;
	}

	private List<Evaluation<Move>> getBestMoves(Board<Move> board, int size, int accuracy, Stat stat) {
		try (ExecutionContext<Move> context = buildExecutionContext(board, stat)) {
			final Negamax<Move> internal = new Negamax<>(context);
			internal.setTranspositonTable(transpositionTable);
			return doSession(internal, depth, size, accuracy);
		}
	}
	
	private ExecutionContext<Move> buildExecutionContext(Board<Move> board, Stat stat) {
		if (parallelism==1) {
			return new SingleThreadContext<>(new InstrumentedMoveGenerator(board, evaluator, stat));
		} else {
			final Supplier<GamePosition<Move>> supplier = () -> {
				Board<Move> b = board.create();
				b.copy(board);
				return new InstrumentedMoveGenerator(b, evaluator, stat);
			};
			return new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(parallelism));
		}
	}
	
	protected List<Evaluation<Move>> doSession(AI<Move> ai, int depth, int size, int accuracy) {
		synchronized (this) {
			this.sessions.add(ai);
		}
		try {
			return ai.getBestMoves(depth, size, accuracy);
		} finally {
			sessions.remove(ai);
		}
	}
}
