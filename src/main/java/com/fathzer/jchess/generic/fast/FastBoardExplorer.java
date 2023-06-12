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
	private int cellIncrement;
	
	FastBoardExplorer(Piece[] pieces, int startPosition) {
		this.pieces = pieces;
		this.cellIncrement = 1;
		this.index = startPosition;
		this.piece = pieces[index];
	}
	
	@Override
	public boolean next() {
		index += cellIncrement;
		if (index<0 || index>=pieces.length) {
			return false;
		}
		piece = pieces[index];
		return piece == Piece.BORDER ? next() : true;
	}

	@Override
	public void reset(int index) {
		this.index = index;
		this.piece = pieces[index];
	}
}
