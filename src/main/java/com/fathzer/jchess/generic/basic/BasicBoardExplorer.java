package com.fathzer.jchess.generic.basic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

class BasicBoardExplorer implements BoardExplorer {
	private final Piece[] pieces;
	@Getter
	private int index;
	@Getter
	private Piece piece;
	
	BasicBoardExplorer(Piece[] pieces, int startPosition) {
		this.pieces = pieces;
		this.index = startPosition;
		this.piece = pieces[index];
	}
	
	@Override
	public boolean next() {
		index++;
		final boolean result = index<pieces.length;
		if (result) {
			piece = pieces[index];
		}
		return result;
	}
}
