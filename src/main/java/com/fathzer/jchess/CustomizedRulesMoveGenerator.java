package com.fathzer.jchess;

import com.fathzer.games.GameState;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Rules;

public class CustomizedRulesMoveGenerator implements MoveGenerator<Move>  {
	private Rules<Board<Move>, Move> rules;
	private Board<Move> board;
	
	public CustomizedRulesMoveGenerator(Rules<Board<Move>, Move> rules, Board<Move> board) {
		this.rules = rules;
		this.board = board;
	}

	@Override
	public void makeMove(Move move) {
		board.makeMove(move);
	}

	@Override
	public GameState<Move> getState() {
		return rules.getState(board);
	}

	@Override
	public void unmakeMove() {
		board.unmakeMove();
	}

	public Board<Move> getBoard() {
		return board;
	}
}
