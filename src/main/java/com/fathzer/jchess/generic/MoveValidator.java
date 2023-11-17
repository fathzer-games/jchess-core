package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.util.BiIntPredicate;
import com.fathzer.util.MemoryStats;

class MoveValidator {
	private final ChessBoard board;
	private final BiPredicate<BoardExplorer, BoardExplorer> defaultValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> kingValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnCatchValidator;
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatchValidator;
	
	MoveValidator(ChessBoard board) {
		this.board = board;
		final PinnedDetector detector = board.getPinnedDetector();
		final Color opponent = board.getActiveColor().opposite();
		AttackDetector attacks = board.getAttackDetector();
		final boolean isCheck = detector.getCheckCount()>0;
		if (isCheck || detector.hasPinned()) {
			final IntPredicate defenderDetector = isCheck ? i->true : i -> detector.apply(i)!=null;
			final BiIntPredicate optimizedKingSafe = (s,d) -> !defenderDetector.test(s) || isKingSafeAfterMove(s,d);
			this.kingValidator = isCheck ? (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && isKingSafeAfterMove(s.getIndex(), d.getIndex()) : (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && !attacks.isAttacked(d.getIndex(), opponent);
			this.defaultValidator = (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && optimizedKingSafe.test(s.getIndex(), d.getIndex());
			this.pawnNoCatchValidator = (s,d) -> d.getPiece()==null && optimizedKingSafe.test(s.getIndex(), d.getIndex());
			this.pawnCatchValidator = new PawnCatchValidator(board, this::isKingSafeAfterMove, optimizedKingSafe);
		} else {
			this.defaultValidator = (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece());
			this.kingValidator = (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && !attacks.isAttacked(d.getIndex(), opponent);
			this.pawnNoCatchValidator = (s,d) -> d.getPiece()==null;
			if (board.getEnPassant()>=0) {
				this.pawnCatchValidator = new PawnCatchValidator(board, this::isKingSafeAfterMove, (s,d)->true);
			} else {
				this.pawnCatchValidator = (s,d) -> d.getPiece()!=null && d.getPiece().getColor().equals(opponent);
			}
		}
		MemoryStats.add(this);
	}
	
	/** Checks whether the king is safe after a move is played.
	 * @param source the index of the piece's cell to move.
	 * @param dest the index of destination cell
	 */
	public boolean isKingSafeAfterMove(int source, int dest) {
		final int kingPos = board.moveOnlyCells(source, dest);
		final boolean result = !board.isAttacked(kingPos, board.getActiveColor().opposite());
		board.restoreCells();
		return result; 
	}
	
	private boolean isDestBoardExplorerOk(Color color, Piece p) {
		return p==null || p.getColor().equals(color);
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

	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe after it moves (used for king)
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	public BiPredicate<BoardExplorer, BoardExplorer> getKing() {
		return this.kingValidator;
	}
}
