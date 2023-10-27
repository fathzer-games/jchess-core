package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicMove;

class StrictMoveComparatorTest {

	@Test
	void test() {
		Board<Move> b = FENParser.from(FENParser.NEW_STANDARD_GAME);
		CoordinatesSystem cs = b.getCoordinatesSystem();
		StrictMoveComparator c = new StrictMoveComparator(b);
		// Ensure first is A8
		assertTrue(c.compare(new BasicMove(cs.getIndex("a8"),cs.getIndex("a6")),new BasicMove(cs.getIndex("a7"),cs.getIndex("a6")))<0);
		assertTrue(c.compare(new BasicMove(cs.getIndex("a8"),cs.getIndex("a6")),new BasicMove(cs.getIndex("b8"),cs.getIndex("a6")))<0);
		
		// Ensure Last is H1
		assertTrue(c.compare(new BasicMove(cs.getIndex("h1"),cs.getIndex("h6")),new BasicMove(cs.getIndex("h2"),cs.getIndex("h6")))>0);
		assertTrue(c.compare(new BasicMove(cs.getIndex("h1"),cs.getIndex("h6")),new BasicMove(cs.getIndex("g8"),cs.getIndex("h6")))>0);
	}

	@Test
	void test2() {
		Board<Move> board = FENParser.from("5B2/8/7p/8/8/NN6/pk1K4/8 b - - 0 1");
		CoordinatesSystem cs = board.getCoordinatesSystem();
		final StrictMoveComparator cmp = new StrictMoveComparator(board);
		
		List<Move> moves = board.getMoves(false); //FIXME Should use legal moves
		moves.sort(cmp);
		
		final Move queenPromo = new BasicMove(cs.getIndex("a2"), cs.getIndex("a1"), Piece.BLACK_QUEEN);
		final Move rookPromo = new BasicMove(cs.getIndex("a2"), cs.getIndex("a1"), Piece.BLACK_ROOK);
		final Move bishopPromo = new BasicMove(cs.getIndex("a2"), cs.getIndex("a1"), Piece.BLACK_BISHOP);
		final Move knightPromo = new BasicMove(cs.getIndex("a2"), cs.getIndex("a1"), Piece.BLACK_KNIGHT);
		final Move knightCaught = new BasicMove(cs.getIndex("b2"), cs.getIndex("b3"));
		final Move pawnAdvance = new BasicMove(cs.getIndex("h6"), cs.getIndex("h5"));

		assertEquals(Arrays.asList(queenPromo, rookPromo, knightCaught, bishopPromo, knightPromo, pawnAdvance), moves);
		
		assertTrue(cmp.compare(bishopPromo,rookPromo)>0);
		assertTrue(cmp.compare(knightPromo, bishopPromo)>0);
	}
	
}
