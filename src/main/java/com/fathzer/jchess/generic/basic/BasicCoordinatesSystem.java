package com.fathzer.jchess.generic.basic;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;

import lombok.Getter;

class BasicCoordinatesSystem implements CoordinatesSystem {
	@Getter
	private final Dimension dimension;
	
	BasicCoordinatesSystem(Dimension dimension) {
		this.dimension = dimension;
	}

	@Override
	public int getIndex(int row, int column) {
		return row*dimension.getWidth()+column;
	}
	
	@Override
	public int nextRow(int index) {
		return index+dimension.getWidth();
	}

	@Override
	public int previousRow(int index) {
		return index-dimension.getWidth();
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
	public String getAlgebraicNotation(int row, int column) {
		return CoordinatesSystem.super.getAlgebraicNotation(dimension.getHeight()-row-1, column);
	}
}
