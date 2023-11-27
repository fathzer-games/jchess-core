package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.ChessBoard;
import com.fathzer.jchess.util.BiIntPredicate;

import lombok.Getter;

class PinnedMoveValidator implements MoveValidator {
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> others;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> king;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnCatch;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatch;
	
	PinnedMoveValidator(ChessBoard board) {
		final BiIntPredicate optimizedKingSafe = (s,d) -> board.getPinnedDetector().apply(s)==null || board.isKingSafeAfterMove(s, d);
		this.king = (s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && !board.isAttacked(d.getIndex(), board.getActiveColor().opposite());
		this.others = (s,d) -> isDestBoardExplorerOk(board.getActiveColor().opposite(), d.getPiece()) && optimizedKingSafe.test(s.getIndex(), d.getIndex());
		this.pawnNoCatch = (s,d) -> d.getPiece()==null && optimizedKingSafe.test(s.getIndex(), d.getIndex());
		this.pawnCatch = new PawnCatchValidator(board, optimizedKingSafe);
	}
	
	private boolean isDestBoardExplorerOk(Color color, Piece p) {
		return p==null || p.getColor().equals(color);
	}
}
