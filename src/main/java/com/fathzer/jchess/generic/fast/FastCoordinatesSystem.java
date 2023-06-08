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
	public int getIndex(String algebraicNotation) {
		if (algebraicNotation.length()<2) {
			throw new IllegalArgumentException();
		}
		final int column = getColumn(algebraicNotation);
		final int row = getRow(algebraicNotation);
		return getIndex(row, column);
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
