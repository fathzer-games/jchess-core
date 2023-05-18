package com.fathzer.jchess.pgn;

import java.util.LinkedList;
import java.util.List;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.Move;

public class GameHistory {
	private final Board<Move> startBoard;
	private final ChessRules rules;
	private List<Move> moves;

	public GameHistory(ChessRules rules) {
		this(rules.newGame(), rules);
	}

	public GameHistory(Board<Move> board, ChessRules rules) {
		this(board, rules, new LinkedList<>());
	}

	public GameHistory(Board<Move> board, ChessRules rules, List<Move> moves) {
		this.startBoard = board.create();
		this.startBoard.copy(board);
		this.rules = rules;
		this.moves = moves;
	}

	public void add(Move move) {
		this.moves.add(move);
	}
	
	public List<String> getPGN() {
		final MoveAlgebraicNotation an = new MoveAlgebraicNotation(rules).withPlayMove(true);
		final Board<Move> board = startBoard.create();
		board.copy(startBoard);
		final StringBuilder buf = new StringBuilder();
		final List<String> result = new LinkedList<>();
		int moveNumber = -1;
		for (Move move:moves) {
			if (board.getMoveNumber()!=moveNumber) {
				if (!buf.isEmpty()) {
					result.add(buf.toString());
				}
				moveNumber = board.getMoveNumber();
				buf.setLength(0);
				buf.append(moveNumber);
				buf.append(". ");
			} else {
				buf.append(" ");
			}
			buf.append(an.get(board, move));
		}
		if (!buf.isEmpty()) {
			result.add(buf.toString());
		}
		return result;
	}
}
