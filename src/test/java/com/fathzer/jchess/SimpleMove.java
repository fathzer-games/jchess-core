package com.fathzer.jchess;

import lombok.Getter;

@Getter
public class SimpleMove implements Move {
	private final int from;
	private final int to;
	private Piece promoted;
	
	private SimpleMove(CoordinatesSystem cs, String from, String to) {
		this(cs, from, to, null);
	}

	private SimpleMove(CoordinatesSystem cs, String from, String to, Piece promoted) {
		this.from = cs.getIndex(from.toLowerCase());
		this.to = cs.getIndex(to.toLowerCase());
		this.promoted = promoted;
	}

	@Override
	public Piece getPromotion() {
		return promoted;
	}
	
	public static Move get(Board<Move> board, String from, String to) {
		return new SimpleMove(board.getCoordinatesSystem(), from, to);
	}
	public static Move get(Board<Move> board, String from, String to, Piece promoted) {
		return new SimpleMove(board.getCoordinatesSystem(), from, to, promoted);
	}
}