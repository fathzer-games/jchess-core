package com.fathzer.jchess;

/** A class that allows to browse board content in a specific direction.
 */
public interface DirectionExplorer extends BoardExplorer {
	void start(Direction direction);
	int getStartPosition();
}
