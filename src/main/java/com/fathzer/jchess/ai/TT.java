package com.fathzer.jchess.ai;

import com.fathzer.games.ai.transposition.OneLongEntryTranspositionTable;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.BasicMove;

class TT extends OneLongEntryTranspositionTable<Move> {
	// Move is encoded in an int:
	// 12 bits for source, 12 bits for destination
	// 8 bits for promotion
	
	private static final int FROM_MASK = 0xfff;
	private static final int TO_MASK = 0xfff000;
	private static final int TO_OFFSET = 12;
	private static final int PROMOTION_OFFSET = 24;
	
	public TT(int size, SizeUnit unit) {
		super(size, unit);
	}

	@Override
	protected int toInt(Move move) {
		if (move==null) {
			return 0;
		}
		int result = move.getFrom() | (move.getTo()<<TO_OFFSET);
		if (move.getPromotion()!=null) {
			result |= ((move.getPromotion().ordinal()+1)<<PROMOTION_OFFSET);
		}
		return result;
	}

	@Override
	protected Move toMove(int value) {
		if (value==0) {
			return null;
		}
		final int promotionIndex = value >> PROMOTION_OFFSET;
		final int from = value & FROM_MASK;
		final int to = (value & TO_MASK) >> TO_OFFSET;
		return promotionIndex==0 ? new BasicMove(from, to) : new BasicMove(from, to, Piece.ALL.get(promotionIndex-1));
	}
}