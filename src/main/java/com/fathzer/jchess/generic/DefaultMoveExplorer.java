package com.fathzer.jchess.generic;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.util.BiIntPredicate;

public class DefaultMoveExplorer {
	@FunctionalInterface
	public interface MoveGenerator {
		void generate(ChessGameState moves, int from, int to);
	}
	
	public static final MoveGenerator DEFAULT = ChessGameState::add;
	
	public void addMoves(ChessGameState moves, BoardExplorer explorer, int from, Direction direction, int maxIteration, BiIntPredicate validator)  {
		addMoves(moves, explorer, from, direction, maxIteration, validator, DEFAULT);
	}

	public void addMoves(ChessGameState moves, BoardExplorer explorer, int from, Direction direction, int maxIteration, BiIntPredicate validator, MoveGenerator moveGenerator)  {
		explorer.setPosition(from);
		explorer.setDirection(direction);
		int iteration = 0;
		while (explorer.next()) {
			final int to = explorer.getIndex();
			final Piece piece = explorer.getPiece();
			boolean isFree = piece==null;
			if (validator.test(from, to)) {
				moveGenerator.generate(moves, from, to);
			}
			iteration++;
			if (iteration>=maxIteration || !isFree) {
				break;
			}
		}
	}
}
