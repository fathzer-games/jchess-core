package com.fathzer.jchess;

public interface CoordinatesSystem {

	int getIndex(int row, int column);
	int getRow(int index);
	int getColumn(int index);

	
	int getIndex(String algebraicNotation);
	String getAlgebraicNotation(int index);
	
	default String getAlgebraicNotation(int row, int column) {
		final char x = (char)('a' + column);
		return x+Integer.toString(row+1);
	}
	
	BoardExplorer buildExplorer(int index);
	ZobristKeyBuilder getZobristKeyBuilder();
}
