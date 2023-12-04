package com.fathzer.jchess.chess960;

import com.fathzer.jchess.generic.MovesBuilder;

public class Chess960MovesBuilder extends MovesBuilder {
	public Chess960MovesBuilder(Chess960Board board) {
		super(board);
	}

	@Override
	protected void addCastling(int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		defaultMoveAdder.add(kingPosition, rookPosition);
	}
}
