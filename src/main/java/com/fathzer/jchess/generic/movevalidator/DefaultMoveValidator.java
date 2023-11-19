package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.ChessBoard;
import com.fathzer.jchess.generic.PinnedDetector;
import com.fathzer.jchess.util.BiIntPredicate;
import com.fathzer.util.MemoryStats;

import lombok.Getter;

class DefaultMoveValidator implements MoveValidator {
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> others;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> king;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnCatch;
	@Getter
	private BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatch;
	
	DefaultMoveValidator(ChessBoard board) {
		final PinnedDetector detector = board.getPinnedDetector();
		final Color opponent = board.getActiveColor().opposite();
		final boolean isCheck = detector.getCheckCount()>0;
		final IntPredicate defenderDetector = isCheck ? i->true : i -> detector.apply(i)!=null;
		final BiIntPredicate optimizedKingSafe = (s,d) -> !defenderDetector.test(s) || board.isKingSafeAfterMove(s, d);
		this.king = isCheck ? (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && board.isKingSafeAfterMove(s.getIndex(), d.getIndex()) : (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && !board.isAttacked(d.getIndex(), opponent);
		this.others = (s,d) -> isDestBoardExplorerOk(opponent, d.getPiece()) && optimizedKingSafe.test(s.getIndex(), d.getIndex());
		this.pawnNoCatch = (s,d) -> d.getPiece()==null && optimizedKingSafe.test(s.getIndex(), d.getIndex());
		this.pawnCatch = new PawnCatchValidator(board, board::isKingSafeAfterMove, optimizedKingSafe);
		MemoryStats.add(this);
	}
	
	private boolean isDestBoardExplorerOk(Color color, Piece p) {
		return p==null || p.getColor().equals(color);
	}
}
