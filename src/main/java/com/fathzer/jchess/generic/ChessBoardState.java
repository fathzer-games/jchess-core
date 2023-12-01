package com.fathzer.jchess.generic;

class ChessBoardState {
	int enPassant;
	int enPassantDeletePawnIndex;
	int castlings;
	int moveNumber;
	int halfMoveCount;
	InsufficientMaterialDetector insufficientMaterialDetector;
	PinnedDetector pinnedDetector;
	MoveHelperHolder moveHelperHolder;
	long key;
	MovesBuilder.MovesBuilderState moveBuidlerState;
	
	ChessBoardState(ChessBoard board) {
		this.moveHelperHolder = new MoveHelperHolder();
		this.insufficientMaterialDetector = new InsufficientMaterialDetector();
		this.pinnedDetector = new PinnedDetector(board);
		this.moveBuidlerState = new MovesBuilder.MovesBuilderState();
	}
}
