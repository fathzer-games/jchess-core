package com.fathzer.jchess.chess960;

import com.fathzer.games.Color;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.generic.MovesBuilder;

public class Chess960MovesBuilder extends MovesBuilder {
	public Chess960MovesBuilder(Chess960Board board) {
		super(board);
	}

	@Override
	protected void addCastling(int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		defaultMoveAdder.add(kingPosition, rookPosition);
	}
	
	@Override
	protected boolean areCastlingCellsSafe(Color attacker, int kingPosition, int kingDestination, int rookPosition) {
		// In Chess960, there can be an attacker behind the rook that will not be detected by the simple "before move cell safe" checking
		// performed by the generic method.
		final Direction pinnedRook = board.getPinnedDetector().apply(rookPosition);
		return pinnedRook!=Direction.EAST && pinnedRook!=Direction.WEST && super.areCastlingCellsSafe(attacker, kingPosition, kingDestination, rookPosition);
	}

}
