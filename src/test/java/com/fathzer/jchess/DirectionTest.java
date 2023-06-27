package com.fathzer.jchess;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class DirectionTest {

	@Test
	void test() {
		Arrays.stream(Direction.values()).forEach(d -> {
			final Direction o = d.getOpposite();
			assertNotNull(o, d+" has no opposite");
			assertEquals(-d.getColumnIncrement(), o.getColumnIncrement());
			assertEquals(-d.getRowIncrement(), o.getRowIncrement());
		});
	}

}