package com.fathzer.jchess.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

public class NaiveEngine implements Function<Board<Move>, Move> {
	private static final Random RND = new Random();
	private final ToIntFunction<Board<Move>> evaluator;
	
	private final Board<Move> board;

	public NaiveEngine (Board<Move> board, ToIntFunction<Board<Move>> evaluator) {
		this.board = board;
		this.evaluator = evaluator;
	}
	
	@Override
	public Move apply(Board<Move> board) {
		List<Move> possibleMoves = board.getMoves(); 
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
		// Play the evaluated move 
		this.board.makeMove(move);
		try {
			// Gets the opponent responses
			final List<Move> moves = this.board.getMoves();
			int max = 0;
			for (int i = 0; i < moves.size(); i++) {
				// For all opponent responses
				int value = evaluateOpponentMove(moves.get(i));
				if (value>max) {
					max = value;
				}
			}
			return max;
		} finally {
			this.board.unmakeMove();
		}
	}
	
	private int evaluateOpponentMove (Move oppMove) {
		// Play the response and evaluate the obtained board
		board.makeMove(oppMove);
		try {
			return evaluator.applyAsInt(board);
		} finally {
			board.unmakeMove();
		}

	}
}
