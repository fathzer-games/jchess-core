package com.fathzer.jchess.generic;

import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.util.BiIntPredicate;

public class DefaultMoveExplorer {
	@FunctionalInterface
	public interface MoveGenerator {
		void generate(ChessGameState moves, int from, int to);
	}
	
	public static final MoveGenerator DEFAULT = ChessGameState::add;
	
	public void addMove(ChessGameState moves, DirectionExplorer explorer, Direction direction, BiIntPredicate validator)  {
		addMove(moves, explorer, direction, validator, DEFAULT);
	}

	public void addMove(ChessGameState moves, DirectionExplorer explorer, Direction direction, BiIntPredicate validator, MoveGenerator moveGenerator)  {
		explorer.start(direction);
		if (explorer.next()) {
			final int from = explorer.getStartPosition();
			final int to = explorer.getIndex();
			if (validator.test(from, to)) {
				moveGenerator.generate(moves, from, to);
			}
		}
	}

	public void addAllMoves(ChessGameState moves, DirectionExplorer explorer, Direction direction, BiIntPredicate validator)  {
		explorer.start(direction);
		while (explorer.next()) {
			final int from = explorer.getStartPosition();
			final int to = explorer.getIndex();
			if (validator.test(from, to)) {
				DEFAULT.generate(moves, from, to);
			}
			if (explorer.getPiece()!=null) {
				break;
			}
		}
	}

	public void addMoves(ChessGameState moves, DirectionExplorer explorer, Direction direction, int maxIteration, BiIntPredicate validator)  {
		addMoves(moves, explorer, direction, maxIteration, validator, DEFAULT);
	}

	public void addMoves(ChessGameState moves, DirectionExplorer explorer, Direction direction, int maxIteration, BiIntPredicate validator, MoveGenerator moveGenerator)  {
		explorer.start(direction);
		int iteration = 0;
		while (explorer.next()) {
			final int from = explorer.getStartPosition();
			final int to = explorer.getIndex();
			if (validator.test(from, to)) {
				moveGenerator.generate(moves, from, to);
			}
			iteration++;
			if (iteration>=maxIteration || explorer.getPiece()!=null) {
				break;
			}
		}
	}
}
