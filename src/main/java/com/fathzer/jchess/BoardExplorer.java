package com.fathzer.jchess;

/** A class that allows to browse board content.
 */
public interface BoardExplorer {
	/** Moves to next cell
	 * @return false if there's no more cell
	 */
	boolean next();
	/** Gets the current cell's index.
	 * @return an int
	 */
	int getIndex();
	/** Gets the piece in the current cell.
	 * @return A piece or null if is empty
	 */
	Piece getPiece();
}
