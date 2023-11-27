package com.fathzer.jchess.generic.movevalidator;

import java.util.function.Supplier;

import com.fathzer.jchess.generic.ChessBoard;

public class MoveValidatorBuilder implements Supplier<MoveValidator> {
	private static final class SimplifiedMoveValidator extends MoveValidatorBase {
		SimplifiedMoveValidator(ChessBoard board) {
			super(
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()),
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && !board.isAttacked(d.getIndex(), board.getActiveColor().opposite()),
				(s,d) -> d.getPiece()!=null && d.getPiece().getColor()==board.getActiveColor().opposite(),
				(s,d) -> d.getPiece()==null);
		}
	} 

	private static final class SimplifiedWithEnPassantMoveValidator extends MoveValidatorBase {
		private SimplifiedWithEnPassantMoveValidator(ChessBoard board) {
			super(
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()),
				(s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && !board.isAttacked(d.getIndex(), board.getActiveColor().opposite()),
				new PawnCatchValidator(board, (s,d)->true),
				(s,d) -> d.getPiece()==null);
		}
	}
	
	private final ChessBoard board;
	private final MoveValidator simplified;
	private final MoveValidator simplifiedWithEnPassant;
	private final MoveValidator checkValidator;
	private final MoveValidator pinnedMoveValidator;
	
	public MoveValidatorBuilder(ChessBoard board) {
		this.board = board;
		this.simplified = new SimplifiedMoveValidator(board);
		this.simplifiedWithEnPassant = new SimplifiedWithEnPassantMoveValidator(board);
		this.checkValidator = new CheckMoveValidator(board);
		this.pinnedMoveValidator = new PinnedMoveValidator(board);
	}

	@Override
	public MoveValidator get() {
		final MoveValidator mv;
		if (board.isCheck()) {
			mv = this.checkValidator;
		} else if (board.getPinnedDetector().hasPinned()) {
			mv = pinnedMoveValidator;
		} else {
			mv = board.getEnPassant()>=0 ? simplifiedWithEnPassant : simplified;
		}
		mv.update(board);
		return mv;
	}
}
