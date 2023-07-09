package com.fathzer.jchess;

public interface Move {
	int getFrom();
	int getTo();
	Piece getPromotion();
	default String toString(CoordinatesSystem cs) {
		return cs.getAlgebraicNotation(getFrom())+"-"+cs.getAlgebraicNotation(getTo())+(getPromotion()==null?"":getPromotion().toString());
	}
}
