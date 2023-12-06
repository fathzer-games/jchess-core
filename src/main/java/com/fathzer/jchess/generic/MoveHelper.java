package com.fathzer.jchess.generic;

import com.fathzer.jchess.Piece;

public abstract class MoveHelper {
	protected Piece piece;
	protected int fromIndex;
	protected int toIndex;
	
	public abstract void unmakePieces(Piece[] p);
	public void unmakeKingPosition(int[] kingPositions, int index) {
		// Does nothing by default
	}
	public boolean isCastling() {
		return false;
	}
	public boolean isKingSafetyTestRequired() {
		return false;
	}
	public boolean shouldIncHalfMoveCount() {
		return false;
	}
	public Piece getCaptured() {
		return null;
	}
}