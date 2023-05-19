package com.fathzer.jchess;

import java.util.LinkedList;
import java.util.List;

public class GameHistory {
	private final Board<Move> startBoard;
	private final ChessRules rules;
	private final List<Move> moves;

	public GameHistory(ChessRules rules) {
		this(rules, rules.newGame(), new LinkedList<>());
	}

	public GameHistory(ChessRules rules, Board<Move> board, List<Move> moves) {
		this.startBoard = board.create();
		this.startBoard.copy(board);
		this.rules = rules;
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

	public ChessRules getRules() {
		return rules;
	}
}
