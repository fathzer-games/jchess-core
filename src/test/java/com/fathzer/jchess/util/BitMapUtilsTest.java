package com.fathzer.jchess.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BitMapUtilsTest {

	@Test
	void test() {
		assertEquals("1000000000000000000000000000000000000000000000000000000000000000", BitMapUtils.asBinary(BitMapUtils.getMask(0)));
		assertEquals("0100000000000000000000000000000000000000000000000000000000000000", BitMapUtils.asBinary(BitMapUtils.getMask(1)));
		assertEquals("0000000000000000000000000000000000000000000000000000000000000001", BitMapUtils.asBinary(BitMapUtils.getMask(63)));
		assertEquals("0000000000000000000000000000000000000000000000000000000000000000", BitMapUtils.asBinary(0L));
	}

}
