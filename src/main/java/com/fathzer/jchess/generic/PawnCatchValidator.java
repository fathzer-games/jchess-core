package com.fathzer.jchess.generic;

import java.util.function.BiPredicate;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.util.BiIntPredicate;

import lombok.AllArgsConstructor;

/** A class that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
 */
@AllArgsConstructor
class PawnCatchValidator implements BiPredicate<BoardExplorer, BoardExplorer> {
	private final Board<Move> board;
	private BiIntPredicate basicKingSafeAfterMove;
	private BiIntPredicate optimizedKingSafeAfterMove;
	
	@Override
	public boolean test(BoardExplorer from, BoardExplorer to) {
		final Color targetColor;
		final BiIntPredicate kingSafeAfterMove;
		if (board.getEnPassant()==to.getIndex()) {
			targetColor = board.getCoordinatesSystem().getRow(to.getIndex())==2 ? Color.BLACK : Color.WHITE;
			// Warning, the caught pawn can be a defender of the king
			kingSafeAfterMove = basicKingSafeAfterMove;
		} else if (to.getPiece()==null) {
			// Can't catch no piece
			return false;
		} else {
			// Standard piece catch
			targetColor = to.getPiece().getColor();
			kingSafeAfterMove = optimizedKingSafeAfterMove;
		}
		return !from.getPiece().getColor().equals(targetColor) && kingSafeAfterMove.test(from.getIndex(), to.getIndex());
	}

}
