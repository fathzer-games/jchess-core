package com.fathzer.jchess;

public interface BoardExplorer {
	void restart(int index);
	void start(Direction direction);
	boolean hasNext();
	int next();
	int getStartPosition();
}
