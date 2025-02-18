package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

class FastBoardExplorer implements BoardExplorer {
	private final Piece[] pieces;
	@Getter
	private int index;
	@Getter
	private Piece piece;
	
	FastBoardExplorer(Piece[] pieces, int startPosition) {
		this.pieces = pieces;
		this.index = startPosition;
		this.piece = pieces[index];
	}
	
	@Override
	public boolean next() {
		index++;
		if (index>=pieces.length) {
			return false;
		}
		piece = pieces[index];
		return piece != Piece.BORDER || next();
	}

	@Override
	public void reset(int index) {
		this.index = index;
		this.piece = pieces[index];
	}
}
