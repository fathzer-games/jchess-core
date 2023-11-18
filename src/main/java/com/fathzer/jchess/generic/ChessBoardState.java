package com.fathzer.jchess.generic;

class ChessBoardState {
	int[] kingPositions;
	int enPassant;
	int enPassantDeletePawnIndex;
	int castlings;
	int moveNumber;
	int halfMoveCount;
	InsufficientMaterialDetector insufficientMaterialDetector;
	PinnedDetector pinnedDetector;
	BoardMoveUnmaker boardMoveUnmaker;
	long key;
	MovesBuilder.MovesBuilderState moveBuidlerState;
	
	ChessBoardState(ChessBoard board) {
		this.boardMoveUnmaker = new BoardMoveUnmaker();
		this.kingPositions = new int[2];
		this.insufficientMaterialDetector = new InsufficientMaterialDetector();
		this.pinnedDetector = new PinnedDetector(board);
		this.moveBuidlerState = new MovesBuilder.MovesBuilderState();
	}
}
