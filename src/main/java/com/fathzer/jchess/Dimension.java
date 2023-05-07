package com.fathzer.jchess;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(exclude = {"size","zobristKeyBuilder"})
public class Dimension {
	public static final Dimension STANDARD = new Dimension(8,8);
	
	private final int width;
	private final int height;
	private final int size;
	private final ZobristKeyBuilder zobristKeyBuilder;

	public Dimension(int width, int heigth) {
		this.width = width;
		this.height = heigth;
		this.size = width*heigth;
		this.zobristKeyBuilder = new ZobristKeyBuilder(this);
	}
	
	public int getPosition(int row, int column) {
		return row*width + column;
	}
	
	public int getRow(int pos) {
		return pos/width;
	}
	
	public int getColumn(int pos) {
		return pos % width;
	}

	public class Explorer {
		@Getter
		private int startPosition;
		private int position;
		private int row;
		private int column;
		private int cellIncrement;
		private int rowIncrement;
		private int columnIncrement;
		
		public Explorer(int startPosition) {
			this.startPosition = startPosition;
			this.row = -1;
		}
		
		public Explorer restart(int startPosition) {
			this.startPosition = startPosition;
			this.row = -1;
			return this;
		}
		
		public Explorer start(Direction direction) {
			return start(direction.getRowIncrement(), direction.getColumnIncrement());
		}
		
		public Explorer start(int rowIncrement, int columnIncrement) {
			this.rowIncrement = rowIncrement;
			this.columnIncrement = columnIncrement;
			this.cellIncrement = rowIncrement*getWidth()+columnIncrement;
			this.row = getRow(startPosition) + rowIncrement;
			this.column = getColumn(startPosition)+ columnIncrement;
			this.position = startPosition + cellIncrement;
			return this;
		}
		
		private void prepareNext() {
			row += rowIncrement;
			column += columnIncrement;
			position += cellIncrement;
		}
		
		public boolean hasNext() {
			return row>=0 && row<getHeight() && column>=0 && column<getWidth();
		}
		public int next() {
			int result = position;
			prepareNext();
			return result;
		}
	}
}
