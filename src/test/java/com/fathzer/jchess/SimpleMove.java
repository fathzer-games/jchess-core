package com.fathzer.jchess;

import lombok.Getter;

@Getter
public class SimpleMove implements Move {
	private final int from;
	private final int to;
	private Piece promoted;
	
	public SimpleMove(CoordinatesSystem cs, String from, String to) {
		this(cs, from, to, null);
	}

	public SimpleMove(CoordinatesSystem cs, String from, String to, Piece promoted) {
		this.from = cs.getIndex(from);
		this.to = cs.getIndex(to);
		this.promoted = promoted;
	}

	@Override
	public Piece promotedTo() {
		return promoted;
	}
}