package com.fathzer.jchess.ai.evaluator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicMove;

class BasicMoveComparatorTest {

	@Test
	void test() {
		final Board<Move> board = FENParser.from("Q2n4/4P3/8/5P2/8/qK3p1k/1P6/8 w - - 0 1");
		CoordinatesSystem cs = board.getCoordinatesSystem();
		final BasicMoveComparator cmp = new BasicMoveComparator(board);
		final Move queenPawnCatch = new BasicMove(cs.getIndex("a8"), cs.getIndex("f3"));
		final Move queenQueenCatch = new BasicMove(cs.getIndex("a8"), cs.getIndex("a3"));
		final Move kingCatch = new BasicMove(cs.getIndex("b3"), cs.getIndex("a3"));
		final Move pawnMove = new BasicMove(cs.getIndex("f5"), cs.getIndex("f6"));
		final Move pawnPromo = new BasicMove(cs.getIndex("e7"), cs.getIndex("e8"), Piece.WHITE_QUEEN);
		final Move pawnCatchPromo = new BasicMove(cs.getIndex("e7"), cs.getIndex("d8"), Piece.WHITE_QUEEN);
		
		Arrays.stream(new Move[] {pawnMove, queenQueenCatch, queenPawnCatch, kingCatch, pawnCatchPromo, pawnPromo}).map(m -> m.toString(cs)+":"+cmp.getValue(m)).forEach(System.out::println);
		final List<Move> sorted = Arrays.asList(queenPawnCatch, pawnPromo, kingCatch, pawnMove, queenQueenCatch, pawnCatchPromo);
		sorted.sort(cmp);
		System.out.println(sorted.stream().map(m -> m.toString(cs)).collect(Collectors.joining(",")));
		assertEquals(Arrays.asList(pawnCatchPromo, queenQueenCatch, kingCatch, pawnPromo, queenPawnCatch, pawnMove), sorted);
		assertTrue(cmp.compare(kingCatch, queenQueenCatch)>0);
		assertTrue(cmp.compare(pawnMove, queenQueenCatch)>0);
		assertTrue(cmp.compare(pawnMove, kingCatch)>0);
		assertTrue(cmp.compare(queenPawnCatch, kingCatch)>0);
	}

}
