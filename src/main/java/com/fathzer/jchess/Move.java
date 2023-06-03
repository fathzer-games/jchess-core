package com.fathzer.jchess;

public interface Move {
	int getFrom();
	int getTo();
	default Piece promotedTo() {
		return null;
	}
	default String toString(CoordinatesSystem cs) {
		return cs.getAlgebraicNotation(getFrom())+"-"+cs.getAlgebraicNotation(getTo())+(promotedTo()==null?"":promotedTo().toString());
	}
}
