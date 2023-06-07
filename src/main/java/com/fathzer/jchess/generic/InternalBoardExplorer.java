package com.fathzer.jchess.generic;

import com.fathzer.jchess.Direction;

/** A class that allows to browse board content.
 */
interface InternalBoardExplorer {
	void reset(int index);
	void start(Direction direction);
	boolean hasNext();
	int next();
	int getStartPosition();
}
