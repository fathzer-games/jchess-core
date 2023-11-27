package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;

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
	private BiIntPredicate optimizedKingSafeAfterMove;
	
	@Override
	public boolean test(BoardExplorer from, BoardExplorer to) {
		if (to.getPiece()==null) {
			// Can't catch no piece
			return false;
		} else {
			// Standard piece catch
			return !to.getPiece().getColor().equals(board.getActiveColor()) && optimizedKingSafeAfterMove.test(from.getIndex(), to.getIndex());
		}
	}

}
