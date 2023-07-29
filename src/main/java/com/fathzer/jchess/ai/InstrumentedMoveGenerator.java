package com.fathzer.jchess.ai;

import java.util.Comparator;
import java.util.List;

import com.fathzer.games.Status;
import com.fathzer.games.HashProvider;
import com.fathzer.games.ai.GamePosition;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

import lombok.Getter;

class InstrumentedMoveGenerator implements GamePosition<Move>, HashProvider {
	@Getter
	private final Board<Move> board;
	private final ChessEvaluator evaluator;
	private final Stat stat;
	private final Comparator<Move> cmp;

	public InstrumentedMoveGenerator(Board<Move> board, ChessEvaluator evaluator, Stat stat) {
		this.board = board;
		this.evaluator = evaluator;
		this.stat = stat;
		this.cmp = new BasicMoveComparator(board);
	}

	@Override
	public void makeMove(Move move) {
		stat.movesPlayed.incrementAndGet();
		board.makeMove(move);
	}

	@Override
	public List<Move> getMoves() {
		stat.moveGenerations.incrementAndGet();
		final List<Move> moves = board.getMoves();
		moves.sort(cmp); //TODO Remove from here
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
		return board.getHashKey();
	}

	@Override
	public int evaluate() {
		return evaluator.evaluate(board);
	}
}