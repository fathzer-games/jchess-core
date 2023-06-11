package com.fathzer.jchess.generic;

import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.util.BiIntPredicate;

class MoveValidator {
	private final BiIntPredicate defaultValidator;
	private final BiIntPredicate kingValidator;
	private final BiIntPredicate pawnCatchValidator;
	private final BiIntPredicate pawnNoCatchValidator;

	MoveValidator(Board<Move> board, AttackDetector attacks, PinnedDetector detector) {
		final Color opponent = board.getActiveColor().opposite();
		boolean isCheck = detector.getCheckCount()>0;
		final IntPredicate defenderDetector = isCheck ? i->true : i -> detector.apply(i)!=null;

		final BiIntPredicate kingSafeAfterMove = new KingSafeAfterMoveValidator(board, attacks);
		final BiIntPredicate optimizedKingSafe = (s,d) -> !defenderDetector.test(s) || kingSafeAfterMove.test(s,d);
		this.kingValidator = isCheck ? (s,d) -> isDestCellOk(board, d) && kingSafeAfterMove.test(s, d) : (s,d) -> isDestCellOk(board, d) && !attacks.isAttacked(d, opponent);
		this.defaultValidator = (s,d) -> isDestCellOk(board, d) && optimizedKingSafe.test(s, d);
		this.pawnNoCatchValidator = (s,d) -> board.getPiece(d)==null && optimizedKingSafe.test(s, d);
		this.pawnCatchValidator = new PawnCatchValidator(board, kingSafeAfterMove, optimizedKingSafe);
	}
	
	private boolean isDestCellOk(Board<Move> board, int d) {
		return board.getPiece(d)==null || !board.getPiece(d).getColor().equals(board.getActiveColor());
	}

	/** Gets a predicate that checks the destination is free and king is safe (used when pawn is moving vertically).
	 * @return
	 */
	public BiIntPredicate getPawnNoCatch() {
		return this.pawnNoCatchValidator;
	}

	/** Gets a predicate that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
	 * @return a BiIntPredicate
	 */
	public BiIntPredicate getPawnCatch() {
		return this.pawnCatchValidator;
	}
	
	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe (used for all pieces except pawns and king)
	 * @return a BiIntPredicate
	 */
	public BiIntPredicate getDefault() {
		return this.defaultValidator;
	}

	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe after it won move (used for king)
	 * @return a BiIntPredicate
	 */
	public BiIntPredicate getKing() {
		return this.kingValidator;
	}
}
