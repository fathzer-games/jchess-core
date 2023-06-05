package com.fathzer.jchess;

public interface BoardExplorer {
	void restart(int index);
	void start(Direction direction);
	void start(int rowIncrement, int columnIncrement); //TODO To be removed
	boolean hasNext();
	int next();
	int getStartPosition();
}
