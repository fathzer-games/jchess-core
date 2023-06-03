package com.fathzer.jchess.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.GameState;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.generic.StandardChessRules;

public class NaiveEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random();
	private final ToIntFunction<Board<Move>> evaluator;
	
	private final Board<Move> board;
	private final Board<Move> copy;
	private final ChessRules rules;

	public NaiveEngine (Board<Move> board, ToIntFunction<Board<Move>> evaluator) {
		this.board = board;
		this.copy = board.create();
		this.rules = StandardChessRules.INSTANCE;
		this.evaluator = evaluator;
	}
	
	@Override
	public Move apply(Board<Move> board) {
		GameState<Move> possibleMoves = rules.getState(board); 
		List<Evaluation<Move>> moves = IntStream.range(0, possibleMoves.size()).mapToObj(i -> {
			final Move mv = possibleMoves.get(i);
			return new Evaluation<>(mv, evaluate(mv));
		}).sorted().collect(Collectors.toList());
		System.out.println(Evaluation.toString(moves, m -> m.toString(board.getCoordinatesSystem()))); //TODO
		final double best = moves.get(0).getValue();
		List<Move> bestMoves = moves.stream().filter(m -> m.getValue()==best).map(Evaluation::getContent).collect(Collectors.toList());
		return bestMoves.get(RND.nextInt(bestMoves.size()));
	}
	
	private int evaluate(Move move) {
		this.copy.copy(board);
		// Play the evaluated move 
		this.copy.move(move);
		// Gets the opponent responses
		final GameState<Move> moves = this.rules.getState(copy);
		final Board<Move> workingCopy = copy.create();
		int max = 0;
		for (int i = 0; i < moves.size(); i++) {
			// For all opponent responses
			final Move oppMove = moves.get(i);
			workingCopy.copy(copy);
			// Play the response
			workingCopy.move(oppMove);
			int value = evaluator.applyAsInt(workingCopy);
			if (value>max) {
				max = value;
			}
		}
		return max;
	}
}
