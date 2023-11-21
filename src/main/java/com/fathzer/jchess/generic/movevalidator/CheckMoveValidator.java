package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.ChessBoard;

import lombok.Getter;

class CheckMoveValidator implements MoveValidator {
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> others;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> king;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnCatch;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatch;
	
	CheckMoveValidator(ChessBoard board) {
		this.king = (s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && board.isKingSafeAfterMove(s.getIndex(), d.getIndex());
		this.others = (s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && board.isKingSafeAfterMove(s.getIndex(), d.getIndex());
		this.pawnNoCatch = (s,d) -> d.getPiece()==null && board.isKingSafeAfterMove(s.getIndex(), d.getIndex());
		this.pawnCatch = new PawnCatchValidator(board, board::isKingSafeAfterMove, board::isKingSafeAfterMove);
	}
	
	private boolean isDestBoardExplorerOk(Color color, Piece p) {
		return p==null || p.getColor()==color;
	}
}
