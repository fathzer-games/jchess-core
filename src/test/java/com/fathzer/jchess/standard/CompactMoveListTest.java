package com.fathzer.jchess.standard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

class CompactMoveListTest {

	@Test
	void test() {
		final CompactMoveList list = new CompactMoveList();
		list.add(10,18);
		Move m = list.get(0);
		assertEquals(18, m.getTo());
		assertEquals(10, m.getFrom());
		assertNull(m.promotedTo());
		
		list.add(55 ,63, Piece.WHITE_QUEEN);
		m = list.get(1);
		assertEquals(63, m.getTo());
		assertEquals(55, m.getFrom());
		assertEquals(Piece.WHITE_QUEEN, m.promotedTo());
	}
	
	@Test
	void sortTest() {
		final CompactMoveList list = new CompactMoveList();
		list.add(0, 1);
		list.add(1, 2);
		list.add(1, 3, Piece.BLACK_QUEEN);
		list.sort(i -> list.get(i).getTo());
		list.sort(i -> list.get(i).getTo());
		assertArrayEquals(new int[] {3,2,1}, IntStream.range(0, list.size()).map(i -> list.get(i).getTo()).toArray());
		assertEquals(Piece.BLACK_QUEEN, list.get(0).promotedTo());
	}


}
