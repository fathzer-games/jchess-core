package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;

import com.fathzer.games.Color;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

import lombok.Setter;

public class BasicEvaluator implements Evaluator<Board<Move>> {
	@Setter
	private Color viewPoint;
	
	public int evaluate(Board<Move> board) {
		int points = 100*getPoints(board);
		if (BLACK==viewPoint || (viewPoint==null && BLACK==board.getActiveColor())) {
			points = -points;
		}
		return points;
	}

	private int getPoints(Board<Move> board) {
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
