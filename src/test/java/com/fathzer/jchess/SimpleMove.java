package com.fathzer.jchess;

import com.fathzer.jchess.standard.Coord;

import lombok.Getter;

@Getter
public class SimpleMove implements Move {
	private final int from;
	private final int to;
	private Piece promoted;
	
	public SimpleMove(String from, String to) {
		this(from,to,null);
	}

	public SimpleMove(String from, String to, Piece promoted) {
		this.from = Coord.toIndex(from);
		this.to = Coord.toIndex(to);
		this.promoted = promoted;
	}

	@Override
	public Piece promotedTo() {
		return promoted;
	}
}