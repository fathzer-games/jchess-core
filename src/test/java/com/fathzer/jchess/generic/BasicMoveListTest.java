package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class BasicMoveListTest {
	@Test
	void test() {
		final BasicMoveList list = new BasicMoveList();
		list.add(0, 1);
		list.add(1, 2);
		list.add(1, 3);
		list.sort(i -> list.get(i).getTo());
		assertArrayEquals(new int[] {3,2,1}, IntStream.range(0, list.size()).map(i -> list.get(i).getTo()).toArray());
	}
}
