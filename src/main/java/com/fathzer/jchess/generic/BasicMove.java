package com.fathzer.jchess.generic;

import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
class BasicMove implements Move {
	private final int from;
	private final int to;
	private final Piece promoted;
	
	public BasicMove(int from, int to, Piece promoted) {
		this.from = from;
		this.to = to;
		this.promoted = promoted;
	}
	
	public BasicMove(int from, int to) {
		this(from, to, null);
	}
	
}