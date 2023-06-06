package com.fathzer.jchess.generic;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Dimension;

import lombok.Getter;

class DefaultCoordinatesSystem implements CoordinatesSystem {
	@Getter
	private final Dimension dimension;
	
	DefaultCoordinatesSystem(Dimension dimension) {
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
	public String getAlgebraicNotation(int row, int column) {
		return CoordinatesSystem.super.getAlgebraicNotation(dimension.getHeight()-row-1, column);
	}
}
