package com.fathzer.jchess.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BitMapUtils {
	private static final long POS_0 = 1L<<63;
	private static final String ZERO = "0000000000000000000000000000000000000000000000000000000000000000";
	
	public static String asBinary(long bitmap) {
		String binary = Long.toBinaryString(bitmap);
		return ZERO.substring(0, 64-binary.length()) + binary;
	}

	public static CharSequence asBoard(long bitmap) {
		final String binary = asBinary(bitmap);
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			if (b.length()!=0) {
				b.append('\n');
			}
			b.append(binary.substring(i*8, (i+1)*8));
		}
		return b;
	}
	
	public static long getMask(int position) {
		return POS_0 >>> position;
	}
}
