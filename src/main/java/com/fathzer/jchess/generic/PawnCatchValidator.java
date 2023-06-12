package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;

import lombok.AllArgsConstructor;

/** A class that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
 */
@AllArgsConstructor
class PawnCatchValidator implements BiPredicate<BoardExplorer, BoardExplorer> {
	private final Board<Move> board;
	private BiPredicate<BoardExplorer, BoardExplorer> basicKingSafeAfterMove;
	private BiPredicate<BoardExplorer, BoardExplorer> optimizedKingSafeAfterMove;
	
	@Override
	public boolean test(BoardExplorer from, BoardExplorer to) {
		final Color targetColor;
		final BiPredicate<BoardExplorer, BoardExplorer> kingSafeAfterMove;
		if (to.getPiece()!=null) {
			targetColor = to.getPiece().getColor();
			kingSafeAfterMove = optimizedKingSafeAfterMove;
		} else if (board.getEnPassant()==to.getIndex()) {
			targetColor = board.getCoordinatesSystem().getRow(to.getIndex())==2 ? Color.BLACK : Color.WHITE;
			// Warning, the caught pawn can be a defender of the king
			kingSafeAfterMove = basicKingSafeAfterMove;
		} else {
			// Can't catch no piece
			return false;
		}
		return !from.getPiece().getColor().equals(targetColor) && kingSafeAfterMove.test(from, to);
	}

}
