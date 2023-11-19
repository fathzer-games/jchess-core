package com.fathzer.jchess.generic.movevalidator;

import java.util.function.Supplier;

import com.fathzer.jchess.generic.ChessBoard;
import com.fathzer.util.MemoryStats;

public class MoveValidatorBuilder implements Supplier<MoveValidator> {
	private static final class SimplifiedMoveValidator extends MoveValidatorBase {
		SimplifiedMoveValidator(ChessBoard board) {
			super(
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()),
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && !board.isAttacked(d.getIndex(), board.getActiveColor().opposite()),
				(s,d) -> d.getPiece()!=null && d.getPiece().getColor().equals(board.getActiveColor().opposite()),
				(s,d) -> d.getPiece()==null);
		}
	} 

	private static final class SimplifiedWithEnPassantMoveValidator extends MoveValidatorBase {
		private SimplifiedWithEnPassantMoveValidator(ChessBoard board) {
			super(
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()),
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && !board.isAttacked(d.getIndex(), board.getActiveColor().opposite()),
				new PawnCatchValidator(board, board::isKingSafeAfterMove, (s,d)->true),
				(s,d) -> d.getPiece()==null);
		}
	}
	
	private final ChessBoard board;
	private final MoveValidator simplified;
	private final MoveValidator simplifiedWithEnPassant;
	
	public MoveValidatorBuilder(ChessBoard board) {
		this.board = board;
		this.simplified = new SimplifiedMoveValidator(board);
		this.simplifiedWithEnPassant = new SimplifiedWithEnPassantMoveValidator(board);
		MemoryStats.add(this);
	}

	@Override
	public MoveValidator get() {
		if (!board.isCheck() && !board.getPinnedDetector().hasPinned()) {
			return board.getEnPassant()>=0 ? simplifiedWithEnPassant : simplified;
		}
		return new DefaultMoveValidator(board);
	}
}
