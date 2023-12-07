package com.fathzer.jchess.ai.evaluator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;

class BasicMoveComparatorTest implements MoveBuilder {

	@Test
	void test() {
		final Board<Move> board = FENUtils.from("Q2n4/4P3/8/5P2/8/qK3p1k/1P6/8 w - - 0 1");
		CoordinatesSystem cs = board.getCoordinatesSystem();
		final BasicMoveComparator cmp = new BasicMoveComparator(board);
		final Move queenPawnCatch = move(board, "a8", "f3");
		final Move queenQueenCatch = move(board, "a8", "a3");
		final Move kingCatch = move(board, "b3", "a3");
		final Move pawnMove = move(board, "f5", "f6");
		final Move pawnPromo = move(board, "e7", "e8", Piece.WHITE_QUEEN);
		final Move pawnCatchPromo = move(board, "e7", "d8", Piece.WHITE_QUEEN);
		
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
