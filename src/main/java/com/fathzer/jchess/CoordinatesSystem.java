package com.fathzer.jchess;

public interface CoordinatesSystem {
	Dimension getDimension();
	int getIndex(int row, int column);
	int nextRow(int index);
	int previousRow(int index);
	int getRow(int index);
	int getColumn(int index);

	default String getAlgebraicNotation(int index) {
		return getAlgebraicNotation(getRow(index), getColumn(index));
	}
	default String getAlgebraicNotation(int row, int column) {
		final char x = (char)('a' + column);
		return x+Integer.toString(row+1);
	}
	
	default int getIndex(String algebraicNotation) {
		if (algebraicNotation.length()<2) {
			throw new IllegalArgumentException();
		}
		final int column = algebraicNotation.charAt(0)-'a';
		final int row = getDimension().getHeight()-Integer.parseInt(algebraicNotation.substring(1));
		if (column<0 || row <0) {
			throw new IllegalArgumentException(); 
		}
		return getIndex(row, column);
	}
}
