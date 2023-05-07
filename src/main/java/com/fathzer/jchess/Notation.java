package com.fathzer.jchess;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Notation {
	public static int toPosition(String pos, Dimension d) {
		if (pos.length()<2) {
			throw new IllegalArgumentException();
		}
		final int x = pos.charAt(0)-'a';
		final int y = d.getHeight()-Integer.parseInt(pos.substring(1));
		if (x<0 || y<0) {
			throw new IllegalArgumentException();
		}
		return d.getPosition(y, x);
	}
	
	public static String toString(int pos, Dimension d) {
		if (pos<0 || pos>=d.getSize()) {
			throw new IllegalArgumentException();
		}
		final char x = (char)('a' + d.getColumn(pos));
		final int y = d.getRow(pos);
		return x+Integer.toString(d.getHeight()-y);
	}
}
