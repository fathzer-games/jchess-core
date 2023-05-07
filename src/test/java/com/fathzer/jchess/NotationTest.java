package com.fathzer.jchess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NotationTest {

	@Test
	void testStandard() {
		final Dimension d = new Dimension(8, 8);
		assertEquals(0, Notation.toPosition("a8", d));
		assertEquals(56, Notation.toPosition("a1", d));
		assertThrows (IllegalArgumentException.class, () -> Notation.toString(-1,d));
		assertThrows (IllegalArgumentException.class, () -> Notation.toString(64,d));
		assertEquals("a8", Notation.toString(0,d));
		assertEquals("h1", Notation.toString(63,d));
	}
	
	@Test
	void testNonStandard() {
		Dimension tenPerTen = new Dimension(10, 10);
		assertEquals("j1", Notation.toString(99,tenPerTen));
		assertEquals("a10", Notation.toString(0,tenPerTen));
		assertEquals(0, Notation.toPosition("a10",tenPerTen));
		assertEquals(99, Notation.toPosition("j1",tenPerTen));
	}
}
