package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;

import lombok.Getter;

abstract class MoveValidatorBase implements MoveValidator {
	@Getter
	private final BiPredicate<BoardExplorer, BoardExplorer> others;
	@Getter
	private final BiPredicate<BoardExplorer, BoardExplorer> king;
	@Getter
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnCatch;
	@Getter
	private final BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatch;
	
	protected MoveValidatorBase(
			BiPredicate<BoardExplorer, BoardExplorer> defaultValidator,
			BiPredicate<BoardExplorer, BoardExplorer> kingValidator,
			BiPredicate<BoardExplorer, BoardExplorer> pawnCatchValidator,
			BiPredicate<BoardExplorer, BoardExplorer> pawnNoCatchValidator) {
		this.others = defaultValidator;
		this.king = kingValidator;
		this.pawnCatch = pawnCatchValidator;
		this.pawnNoCatch = pawnNoCatchValidator;
	}
	
	protected static boolean isDestBoardExplorerOk(Color color, Piece p) {
		return p==null || p.getColor()==color;
	}
}
