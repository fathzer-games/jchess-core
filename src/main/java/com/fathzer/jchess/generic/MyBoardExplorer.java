package com.fathzer.jchess.generic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Piece;

import lombok.Getter;

class MyBoardExplorer implements BoardExplorer {
	private final CoordinatesSystem cs;
	private final Piece[] pieces;
	@Getter
	private int startPosition;
	@Getter
	private int index;
	@Getter
	private Piece piece;
	private int row;
	private int column;
	private int cellIncrement;
	private int rowIncrement;
	private int columnIncrement;
	private boolean hasDirection;
	
	MyBoardExplorer(Piece[] pieces, CoordinatesSystem cs, int startPosition) {
		this.cs = cs;
		this.pieces = pieces;
		setDirection(null);
		reset(startPosition);
	}
	
	@Override
	public void reset(int index) {
		this.startPosition = index;
		this.index = startPosition;
		piece = pieces[startPosition];
		this.row = cs.getRow(startPosition);
		this.column = cs.getColumn(startPosition);
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
		this.rowIncrement = rowIncrement;
		this.columnIncrement = columnIncrement;
		this.cellIncrement = rowIncrement*cs.getDimension().getWidth()+columnIncrement;
	}
	
	@Override
	public boolean next() {
		index += cellIncrement;
		final boolean result;
		if (hasDirection) {
			row += rowIncrement;
			column += columnIncrement;
			result = row>=0 && row<cs.getDimension().getHeight() && column>=0 && column<cs.getDimension().getWidth();
		} else {
			result = index<pieces.length;
		}
		if (result) {
			piece = pieces[index];
		}
		return result;
	}
}
