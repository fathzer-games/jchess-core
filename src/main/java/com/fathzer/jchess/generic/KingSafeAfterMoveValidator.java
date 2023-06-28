package com.fathzer.jchess.generic;

import com.fathzer.jchess.util.BiIntPredicate;

/** A move validator that verifies the king is safe after the move is played.
 */
public class KingSafeAfterMoveValidator implements BiIntPredicate {
	private final ChessBoard board;
	private final AttackDetector detector;

	public KingSafeAfterMoveValidator(ChessBoard board, AttackDetector detector) {
		this.board = board;
		this.detector = detector;
		this.board.saveCells();
	}

	@Override
	public boolean test(int source, int dest) {
		final int kingPos = board.moveOnlyCells(source, dest);
		final boolean result = !detector.isAttacked(kingPos, board.getActiveColor().opposite());
		board.restoreCells();
		return result; 
	}
}
