package com.fathzer.jchess.generic.movevalidator;

import java.util.function.BiPredicate;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.generic.ChessBoard;

public abstract interface MoveValidator {

	/** Gets a predicate that checks the destination is free and king is safe (used when pawn is moving vertically).
	 * @return
	 */
	BiPredicate<BoardExplorer, BoardExplorer> getPawnNoCatch();

	/** Gets a predicate that checks the destination of a move can be caught by a pawn and king remains safe after the move (used for pawn's diagonal moves).
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	BiPredicate<BoardExplorer, BoardExplorer> getPawnCatch();
	
	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe (used for all pieces except pawns and king)
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	BiPredicate<BoardExplorer, BoardExplorer> getOthers();

	/** Gets a predicate that checks the destination is free or occupied by the opposite color and king is safe after it moves (used for king)
	 * @return a BiPredicate<BoardExplorer, BoardExplorer>
	 */
	BiPredicate<BoardExplorer, BoardExplorer> getKing();
	
	default void update(ChessBoard board) {
		
	}
}
