package com.fathzer.jchess.ai;

import java.util.Comparator;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

/** A move comparator that considers a catch is better than other moves and taking a hish valie piece with a small value piece is better than the opposite.
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

	private int getValue(Move m) {
		final Piece caught = board.getPiece(m.getTo());
		if (caught==null) {
			return 0;
		} else {
			return 64 + caught.getKind().getValue() - board.getPiece(m.getFrom()).getKind().getValue();
		}
	}
}
