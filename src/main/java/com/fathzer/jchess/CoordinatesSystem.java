package com.fathzer.jchess;

public interface CoordinatesSystem {
	Dimension getDimension();
	int getIndex(int row, int column);
	int nextRow(int index);
	int previousRow(int index);
	int getRow(int index);
	int getColumn(int index);

	int getIndex(String algebraicNotation);
	default String getAlgebraicNotation(int index) {
		return getAlgebraicNotation(getRow(index), getColumn(index));
	}
	default String getAlgebraicNotation(int row, int column) {
		final char x = (char)('a' + column);
		return x+Integer.toString(row+1);
	}
}
