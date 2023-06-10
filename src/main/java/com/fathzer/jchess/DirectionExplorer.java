package com.fathzer.jchess;

/** A class that allows to browse board content in a specific direction.
 */
public interface DirectionExplorer {
	void reset(int index);
	void start(Direction direction);
	boolean next();
	Piece getPiece();
	int getIndex();
	int getStartPosition();
}
