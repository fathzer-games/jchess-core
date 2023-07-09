package com.fathzer.jchess.generic;

import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.Getter;

@Getter
public class BasicMove implements Move {
	private final int from;
	private final int to;
	private final Piece promotion;
	
	public BasicMove(int from, int to) {
		this(from, to, null);
	}
	
	public BasicMove(int from, int to, Piece promoted) {
		this.from = from;
		this.to = to;
		this.promotion = promoted;
	}

	@Override
	public String toString() {
		return from + "-" + to + (promotion==null ? "" : "("+promotion+ ")");
	}
}