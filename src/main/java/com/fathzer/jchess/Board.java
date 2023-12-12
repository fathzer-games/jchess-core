package com.fathzer.jchess;

import java.util.Comparator;
import java.util.function.Function;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;

/** A chess board.
 * @param <M> The class that represents a move.
 */
public interface Board<M> extends MoveGenerator<M>, HashProvider {
	Dimension getDimension();
	/** Many methods in this interface use an int index to identify a position on the board.
	 * Don't make any assumptions on how these values are related to row and columns.
	 * Use the CoordinatesSystem instance returned by the this method of this class.
	 * @return a CoordinatesSystem
	*/
	CoordinatesSystem getCoordinatesSystem();
	
	BoardExplorer getExplorer();
	DirectionExplorer getDirectionExplorer(int index);
	
	Color getActiveColor();
	
	/** Gets the enPassant cell
	 * @return a negative number if there's no enPassant cell or the internal en passant cell index
	 * @see #getCoordinatesSystem()
	 */
	int getEnPassant();
	int getHalfMoveCount();
	boolean isInsufficientMaterial();
	boolean isDrawByRepetition();
	int getMoveNumber();
	int getKingPosition(Color color);
	boolean hasCastling(Castling c);
	boolean isCheck();

	/** Gets the initial rook position of a castling.
	 * @param castling The castling
	 * @return The initial position of the rook involved in the castling.
	 * @see Board#getCoordinatesSystem()
	 */
	int getInitialRookPosition(Castling castling);
	
	/** Gets the king's destination of a castling.
	 * @param castling The castling
	 * @return The king's position after the castling.
	 * @see Board#getCoordinatesSystem()
	 */
	int getKingDestination(Castling castling);

	/** Tests whether a king's move is a castling or not and, if it is the case, returns the corresponding castling.
	 * <br>Please note this method is called by this class only when king is moving. Calling it on other moves
	 * may produce unpredictable results.
	 * @param from The king's starting position.
	 * @param to The king's end position.
	 * <br>Please note this position is the representation of the king's destination in the encoded move,
	 * not necessarily the 'effective' king's position after the move. For example, in chess360, the castling move
	 * is encoded as 'king moves to the rook it castles with', but the 'effective' end position is the same as in standard chess.
	 * @return The castling if the move is a castling, null if it is a king standard move.
	 * <br>The default implementation, returns true if the king moves more than 1 cell or on a cell occupied by a rook of the same color).
	 */
	default Castling getCastling(int from, int to) {
		final int offset = Math.abs(to-from);
		boolean castling = offset>=2 && (getCoordinatesSystem().getRow(from)==getCoordinatesSystem().getRow(to));
		if (!castling) {
			final Piece rook = Color.WHITE.equals(getActiveColor()) ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
			castling = rook.equals(getPiece(to));
		}
		return castling ? Castling.get(getActiveColor(), to>from) : null;
	}
	
	Piece getPiece(int position);
	
    /**
     * Gets the current game status.
     * @return a status
     */
	Status getStatus();
	
	Function<Board<Move>, Comparator<Move>> getMoveComparatorBuilder();

	void setMoveComparatorBuilder(Function<Board<Move>, Comparator<Move>> moveComparatorBuilder);
}
