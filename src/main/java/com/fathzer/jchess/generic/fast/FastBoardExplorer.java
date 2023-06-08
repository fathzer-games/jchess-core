package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Piece;

import lombok.Getter;

class FastBoardExplorer implements BoardExplorer {
	private final CoordinatesSystem cs;
	private final Piece[] pieces;
	@Getter
	private int index;
	@Getter
	private Piece piece;
	private int cellIncrement;
	private boolean hasDirection;
	
	FastBoardExplorer(Piece[] pieces, CoordinatesSystem cs, int startPosition) {
		this.cs = cs;
		this.pieces = pieces;
		setDirection(null);
		setPosition(startPosition);
	}
	
	@Override
	public void setPosition(int index) {
		this.index = index;
		piece = pieces[index];
	}
	
	@Override
	public void setDirection(Direction direction) {
		if (direction==null) {
			this.hasDirection = false;
			this.cellIncrement = 1;
		} else {
			setDirection(direction.getRowIncrement(), direction.getColumnIncrement());
		}
	}
	
	public void setDirection(int rowIncrement, int columnIncrement) {
		this.hasDirection = true;
		this.cellIncrement = rowIncrement*(cs.getDimension().getWidth()+2)+columnIncrement;
	}
	
	@Override
	public boolean next() {
		index += cellIncrement;
		if (index<0 || index>=pieces.length) {
			return false;
		}
		piece = pieces[index];
		if (piece != Piece.BORDER) {
			return true;
		}
		return !hasDirection && next();
	}
}
