package com.fathzer.jchess.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.generic.BasicMove;

class TranspositionTableTest {

	@Test
	void test() {
		TT tt = new TT(1, SizeUnit.KB);
		BasicMove mv = new BasicMove(128,208, Piece.WHITE_QUEEN);
		Move other = tt.toMove(tt.toInt(mv));
		assertEquals(128, other.getFrom());
		assertEquals(208, other.getTo());
		assertEquals(Piece.WHITE_QUEEN, other.getPromotion());
		
		mv = new BasicMove(1,0);
		other = tt.toMove(tt.toInt(mv));
		assertEquals(1, other.getFrom());
		assertEquals(0, other.getTo());
		assertNull(other.getPromotion());
	}

}
