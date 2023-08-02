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

    /** Gets the score obtained for a win after nbMoves moves.
     * <br>The default value is Short.MAX_VALUE - nbMoves
     * @param nbMoves The number of moves needed to win.
     * @return a positive int &gt; to any value returned by {@link #evaluate()}
     */
	default int getWinScore(int nbMoves) {
		return Short.MAX_VALUE-nbMoves;
	}

	/** The inverse function of {@link #getWinScore(int)}
	 * @param winScore a score returned by {@link #getWinScore(int)} 
	 * @return The number of moves passed to {@link #getWinScore(int)}
	 */
	default int getNbMovesToWin(int winScore) {
		return Short.MAX_VALUE-winScore;
	}
}
