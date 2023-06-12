package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

class MoveValidator {
	private final BiPredicate<BoardExplorer, BoardExplorer> defaultValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> kingValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnCatchValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatchValidator;

	MoveValidator(Board<Move> board, AttackDetector attacks, PinnedDetector detector) {
		final Color opponent = board.getActiveColor().opposite();
		boolean isCheck = detector.getCheckCount()>0;
		final IntPredicate defenderDetector = isCheck ? i->true : i -> detector.apply(i)!=null;

		final BiPredicate<BoardExplorer, BoardExplorer> kingSafeAfterMove = new KingSafeAfterMoveValidator(board, attacks);
		final BiPredicate<BoardExplorer, BoardExplorer> optimizedKingSafe = (s,d) -> !defenderDetector.test(s.getIndex()) || kingSafeAfterMove.test(s,d);
		this.kingValidator = isCheck ? (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && kingSafeAfterMove.test(s, d) : (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && !attacks.isAttacked(d.getIndex(), opponent);
		this.defaultValidator = (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && optimizedKingSafe.test(s, d);
		this.pawnNoCatchValidator = (s,d) -> d.getPiece()==null && optimizedKingSafe.test(s, d);
		this.pawnCatchValidator = new PawnCatchValidator(board, kingSafeAfterMove, optimizedKingSafe);
	}
	
	private boolean isDestBoardExplorerOk(Board<Move> board, Piece p) {
		return p==null || !p.getColor().equals(board.getActiveColor());
	}

	/** Gets a predicate that checks the destination is free and king is safe (used when pawn is moving vertically).
	 * @return
	 */
	public BiPredicate<BoardExplorer, BoardExplorer> getPawnNoCatch() {
		return this.pawnNoCatchValidator;
	}

	/** Gets a predicate that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	public BiPredicate<BoardExplorer, BoardExplorer> getPawnCatch() {
		return this.pawnCatchValidator;
	}
	
	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe (used for all pieces except pawns and king)
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	public BiPredicate<BoardExplorer, BoardExplorer> getDefault() {
		return this.defaultValidator;
	}

	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe after it won move (used for king)
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	public BiPredicate<BoardExplorer, BoardExplorer> getKing() {
		return this.kingValidator;
	}
}
