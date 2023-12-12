package com.fathzer.jchess.ai;

import java.util.function.Function;

import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchContextBuilder {
	public static SearchContext<Move, Board<Move>> get(Function<Board<Move>, Evaluator<Move, Board<Move>>> evaluatorBuilder, Board<Move> board) {
		final Board<Move> b = (Board<Move>) board.fork();
		final Evaluator<Move, Board<Move>> evaluator = evaluatorBuilder.apply(b);
		evaluator.setViewPoint(b.getActiveColor());
		return new SearchContext<>(b, evaluator);
	}
}
