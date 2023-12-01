package com.fathzer.jchess.generic;

import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ZobristKeyBuilder;

public abstract class MoveHelper {
	protected Piece piece;
	protected int fromIndex;
	protected int toIndex;
	
	public abstract void unmakePieces(Piece[] p);
	public void unmakeKingPosition(int[] kingPositions, int index) {
		// Does nothing by default
	}
	public long updateKey(long key, ZobristKeyBuilder zobrist) {
		key ^= zobrist.getKey(fromIndex, piece);
		key ^= zobrist.getKey(toIndex, piece);
		key ^= zobrist.getTurnKey();
		return key;
	}
}