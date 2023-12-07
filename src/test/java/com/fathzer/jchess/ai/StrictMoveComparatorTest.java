package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;

class StrictMoveComparatorTest implements MoveBuilder {

	@Test
	void test() {
		Board<Move> b = FENUtils.from(FENUtils.NEW_STANDARD_GAME);
		StrictMoveComparator c = new StrictMoveComparator(b);
		// Ensure first is A8
		assertTrue(c.compare(move(b, "a8", "a6"),move(b, "a7", "a6"))<0);
		assertTrue(c.compare(move(b, "a8", "a6"),move(b, "b8", "a6"))<0);
		
		// Ensure Last is H1
		assertTrue(c.compare(move(b, "h1", "h6"),move(b, "h2", "h6"))>0);
		assertTrue(c.compare(move(b, "h1", "h6"),move(b, "g8", "h6"))>0);
	}

	@Test
	void test2() {
		Board<Move> board = FENUtils.from("5B2/8/7p/8/8/NN6/pk1K4/8 b - - 0 1");
		final StrictMoveComparator cmp = new StrictMoveComparator(board);
		
		List<Move> moves = board.getLegalMoves();
		moves.sort(cmp);
		
		final Move queenPromo = move(board, "a2", "a1", Piece.BLACK_QUEEN);
		final Move rookPromo = move(board, "a2", "a1", Piece.BLACK_ROOK);
		final Move bishopPromo = move(board, "a2", "a1", Piece.BLACK_BISHOP);
		final Move knightPromo = move(board, "a2", "a1", Piece.BLACK_KNIGHT);
		final Move knightCaught = move(board, "b2", "b3");
		final Move pawnAdvance = move(board, "h6", "h5");

		assertEquals(Arrays.asList(queenPromo, rookPromo, knightCaught, bishopPromo, knightPromo, pawnAdvance), moves);
		
		assertTrue(cmp.compare(bishopPromo,rookPromo)>0);
		assertTrue(cmp.compare(knightPromo, bishopPromo)>0);
	}
	
}
