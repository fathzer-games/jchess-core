package com.fathzer.jchess.generic.basic;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

public class SkipFirstExplorer implements DirectionExplorer {
	private final Piece[] pieces;
	private final CoordinatesSystem cs;
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
	
	public SkipFirstExplorer(Piece[] pieces, CoordinatesSystem cs, int startPosition) {
		this.pieces = pieces;
		this.cs = cs;
		this.startPosition = startPosition;
	}
	
	public void reset(int startPosition) {
		this.startPosition = startPosition;
	}
	
	public void start(Direction direction) {
		start(direction.getRowIncrement(), direction.getColumnIncrement());
	}
	
	private void start(int rowIncrement, int columnIncrement) {
		this.rowIncrement = rowIncrement;
		this.columnIncrement = columnIncrement;
		this.cellIncrement = rowIncrement*cs.getDimension().getWidth()+columnIncrement;
		this.index = startPosition;
		this.row = cs.getRow(index);
		this.column = cs.getColumn(index);
	}
	
	@Override
	public boolean next() {
		row += rowIncrement;
		column += columnIncrement;
		final boolean result = row>=0 && row<cs.getDimension().getHeight() && column>=0 && column<cs.getDimension().getWidth();
		if (result) {
			index += cellIncrement;
			this.piece = pieces[index];
		}
		return result;
	}
}