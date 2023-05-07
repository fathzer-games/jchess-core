package com.fathzer.jchess.generic;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.util.BiIntPredicate;

import lombok.AllArgsConstructor;

/** A class that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
 */
@AllArgsConstructor
class PawnCatchValidator implements BiIntPredicate {
	private final Board<Move> board;
	private BiIntPredicate basicKingSafeAfterMove;
	private BiIntPredicate optimizedKingSafeAfterMove;
	
	@Override
	public boolean test(int from, int to) {
		final Color targetColor;
		final BiIntPredicate kingSafeAfterMove;
		if (board.getPiece(to)!=null) {
			targetColor = board.getPiece(to).getColor();
			kingSafeAfterMove = optimizedKingSafeAfterMove;
		} else if (board.getEnPassant()==to) {
			targetColor = board.getDimension().getRow(to)==2 ? Color.BLACK : Color.WHITE;
			// Warning, the caught pawn can be a defender of the king
			kingSafeAfterMove = basicKingSafeAfterMove;
		} else {
			// Can't catch no piece
			return false;
		}
		return !board.getPiece(from).getColor().equals(targetColor) && kingSafeAfterMove.test(from, to);
	}

}
