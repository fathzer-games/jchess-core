package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.AlphaBetaState;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.ai.experimental.Negamax3;
import com.fathzer.games.ai.experimental.Spy;
import com.fathzer.games.ai.experimental.TreeSearchStateStack;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.SingleThreadContext;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.ai.evaluator.BasicEvaluator;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;

class MinimaxEngineTest implements MoveBuilder {
	
	@Test
	void blackPlayingTest() {
		final JChessEngine mme4 = new JChessEngine(BasicEvaluator::new, 3);
		mme4.getDeepeningPolicy().setSize(Integer.MAX_VALUE);
		final Board<Move> board = FENUtils.from("7k/5p1Q/5P1N/5PPK/6PP/8/8/8 b - - 6 5");
		final List<EvaluatedMove<Move>> moves = mme4.getBestMoves(board);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
show(moves, cs);
		assertEquals(1, moves.size());
		assertEquals("h8", cs.getAlgebraicNotation(moves.get(0).getContent().getFrom()));
		assertEquals("h7", cs.getAlgebraicNotation(moves.get(0).getContent().getTo()));
		assertEquals(-800, moves.get(0).getScore());
	}
	
	private void show(Collection<EvaluatedMove<Move>> moves, CoordinatesSystem cs) {
		System.out.println(EvaluatedMove.toString(moves, m -> m.toString(cs)));
	}
	
