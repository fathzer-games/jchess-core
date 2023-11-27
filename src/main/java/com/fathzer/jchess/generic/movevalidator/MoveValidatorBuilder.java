package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.generic.ChessBoard;

import lombok.Getter;

public class MoveValidatorBuilder implements Supplier<MoveValidator> {
	private static class DefaultMoveValidator implements MoveValidator {
		@Getter
		protected BiPredicate<BoardExplorer, BoardExplorer> others;
		@Getter
		protected BiPredicate<BoardExplorer, BoardExplorer> king;
		@Getter
		protected BiPredicate<BoardExplorer, BoardExplorer> pawnCatch;
		@Getter
		protected BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatch;
		
		private DefaultMoveValidator(ChessBoard board) {
			this.others = (s,d) -> d.getPiece()==null || d.getPiece().getColor()!=board.getActiveColor();
			this.pawnCatch = (s,d) -> d.getPiece()!=null && d.getPiece().getColor()!=board.getActiveColor();
			this.pawnNoCatch = (s,d) -> d.getPiece()==null;
			this.king = this.others.and((s,d) -> !board.isAttacked(d.getIndex(), board.getActiveColor().opposite()));
		}
	}
	
	private static class CheckMoveValidator extends DefaultMoveValidator {
		private CheckMoveValidator(ChessBoard board) {
			super(board);
			final BiPredicate<BoardExplorer, BoardExplorer> kingSafe = (s,d) -> board.isKingSafeAfterMove(s.getIndex(), d.getIndex());
			this.others = this.others.and(kingSafe);
			this.king = this.others;
			this.pawnNoCatch = this.pawnNoCatch.and(kingSafe);
			this.pawnCatch = this.pawnCatch.and(kingSafe);
		}
	}
	
	private final ChessBoard board;
	private final MoveValidator simplified;
	private final MoveValidator checkValidator;
	
	public MoveValidatorBuilder(ChessBoard board) {
		this.board = board;
		this.simplified = new DefaultMoveValidator(board);
		this.checkValidator = new CheckMoveValidator(board);
	}

	@Override
	public MoveValidator get() {
		final MoveValidator mv;
		if (board.isCheck()) {
			mv = this.checkValidator;
		} else {
			mv = simplified;
		}
		mv.update(board);
		return mv;
	}
}
