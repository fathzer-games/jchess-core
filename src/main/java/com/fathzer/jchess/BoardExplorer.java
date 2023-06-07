package com.fathzer.jchess;

/** A class that allows to browse board content.
 */
public interface BoardExplorer {
	void reset(int index);
	void setDirection(Direction direction);
	boolean next();
	int getIndex();
	Piece getPiece();
	int getStartPosition();
}
