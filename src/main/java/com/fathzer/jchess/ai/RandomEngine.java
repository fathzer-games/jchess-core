package com.fathzer.jchess.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

/** A trivial engine that randomly plays a possible move.
 */
public class RandomEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random();
	
	@Override
	public Move apply(Board<Move> board) {
		final List<Move> possibleMoves = board.getMoves();
		return possibleMoves.get(RND.nextInt(possibleMoves.size()));
	}
}
