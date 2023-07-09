package com.fathzer.jchess.fischerrandom;

import java.util.List;

import com.fathzer.jchess.Move;
import com.fathzer.jchess.generic.BasicMove;
import com.fathzer.jchess.generic.MovesBuilder;

public class Chess960MovesBuilder extends MovesBuilder {
	public Chess960MovesBuilder(Chess960Board board) {
		super(board);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addCastling(List<Move> moves, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		moves.add(new BasicMove(kingPosition, rookPosition));
	}

}
