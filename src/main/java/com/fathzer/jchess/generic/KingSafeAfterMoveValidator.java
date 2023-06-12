package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;

/** A move validator that verifies the king is safe after the move is played.
 */
public class KingSafeAfterMoveValidator implements BiPredicate<BoardExplorer, BoardExplorer> {
	private final Board<?> board;
	private final AttackDetector detector;
	
	public KingSafeAfterMoveValidator(Board<?> board) {
		this(board, new AttackDetector(board));
	}

	public KingSafeAfterMoveValidator(Board<?> board, AttackDetector detector) {
		this.board = board;
		this.detector = detector;
	}

	@Override
	public boolean test(BoardExplorer source, BoardExplorer dest) {
		final Color movingColor = source.getPiece().getColor();
		board.moveCellsOnly(source.getIndex(), dest.getIndex());
		final boolean result = !detector.isAttacked(board.getKingPosition(movingColor), movingColor.opposite());
		board.restoreMoveCellsOnly();
		return result; 
	}
}
