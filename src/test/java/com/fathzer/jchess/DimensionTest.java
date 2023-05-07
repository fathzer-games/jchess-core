package com.fathzer.jchess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DimensionTest {

	@Test
	void test() {
		final Dimension d = new Dimension(8,8);
		assertEquals(0,d.getRow(0));
		assertEquals(0,d.getColumn(0));
	}

}
