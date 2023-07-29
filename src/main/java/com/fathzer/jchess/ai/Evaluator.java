package com.fathzer.jchess.ai;

import com.fathzer.games.Color;

public interface Evaluator<T> {
	/** Sets the point of view from which the evaluation should be made. 
	 * @param color The color from which the evaluation is made, null to evaluate the position from the point of view of the current player.
	 */
	void setViewPoint(Color color);
	
	/** Evaluates a board's position.
	 * @param board The board to evaluate
	 * @return An integer
	 */
	int evaluate(T board);
}
