package com.fathzer.jchess.ai;

import java.util.Random;
import java.util.function.Function;

import com.fathzer.games.GameState;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ChessRules;

/** A trivial engine that randomly plays a possible move.
 */
public class RandomEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random();
	private ChessRules rules;
	
	public RandomEngine(ChessRules rules) {
		this.rules = rules;
	}
	
	@Override
	public Move apply(Board<Move> board) {
		final GameState<Move> possibleMoves = rules.getState(board);
		return possibleMoves.get(RND.nextInt(possibleMoves.size()));
	}
}
