package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.util.BiIntPredicate;

class MoveValidator {
	private final BiPredicate<BoardExplorer, BoardExplorer> defaultValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> kingValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnCatchValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatchValidator;
	private final AttackDetector attacks;
	
	MoveValidator(Board<Move> board, PinnedDetector detector) {
		final Color opponent = board.getActiveColor().opposite();
		this.attacks = new AttackDetector(board.getDirectionExplorer(-1));
		final boolean isCheck = detector.getCheckCount()>0;
		final boolean hasPinned = detector.hasPinned();
		if (isCheck || hasPinned || board.getEnPassant()>=0) {
			final IntPredicate defenderDetector = isCheck ? i->true : i -> detector.apply(i)!=null;
			final BiIntPredicate kingSafeAfterMove = new KingSafeAfterMoveValidator(board, attacks);
			final BiIntPredicate optimizedKingSafe = (s,d) -> !defenderDetector.test(s) || kingSafeAfterMove.test(s,d);
			this.kingValidator = isCheck ? (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && kingSafeAfterMove.test(s.getIndex(), d.getIndex()) : (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && !attacks.isAttacked(d.getIndex(), opponent);
			this.defaultValidator = (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && optimizedKingSafe.test(s.getIndex(), d.getIndex());
			this.pawnNoCatchValidator = (s,d) -> d.getPiece()==null && optimizedKingSafe.test(s.getIndex(), d.getIndex());
			this.pawnCatchValidator = new PawnCatchValidator(board, kingSafeAfterMove, optimizedKingSafe);
		} else {
			this.defaultValidator = (s,d) -> isDestBoardExplorerOk(board, d.getPiece());
			this.kingValidator = (s,d) -> isDestBoardExplorerOk(board, d.getPiece()) && !attacks.isAttacked(d.getIndex(), opponent);
			this.pawnNoCatchValidator = (s,d) -> d.getPiece()==null;
			this.pawnCatchValidator = (s,d) -> d.getPiece()!=null && d.getPiece().getColor().equals(opponent);
		}
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
	
	public boolean isThreatened(Color color, IntStream positions) {
		return positions.anyMatch(pos -> attacks.isAttacked(pos, color));
	}
}
