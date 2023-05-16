package com.fathzer.jchess;

import com.fathzer.games.Color;

public interface Board<M> {
	Dimension getDimension();
	Color getActiveColor();
	/** Gets the enPassant cell
	 * @return a negative number if there's no enPassant cell
	 */
	int getEnPassant();
	int getHalfMoveCount();
	int getMoveNumber();
	int getKingPosition(Color color);
	void move(M move);
	boolean hasCastling(Castling c);

	/** Gets the initial rook position of a castling.
	 * @param castling The castling
	 * @return The initial position of the rook involved in the castling.
	 */
	int getInitialRookPosition(Castling castling);	default Piece getPiece(int position) {
		return null;
	}

	long getKey();

	/** Creates an empty board of the same class as this.
	 * @return a new Board.
	 */
	default Board<M> create() {
		throw new UnsupportedOperationException();
	}
	
	/** Copy another board in this.
	 * @param board The other board, should have the same dimensions as this.
	 * @throws UnsupportedOperationException if copying the argument is not supported by this board.
	 */
	default void copy(Board<M> board) {
		throw new UnsupportedOperationException();
	}

	/** Creates a new empty move list.
	 * @return a new move list
	 */
	ChessGameState newMoveList();
	
	void moveCellsOnly(int from, int to);
	void restoreMoveCellsOnly();
}
