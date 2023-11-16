package com.fathzer.jchess.generic;

class ChessBoardState {
	int[] kingPositions;
	int enPassant;
	int enPassantDeletePawnIndex;
	int castlings;
	int moveNumber;
	int halfMoveCount;
	InsufficientMaterialDetector insufficientMaterialDetector;
	BoardMoveUnmaker boardMoveUnmaker;
	long key;
	
	ChessBoardState() {
		this.boardMoveUnmaker = new BoardMoveUnmaker();
		this.kingPositions = new int[2];
		this.insufficientMaterialDetector = new InsufficientMaterialDetector();
	}
}
