package com.fathzer.jchess;

/** A class that allows to browse board content.
 */
public interface BoardExplorer {
	void reset(int index);
	void start(Direction direction);
	boolean hasNext();
	int next();
	int getStartPosition();
}
