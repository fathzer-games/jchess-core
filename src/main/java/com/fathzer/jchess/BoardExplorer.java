package com.fathzer.jchess;

/** A class that allows to browse board content.
 */
public interface BoardExplorer {
	/** Set the position of the explorer.
	 * @param index The position. The behavior is unspecified ff the index is not in the board.
	 * @see CoordinatesSystem 
	 */
	void setPosition(int index);
	void setDirection(Direction direction);
	boolean next();
	int getIndex();
	Piece getPiece();
}
