package com.fathzer.jchess.generic;

import static com.fathzer.games.Color.*;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ai.ChessEvaluator;

import lombok.Setter;

public class BasicEvaluator implements ChessEvaluator {
	@Setter
	private Color viewPoint;
	
	public int evaluate(Board<Move> board) {
//		System.out.println(board); //TODO
		int points = 0;
		for (int i = 0; i < board.getDimension().getSize(); i++) {
			final Piece p = board.getPiece(i);
			if (p!=null) {
				int inc = p.getKind().getValue();
				if (p.getColor()==WHITE) {
					points += inc;
				} else {
					points -= inc;
				}
			}
		}
		points = 100*points;
		if (BLACK==viewPoint || (viewPoint==null && BLACK==board.getActiveColor())) {
			points = -points;
		}
//		System.out.println(points);
		return points;
	}
}
