package com.fathzer.jchess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PieceWithPosition {
	private Piece piece;
	private int row;
	private int column;
	
	@Override
	public String toString() {
		return piece + ":" + row + "," + column;
	}
}
