package com.fathzer.jchess.generic;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.util.BiIntPredicate;

public class DefaultMoveExplorer {
	@FunctionalInterface
	public interface MoveGenerator {
		void generate(ChessGameState moves, int from, int to);
	}
	
	public static final MoveGenerator DEFAULT = ChessGameState::add;
	
	private final Board<Move> board;

	public DefaultMoveExplorer(Board<Move> board) {
		this.board = board;
	}
	
	public void addMoves(ChessGameState moves, InternalBoardExplorer explorer, Direction direction, int maxIteration, BiIntPredicate validator)  {
		addMoves(moves, explorer, direction, maxIteration, validator, DEFAULT);
	}

	public void addMoves(ChessGameState moves, InternalBoardExplorer explorer, Direction direction, int maxIteration, BiIntPredicate validator, MoveGenerator moveGenerator)  {
		explorer.start(direction);
		int iteration = 0;
		while (explorer.hasNext()) {
			final int to = explorer.next();
			final Piece piece = board.getPiece(to);
			boolean isFree = piece==null;
			if (validator.test(explorer.getStartPosition(), to)) {
				moveGenerator.generate(moves, explorer.getStartPosition(), to);
			}
			iteration++;
			if (iteration>=maxIteration || !isFree) {
				break;
			}
		}
	}
}
