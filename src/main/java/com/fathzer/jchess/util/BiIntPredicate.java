package com.fathzer.jchess.util;

@FunctionalInterface
public interface BiIntPredicate {
	boolean test(int from, int to);
}
