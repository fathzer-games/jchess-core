package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.util.BiIntPredicate;

/** A move validator that verifies the king is safe after the move is played.
 */
public class KingSafeAfterMoveValidator implements BiIntPredicate {
	private final ChessBoard board;
	private final AttackDetector detector;

	public KingSafeAfterMoveValidator(ChessBoard board, AttackDetector detector) {
		this.board = board;
		this.detector = detector;
	}

	@Override
	public boolean test(int source, int dest) {
		final Color movingColor = board.getActiveColor();
		board.moveCellsOnly(source, dest);
		final boolean result = !detector.isAttacked(board.getKingPosition(movingColor), movingColor.opposite());
		board.restoreMoveCellsOnly();
		return result; 
	}
}
