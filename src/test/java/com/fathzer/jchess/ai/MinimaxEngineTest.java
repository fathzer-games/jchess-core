package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.AbstractAI;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.util.ContextualizedExecutor;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.CopyBasedMoveGenerator;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicEvaluator;
import com.fathzer.jchess.generic.StandardChessRules;
import com.fathzer.jchess.standard.CompactMoveList;

class MinimaxEngineTest {
	private int getMateScore(int nbMoves) {
		return new AbstractAI<Move>(null,null) {
			@Override
			public List<Evaluation<Move>> getBestMoves(int depth, Iterator<Move> possibleMoves, int size, int accuracy) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int evaluate() {
				throw new UnsupportedOperationException();
			}}.getWinScore(nbMoves);
	}
	
	@Test
	void blackPlayingTest() {
		final JChessEngine mme4 = new JChessEngine(StandardChessRules.INSTANCE, new BasicEvaluator(), 3);
		final Board<Move> board = FENParser.from("7k/5p1Q/5P1N/5PPK/6PP/8/8/8 b - - 6 5");
		final List<Evaluation<Move>> moves = mme4.getBestMoves(board, Integer.MAX_VALUE, 0);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
show(moves, cs);
		assertEquals(1, moves.size());
		assertEquals("h8", cs.getAlgebraicNotation(moves.get(0).getContent().getFrom()));
		assertEquals("h7", cs.getAlgebraicNotation(moves.get(0).getContent().getTo()));
		assertEquals(-800, moves.get(0).getValue());
	}
	
	private void show(Collection<Evaluation<Move>> moves, CoordinatesSystem cs) {
		System.out.println(Evaluation.toString(moves, m -> m.toString(cs)));
	}
	
	@Test
	void test() {
		List<Evaluation<Move>> moves;
		final JChessEngine mme4 = new JChessEngine(StandardChessRules.INSTANCE, new BasicEvaluator(), 4);
		
		// 3 possible Mats in 1 with whites
		Board<Move> board = FENParser.from("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		moves = mme4.getBestMoves(board, Integer.MAX_VALUE, 0);
show(moves, cs);
		assertEquals(6, moves.size());
		double max = moves.get(0).getValue();
		assertEquals(getMateScore(1), max);
		assertTrue(moves.get(3).getValue()<max);
		moves.stream().limit(3).forEach(m -> assertEquals(max, m.getValue()));
//fail("enough!");		
		// Mat in 1 with blacks
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENParser.from("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5"), Integer.MAX_VALUE, 0);
show(moves, cs);
		assertEquals(7, moves.size());
		assertEquals(getMateScore(1), moves.get(0).getValue());
		Move mv = moves.get(0).getContent();
		assertEquals("c3", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("c2", cs.getAlgebraicNotation(mv.getTo()));
		assertTrue(moves.get(1).getValue()<10000.0);
		
		// Check in 2
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENParser.from("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1"), Integer.MAX_VALUE, 0);
show(moves, cs);
		assertEquals(getMateScore(2), moves.get(0).getValue());
		assertTrue(moves.get(1).getValue()<moves.get(0).getValue());
		mv = moves.get(0).getContent();
		assertEquals("b3", cs.getAlgebraicNotation(mv.getFrom()));
		assertEquals("a1", cs.getAlgebraicNotation(mv.getTo()));
		
		// Check in 2 with blacks
		System.out.println("------------------");
		moves = mme4.getBestMoves(FENParser.from("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1"), Integer.MAX_VALUE, 0);
show(moves, cs);
		assertEquals(getMateScore(2), moves.get(0).getValue());
		assertTrue(moves.get(1).getValue()<moves.get(0).getValue());
		assertEquals("g6", cs.getAlgebraicNotation(moves.get(0).getContent().getFrom()));
		assertEquals("h8", cs.getAlgebraicNotation(moves.get(0).getContent().getTo()));
		
		
		// Check in 3
		System.out.println("------------------");
		board = FENParser.from("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1");
		moves = new JChessEngine(StandardChessRules.INSTANCE, new BasicEvaluator(), 6).getBestMoves(board, 3, 100);
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
		final ChessEvaluator basicEvaluator = new BasicEvaluator();
		basicEvaluator.setViewPoint(Color.WHITE);
		try (ContextualizedExecutor<MoveGenerator<Move>> exec = new ContextualizedExecutor<>(1)) {
			AbstractAI<Move> ai = new Negamax<>(() -> new CopyBasedMoveGenerator<>(StandardChessRules.INSTANCE, board), exec) {
				@Override
				public int evaluate() {
					return basicEvaluator.evaluate(board);
				}
			};
			CompactMoveList l = new CompactMoveList();
			l.add(cs.getIndex("h1"), cs.getIndex("g1"));
			l.add(cs.getIndex("f2"), cs.getIndex("f3"));
			l.add(cs.getIndex("f2"), cs.getIndex("f4"));
			final List<Evaluation<Move>> eval = ai.getBestMoves(4, l.iterator(), Integer.MAX_VALUE, 0);
			assertEquals(3, eval.size());
			for (Evaluation<Move> e : eval) {
				assertEquals(-getMateScore(1), e.getValue());
			}
		}
	}
}
