package com.fathzer.jchess.ai;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.ai.evaluator.BasicMoveComparator;

public class StrictMoveComparator extends BasicMoveComparator {

	public StrictMoveComparator(Board<Move> board) {
		super(board);
	}

	@Override
	public int compare(Move m1, Move m2) {
		if (m1.equals(m2)) {
			return 0;
		}
		int cmp = super.compare(m1, m2);
		if (cmp==0) {
			cmp = m1.getFrom() - m2.getFrom();
			if (cmp==0) {
				cmp = m1.getTo() - m2.getTo();
				if (cmp==0) {
					// pawn promotions to Bishop or knight
					return m1.getPromotion().getKind()==PieceKind.KNIGHT ? 1 : -1;
				}
			}
		}
		return cmp;
	}
}
