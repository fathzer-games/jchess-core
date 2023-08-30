package com.fathzer.jchess.ai.evaluator;

import java.util.Comparator;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

/** A move comparator that considers a catch is better than other moves and taking a high value piece with a small value piece is better than the opposite.
 */
public class BasicMoveComparator implements Comparator<Move> {
	private Board<Move> board;
	
	public BasicMoveComparator(Board<Move> board) {
		this.board = board;
	}

	@Override
	public int compare(Move m1, Move m2) {
		// Important sort from higher to lower scores
		return getValue(m2) - getValue(m1);
	}

	public int getValue(Move m) {
		final Piece promotion = m.getPromotion();
		int value = promotion==null ? 0 : (promotion.getKind().getValue()-1)*16;
		final Piece caught = board.getPiece(m.getTo());
		if (caught==null) {
			return value;
		} else {
			value += caught.getKind().getValue()*16;
			final PieceKind catching = board.getPiece(m.getFrom()).getKind();
			return value - (catching==PieceKind.KING ? 10 : catching.getValue());
		}
	}
}
