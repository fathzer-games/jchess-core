package com.fathzer.jchess.ai.evaluator.simple;

import com.fathzer.games.Color;

class PawnPositionEvaluator implements PositionEvaluator {
	private int value = 0;

	private static final int[] VALUES = new int[] {
			0,  0,  0,  0,  0,  0,  0,  0,
			50, 50, 50, 50, 50, 50, 50, 50,
			10, 10, 20, 30, 30, 20, 10, 10,
			 5,  5, 10, 25, 25, 10,  5,  5,
			 0,  0,  0, 20, 20,  0,  0,  0,
			 5, -5,-10,  0,  0,-10, -5,  5,
			 5, 10, 10,-20,-20, 10, 10,  5,
			 0,  0,  0,  0,  0,  0,  0,  0};

	
	@Override
	public void add(int row, int column, Color color) {
		final int index = getIndex(row, column, color);
		if (color==Color.WHITE) { 
			value = value + VALUES[index];
		} else {
			value = value - VALUES[index];
		}
	}

	@Override
	public int getValue() {
		return value;
	}
}
