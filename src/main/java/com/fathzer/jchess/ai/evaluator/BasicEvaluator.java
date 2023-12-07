package com.fathzer.jchess.ai.evaluator;

import static com.fathzer.games.Color.*;

import com.fathzer.games.Color;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.Setter;

public class BasicEvaluator implements Evaluator<Move, Board<Move>> {
	@Setter
	private Color viewPoint;
	private final Board<Move> board;

	public BasicEvaluator(Board<Move> board) {
		this.board = board;
	}

	@Override
	public int evaluate() {
		int points = 100*getPoints();
		if (BLACK==viewPoint || (viewPoint==null && BLACK==board.getActiveColor())) {
			points = -points;
		}
		return points;
	}

	public int getPoints() {
		final BoardExplorer exp = board.getExplorer(); 
		int points = 0;
		do {
			final Piece p = exp.getPiece();
			if (p!=null) {
				int inc = p.getKind().getValue();
				if (p.getColor()==WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		} while (exp.next());
		return points;
	}
}
