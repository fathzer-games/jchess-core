package com.fathzer.jchess.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.jchess.Move;
import com.fathzer.games.Status;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;

import lombok.Getter;
import lombok.Setter;

public class BasicMoveList implements ChessGameState {
	private static final int MAX_POSSIBLE_MOVES = 218;
	private List<Move> moves;
	@Getter
	@Setter
	private Status status;
	
	public BasicMoveList() {
		moves = new ArrayList<>(MAX_POSSIBLE_MOVES);
		this.status = Status.PLAYING;
	}

	@Override
	public void add(int from, int to) {
		moves.add(BasicMove.get(from, to));
	}

	@Override
	public void add(int from, int to, Piece promotion) {
		moves.add(new BasicMove(from, to) {
			@Override
			public Piece promotedTo() {
				return promotion;
			}
		});
	}

	@Override
	public Move get(int index) {
		return moves.get(index);
	}

	@Override
	public int size() {
		return moves.size();
	}

	@Override
	public void sort(IntUnaryOperator evaluator) {
		moves = IntStream.range(0, size()).mapToObj(i -> new Evaluation<Move>(get(i), evaluator.applyAsInt(i))).
			sorted().map(Evaluation::getContent).collect(Collectors.toList());
	}
}