package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.SingleThreadContext;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicEvaluator;
import com.fathzer.jchess.generic.BasicMove;

class MinimaxEngineTest {
	
	@Test
	void blackPlayingTest() {
		final JChessEngine mme4 = new JChessEngine(new BasicEvaluator(), 3);
		mme4.getSearchParams().setSize(Integer.MAX_VALUE);
		final Board<Move> board = FENParser.from("7k/5p1Q/5P1N/5PPK/6PP/8/8/8 b - - 6 5");
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
		final JChessEngine mme4 = new JChessEngine(new BasicEvaluator(), 4);
		mme4.getSearchParams().setSize(Integer.MAX_VALUE);
		
		// 3 possible Mats in 1 with whites
		Board<Move> board = FENParser.from("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5");
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
		moves = mme4.getBestMoves(FENParser.from("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5"));
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
		moves = mme4.getBestMoves(FENParser.from("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1"));
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
		moves = mme4.getBestMoves(FENParser.from("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1"));
show(moves, cs);
		max = moves.get(0).getEvaluation();
		assertEquals(Type.WIN, max.getType());
		assertEquals(2, max.getCountToEnd());
		assertTrue(moves.get(1).getScore()<max.getScore());
		assertEquals("g6", cs.getAlgebraicNotation(moves.get(0).getContent().getFrom()));
		assertEquals("h8", cs.getAlgebraicNotation(moves.get(0).getContent().getTo()));
		
		
		// Check in 3
		System.out.println("------------------");
		JChessEngine engine = new JChessEngine(new BasicEvaluator(), 6);
		engine.getSearchParams().setSize(3);
		engine.getSearchParams().setAccuracy(100);
		board = FENParser.from("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1");
		moves = engine.getBestMoves(board);
show(moves,cs);
assertEquals(19, moves.size());
		mv = moves.get(0).getContent();
		assertEquals("d1", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("d7", cs.getAlgebraicNotation(mv.getTo()));
	}
	
	@Test
	void moreTests() {
		final Board<Move> board = FENParser.from("8/8/8/3kr3/8/8/5PPP/7K w - - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final Evaluator<Board<Move>> basicEvaluator = new BasicEvaluator();
		basicEvaluator.setViewPoint(Color.WHITE);
		try (ExecutionContext<Move, Board<Move>> exec = new SingleThreadContext<>(board)) {
			Negamax<Move, Board<Move>> ai = new Negamax<>(exec, basicEvaluator);
			List<Move> l = new ArrayList<>();
			l.add(new BasicMove(cs.getIndex("h1"), cs.getIndex("g1")));
			l.add(new BasicMove(cs.getIndex("f2"), cs.getIndex("f3")));
			l.add(new BasicMove(cs.getIndex("f2"), cs.getIndex("f4")));
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
		Board<Move> board = FENParser.from("4n2r/2k1Q2p/5B2/2N5/2B2R2/1P6/3PKPP1/6q1 b - - 2 46");
		JChessEngine engine = new JChessEngine(new BasicEvaluator(), 8);
		engine.setParallelism(4);
		engine.getSearchParams().setSize(1);
		engine.getSearchParams().setAccuracy(300);
		engine.setMaxTime(15000);
		// Tests that loose in 1 are not in the best moves (was a bug in fist iterative engine version)
		final List<EvaluatedMove<Move>> moves = engine.getBestMoves(board);
		assertEquals(2, moves.size());
		assertEquals(3, moves.get(0).getEvaluation().getCountToEnd());
		assertEquals(3, moves.get(1).getEvaluation().getCountToEnd());
	}
	
	@Test
	void iterativeTest2() {
		Board<Move> board = FENParser.from("3bkrnr/p2ppppp/7q/2p5/8/2P5/PP1PPPPP/RNBQKBNR b KQk - 0 1");
		JChessEngine engine = new JChessEngine(new BasicEvaluator(), 4);
		engine.setParallelism(4);
		engine.getSearchParams().setSize(1);
		engine.getSearchParams().setAccuracy(100);
		engine.setMaxTime(15000);
		// Tests that loosing move is not in the best moves (was a bug in fist iterative engine version)
		final List<EvaluatedMove<Move>> moves = engine.getBestMoves(board);
		for (EvaluatedMove<Move> ev : moves) {
			assertEquals(Type.EVAL, ev.getEvaluation().getType());
		}
	}
	
	public static MoveGenerator<Move> getCopy(Board<Move> board) {
		Board<Move> copy = board.create();
		copy.copy(board);
		return copy;
	}
}
