package com.fathzer.jchess.ai.evaluator.simple;

import com.fathzer.games.Color;

public interface PositionEvaluator {
	void add(int row, int column, Color color);
	
	default void setPhase(Phase phase) {}
	
	int getValue();
	
	default int getIndex(int row, int column, Color color) {
		int index = 8*row+column;
		return color==Color.WHITE ? index : 64-index;
	}
}
