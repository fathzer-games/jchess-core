package com.fathzer.jchess.ai;

import java.util.List;

import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.GamePosition;


import lombok.Getter;

//TODO Maybe this could be promoted to games-core 
class BasicGamePosition<M,T extends MoveGenerator<M>> implements GamePosition<M>, HashProvider {
	@FunctionalInterface
	public interface LongBuilder<V> {
		long get(V arg);
	}
	
	@Getter
	private final T board;
	private final Evaluator<T> evaluator;
	private final LongBuilder<T> getHash;
	private final Stat stat;

	public BasicGamePosition(T board, Evaluator<T> evaluator, LongBuilder<T> getHash, Stat stat) {
		this.board = board;
		this.evaluator = evaluator;
		this.getHash = getHash;
		this.stat = stat;
	}

	@Override
	public void makeMove(M move) {
		stat.movesPlayed.incrementAndGet();
		board.makeMove(move);
	}

	@Override
	public List<M> getMoves() {
		stat.moveGenerations.incrementAndGet();
		final List<M> moves = board.getMoves();
		stat.generatedMoves.addAndGet(moves.size());
		return moves;
	}

	@Override
	public void unmakeMove() {
		board.unmakeMove();
	}

	@Override
	public Status getStatus() {
		return board.getStatus();
	}

	@Override
	public long getHashKey() {
		return getHash.get(board);
	}

	@Override
	public int evaluate() {
		return evaluator.evaluate(board);
	}
}