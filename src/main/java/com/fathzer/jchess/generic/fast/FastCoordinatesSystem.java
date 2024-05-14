package com.fathzer.jchess.generic.fast;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;

import lombok.Getter;

class FastCoordinatesSystem implements CoordinatesSystem {
	@Getter
	private final Dimension dimension;
	private final int arrayWidth;
	
	FastCoordinatesSystem(Dimension dimension) {
		this.dimension = dimension;
		this.arrayWidth = dimension.getWidth()+2;
	}

	@Override
	public int getIndex(int row, int column) {
		return row*arrayWidth+column+1;
	}
	
	@Override
	public int nextRow(int index) {
		return index+arrayWidth;
	}

	@Override
	public int previousRow(int index) {
		return index-arrayWidth;
	}

	@Override
	public int getRow(int index) {
		return index/arrayWidth;
	}

	@Override
	public int getColumn(int index) {
		return (index % arrayWidth) - 1;
	}

	@Override
	public String getAlgebraicNotation(int row, int column) {
		return CoordinatesSystem.super.getAlgebraicNotation(dimension.getHeight()-row-1, column);
	}
}
