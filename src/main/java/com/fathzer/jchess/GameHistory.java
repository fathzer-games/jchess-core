package com.fathzer.jchess;

import java.util.LinkedList;
import java.util.List;

public class GameHistory {
	private final Board<Move> startBoard;
	private final List<Move> moves;

	public GameHistory(Board<Move> board) {
		this(board, new LinkedList<>());
	}

	public GameHistory(Board<Move> board, List<Move> moves) {
		this.startBoard = (Board<Move>) board.fork();
		this.moves = moves;
	}

	public void add(Move move) {
		this.moves.add(move);
	}

	public Board<Move> getStartBoard() {
		return startBoard;
	}

	public List<Move> getMoves() {
		return moves;
	}
}
