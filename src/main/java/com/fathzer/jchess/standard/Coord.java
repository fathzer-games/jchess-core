package com.fathzer.jchess.standard;

import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Notation;

public class Coord {
	public static int toIndex(String str) {
		return Notation.toPosition(str, Dimension.STANDARD);
	}
	public static String toString(int index) {
		return Notation.toString(index, Dimension.STANDARD);
	}
}
