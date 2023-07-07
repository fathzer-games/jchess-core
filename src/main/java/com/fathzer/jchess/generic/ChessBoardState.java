package com.fathzer.jchess.generic;

import com.fathzer.jchess.Piece;

class ChessBoardState {
	final Piece[] cells;
	int[] kingPositions;
	int enPassant;
	int enPassantDeletePawnIndex;
	int castlings;
	int moveNumber;
	int halfMoveCount;
	long key;
	
	ChessBoardState(int cellsCount) {
		this.cells = new Piece[cellsCount];
		this.kingPositions = new int[2];
	}
}
