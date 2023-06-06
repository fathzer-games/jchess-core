package com.fathzer.jchess.generic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Direction;

import lombok.Getter;

public class SkipFirstExplorer implements BoardExplorer {
	private final CoordinatesSystem cs;
	@Getter
	private int startPosition;
	private int position;
	private int row;
	private int column;
	private int cellIncrement;
	private int rowIncrement;
	private int columnIncrement;
	
	public SkipFirstExplorer(CoordinatesSystem cs, int startPosition) {
		this.cs = cs;
		this.startPosition = startPosition;
		this.row = -1;
	}
	
	public void reset(int startPosition) {
		this.startPosition = startPosition;
		this.row = -1;
	}
	
	public void start(Direction direction) {
		start(direction.getRowIncrement(), direction.getColumnIncrement());
	}
	
	public void start(int rowIncrement, int columnIncrement) {
		this.rowIncrement = rowIncrement;
		this.columnIncrement = columnIncrement;
		this.cellIncrement = rowIncrement*cs.getDimension().getWidth()+columnIncrement;
		this.row = cs.getRow(startPosition) + rowIncrement;
		this.column = cs.getColumn(startPosition)+ columnIncrement;
		this.position = startPosition + cellIncrement;
	}
	
	private void prepareNext() {
		row += rowIncrement;
		column += columnIncrement;
		position += cellIncrement;
	}
	
	public boolean hasNext() {
		return row>=0 && row<cs.getDimension().getHeight() && column>=0 && column<cs.getDimension().getWidth();
	}
	public int next() {
		int result = position;
		prepareNext();
		return result;
	}
}