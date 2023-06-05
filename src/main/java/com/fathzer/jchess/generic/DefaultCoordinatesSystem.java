package com.fathzer.jchess.generic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;
import com.fathzer.jchess.Direction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultCoordinatesSystem implements CoordinatesSystem {
	private final Dimension dimension;
	
	@Override
	public int getIndex(int row, int column) {
		return row*dimension.getWidth()+column;
	}

	@Override
	public int getIndex(String algebraicNotation) {
		if (algebraicNotation.length()<2) {
			throw new IllegalArgumentException();
		}
		final int column = getColumn(algebraicNotation);
		final int row = getRow(algebraicNotation);
		return row*dimension.getWidth() + column;
	}
	
	private int getRow(String pos) {
		final int y = dimension.getHeight()-Integer.parseInt(pos.substring(1));
		if (y<0) {
			throw new IllegalArgumentException();
		}
		return y;
	}
	
	private int getColumn(String pos) {
		final int x = pos.charAt(0)-'a';
		if (x<0) {
			throw new IllegalArgumentException(); 
		}
		return x;
	}


	@Override
	public int getRow(int index) {
		return index/dimension.getWidth();
	}

	@Override
	public int getColumn(int index) {
		return index % dimension.getWidth();
	}

	@Override
	public String getAlgebraicNotation(int index) {
		return getAlgebraicNotation(getRow(index), getColumn(index));
	}

	@Override
	public String getAlgebraicNotation(int row, int column) {
		return CoordinatesSystem.super.getAlgebraicNotation(dimension.getHeight()-row-1, column);
	}

	@Override
	public BoardExplorer buildExplorer(int index) {
		return new Explorer(index);
	}
	
	private class Explorer implements BoardExplorer {
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
		
		public void restart(int startPosition) {
			this.startPosition = startPosition;
			this.row = -1;
		}
		
		public void start(Direction direction) {
			start(direction.getRowIncrement(), direction.getColumnIncrement());
		}
		
		public void start(int rowIncrement, int columnIncrement) {
			this.rowIncrement = rowIncrement;
			this.columnIncrement = columnIncrement;
			this.cellIncrement = rowIncrement*dimension.getWidth()+columnIncrement;
			this.row = getRow(startPosition) + rowIncrement;
			this.column = getColumn(startPosition)+ columnIncrement;
			this.position = startPosition + cellIncrement;
		}
		
		private void prepareNext() {
			row += rowIncrement;
			column += columnIncrement;
			position += cellIncrement;
		}
		
		public boolean hasNext() {
			return row>=0 && row<dimension.getHeight() && column>=0 && column<dimension.getWidth();
		}
		public int next() {
			int result = position;
			prepareNext();
			return result;
		}
	}
}