	@Test
	void test() {
		List<EvaluatedMove<Move>> moves;
		final JChessEngine mme4 = new JChessEngine(BasicEvaluator::new, 4);
		mme4.getDeepeningPolicy().setSize(Integer.MAX_VALUE);
		
		// 3 possible Mats in 1 with whites
		Board<Move> board = FENUtils.from("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		moves = mme4.getBestMoves(board);
show(moves, cs);
		assertEquals(6, moves.size());
		{
			final Evaluation max = moves.get(0).getEvaluation();
			assertEquals(Type.WIN, max.getType());
			assertEquals(1, max.getCountToEnd());
			assertTrue(moves.get(3).getEvaluation().compareTo(max)<0);
			moves.stream().limit(3).forEach(m -> assertEquals(max, m.getEvaluation()));
		}
//fail("enough!");

		// Mat in 1 with blacks
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENUtils.from("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5"));
show(moves, cs);
		assertEquals(7, moves.size());
		Evaluation max = moves.get(0).getEvaluation();
		assertEquals(Type.WIN, max.getType());
		assertEquals(1, max.getCountToEnd());
		Move mv = moves.get(0).getContent();
		assertEquals("c3", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("c2", cs.getAlgebraicNotation(mv.getTo()));
		assertTrue(moves.get(1).getScore()<10000.0);
		
		// Check in 2
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENUtils.from("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1"));
show(moves, cs);
		max = moves.get(0).getEvaluation();
		assertEquals(Type.WIN, max.getType());
		assertEquals(2, max.getCountToEnd());
		assertTrue(moves.get(1).getScore()<max.getScore());
		mv = moves.get(0).getContent();
		assertEquals("b3", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("a1", cs.getAlgebraicNotation(mv.getTo()));
		
		// Check in 2 with blacks
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENUtils.from("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1"));
show(moves, cs);
		max = moves.get(0).getEvaluation();
		assertEquals(Type.WIN, max.getType());
		assertEquals(2, max.getCountToEnd());
		assertTrue(moves.get(1).getScore()<max.getScore());
		assertEquals("g6", cs.getAlgebraicNotation(moves.get(0).getContent().getFrom()));
		assertEquals("h8", cs.getAlgebraicNotation(moves.get(0).getContent().getTo()));
		
		
		// Check in 3
		System.out.println("------------------");
		JChessEngine engine = new JChessEngine(BasicEvaluator::new, 6);
		engine.getDeepeningPolicy().setSize(3);
		engine.getDeepeningPolicy().setAccuracy(100);
		board = FENUtils.from("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1");
		moves = engine.getBestMoves(board);
show(moves,cs);
assertEquals(19, moves.size());
		mv = moves.get(0).getContent();
		assertEquals("d1", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("d7", cs.getAlgebraicNotation(mv.getTo()));
	}
	
	@Test
	void moreTests() {
		final Board<Move> board = FENUtils.from("8/8/8/3kr3/8/8/5PPP/7K w - - 0 1");
		final SearchContext<Move, Board<Move>> context = SearchContextBuilder.get(BasicEvaluator::new, board);
		try (ExecutionContext<SearchContext<Move, Board<Move>>> exec = new SingleThreadContext<>(context)) {
			Negamax<Move, Board<Move>> ai = new Negamax<>(exec);
			List<Move> l = new ArrayList<>();
			l.add(move(board, "h1", "g1"));
			l.add(move(board, "f2", "f3"));
			l.add(move(board, "f2", "f4"));
			final SearchParameters params = new SearchParameters(4, Integer.MAX_VALUE, 0);
			final List<EvaluatedMove<Move>> eval = ai.getBestMoves(l, params).getCut();
			assertEquals(3, eval.size());
			for (EvaluatedMove<Move> e : eval) {
				assertEquals(Type.LOOSE, e.getEvaluation().getType());
				assertEquals(1, e.getEvaluation().getCountToEnd());
			}
		}
	}
	
	@Test
	@Disabled
	void iterativeTest() {
		//TODO This test is disabled, it tests mat in 1 should not be returned when best moves are mat in 3
		// when ai is called with a reasonable non null accuracy
		// Currently, the only way to achieve this is to have a custom win/loose evaluation with a gap higher than the accuracy
		// I should think more about it...
		Board<Move> board = FENUtils.from("4n2r/2k1Q2p/5B2/2N5/2B2R2/1P6/3PKPP1/6q1 b - - 2 46");
		JChessEngine engine = new JChessEngine(BasicEvaluator::new, 8);
		engine.setParallelism(4);
		engine.getDeepeningPolicy().setSize(1);
		engine.getDeepeningPolicy().setAccuracy(300);
		engine.getDeepeningPolicy().setMaxTime(15000);
		// Tests that loose in 1 are not in the best moves (was a bug in fist iterative engine version)
		final List<EvaluatedMove<Move>> moves = engine.getBestMoves(board);
		assertEquals(2, moves.size());
		assertEquals(3, moves.get(0).getEvaluation().getCountToEnd());
		assertEquals(3, moves.get(1).getEvaluation().getCountToEnd());
	}
	
	@Test
	void iterativeTest2() {
		Board<Move> board = FENUtils.from("3bkrnr/p2ppppp/7q/2p5/8/2P5/PP1PPPPP/RNBQKBNR b KQk - 0 1");
		JChessEngine engine = new JChessEngine(BasicEvaluator::new, 4);
		engine.setParallelism(4);
		engine.getDeepeningPolicy().setSize(1);
		engine.getDeepeningPolicy().setAccuracy(100);
		engine.getDeepeningPolicy().setMaxTime(15000);
		// Tests that loosing move is not in the best moves (was a bug in fist iterative engine version)
		final List<EvaluatedMove<Move>> moves = engine.getBestMoves(board);
		for (EvaluatedMove<Move> ev : moves) {
			assertEquals(Type.EVAL, ev.getEvaluation().getType());
		}
	}
	
	@Test
	@Disabled
	void bug20230813() {
		// Not a bug, just a problem with evaluation function
		Board<Move> board = FENUtils.from("8/8/8/4p1k1/3bK3/8/7p/8 b - - 0 1");
		JChessEngine engine = new JChessEngine(BasicEvaluator::new, 4);
		engine.getDeepeningPolicy().setSize(Integer.MAX_VALUE);
		System.out.println(EvaluatedMove.toString(engine.getBestMoves(board), m -> m.toString(board.getCoordinatesSystem())));
		System.out.println(engine.apply(board).toString(board.getCoordinatesSystem()));
	}

	@Test
	@Disabled
	void bug20230821() {
		// Not a bug, just a problem with evaluation function
		Board<Move> board = FENUtils.from("8/6k1/6p1/1N6/6K1/R7/4B3/8 w - - 21 76");
		JChessEngine engine = new JChessEngine(BasicEvaluator::new, 7);
		System.out.println(engine.apply(board).toString(board.getCoordinatesSystem()));
	}
	
	@Test
	void bug20230911() {
		final JChessEngine engine = new JChessEngine(BasicEvaluator::new, 8);
		engine.setParallelism(1);
		engine.getDeepeningPolicy().setSize(1);
		engine.getDeepeningPolicy().setAccuracy(0);
		Board<Move> board = FENUtils.from("8/4k3/8/R7/8/8/8/4K2R w K - 0 1");
		List<EvaluatedMove<Move>> bestMoves = engine.getBestMoves(board);
		System.out.println(EvaluatedMove.toString(bestMoves, m -> m.toString(board.getCoordinatesSystem())));
		assertEquals(2, bestMoves.size());
	}
	
	// An example of bug chasing
	void bug20230911_chase() {
		final Board<Move> board = FENUtils.from("8/4k3/8/R7/8/8/8/4K2R w K - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		SearchContext<Move, Board<Move>> context = SearchContextBuilder.get(BasicEvaluator::new, board);
		try (ExecutionContext<SearchContext<Move, Board<Move>>> exec = new SingleThreadContext<>(context)) {
			Negamax3<Move, Board<Move>> ai = new Negamax3<>(exec);
			final TT tt = new TT(16, SizeUnit.MB);
			ai.setTranspositonTable(tt);
			final Move a5a6 = move(board, "a5", "a6");
			final MySpy spy = new MySpy(cs, tt);
			ai.setSpy(spy);
			EvaluatedMove<Move> e = ai.getBestMoves(Collections.singletonList(a5a6), new SearchParameters(8)).getList().get(0);
			assertEquals(Type.WIN, e.getEvaluation().getType());
			assertEquals(4, e.getEvaluation().getCountToEnd());
			final Move h1h6 = move(board, "h1", "h6");
			spy.searchedKey = FENUtils.from("8/4k3/7R/R7/8/8/8/4K3 b - - 0 1").getHashKey(); //h1h6
			spy.searchedKey = -8273089417188127202L; //e7d7
			spy.searchedKey = 6365043373273418417L; //a5a7
			spy.searchedKey = -3019684505475777408L; //d7d8
			spy.searchedKey = 1283331931822092560L; //h6h8
			e = ai.getBestMoves(Collections.singletonList(h1h6),new SearchParameters(8)).getList().get(0);
			System.out.println("pv="+tt.collectPV(board, h1h6, 8).stream().map(m->m.toString(cs)).collect(Collectors.toList()));
			assertEquals(Type.WIN, e.getEvaluation().getType());
			assertEquals(4, e.getEvaluation().getCountToEnd());
		}
	}
	
	private static class MySpy implements Spy<Move, Board<Move>> {
		private CoordinatesSystem cs;
		private int traceDepth = Integer.MAX_VALUE;
		private long searchedKey = 0;
		private TT tt;

		private MySpy(CoordinatesSystem cs, TT tt) {
			this.cs = cs;
			this.tt = tt;
		}
		
		private CharSequence tab(int depth) {
			StringBuilder b = new StringBuilder("  ");
			depth = traceDepth-depth;
			for (int i = 0; i < depth; i++) {
				b.append("  ");
			}
			return b;
		}

		@Override
		public void enter(TreeSearchStateStack<Move, Board<Move>> state) {
			if (state.context.getGamePosition().getHashKey()==searchedKey && traceDepth==Integer.MAX_VALUE) {
				traceDepth = state.getCurrentDepth();
				System.out.println ("Start spy "+state.get(traceDepth+1).lastMove.toString(cs)+" --> "+state.context.getGamePosition().getHashKey()+": "+FENUtils.to(state.context.getGamePosition())+" at depth "+state.getCurrentDepth()+"/"+state.maxDepth);
			}
			if (traceDepth>=0 && state.getCurrentDepth()==traceDepth-1) {
				System.out.println (tab(state.getCurrentDepth()+1)+state.get(traceDepth).lastMove.toString(cs)+" --> "+state.context.getGamePosition().getHashKey()+": "+FENUtils.to(state.context.getGamePosition()));
			}
			Spy.super.enter(state);
		}
		
		@Override
		public void alphaBetaFromTT(TreeSearchStateStack<Move, Board<Move>> state, AlphaBetaState<Move> abState) {
			if (state.context.getGamePosition().getHashKey()==1283331931822092560L) {
//			if (traceDepth>=0 && state.getCurrentDepth()==traceDepth-1) {
				System.out.println("Something in TT");
			}
		}
		
		@Override
		public void storeTT(TreeSearchStateStack<Move, Board<Move>> state, AlphaBetaState<Move> abState, boolean store) {
			if (state.context.getGamePosition().getHashKey()==1283331931822092560L) {
				System.out.println ("Stored: "+store+", value="+abState.getValue()+" at depth "+abState.getDepth()+". Put "+tt.get(state.context.getGamePosition().getHashKey()).getValue()+" in table"+" ... "+Short.MAX_VALUE);
			}
		}

		@Override
		public void exit(TreeSearchStateStack<Move, Board<Move>> state, Event evt) {
			if (traceDepth>=0) {
				final long key = state.context.getGamePosition().getHashKey();
				if (state.getCurrentDepth()==traceDepth-1) {
					System.out.println (tab(state.getCurrentDepth())+"Exit with "+state.getCurrent().value+" ("+key+")");
				}
				if (state.getCurrentDepth()==traceDepth && key==searchedKey) {
					traceDepth = Integer.MAX_VALUE;
					System.out.println ("Stop spy on "+evt+". Value="+state.getCurrent().value);
				}
			}
			Spy.super.exit(state, evt);
		}
		
	}
}
