package com.fathzer.jchess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.standard.Coord;

class CastlingTest {

	@Test
	void test() {
		int dest = Castling.WHITE_QUEEN_SIDE.getKingDestination(Dimension.STANDARD);
		assertEquals(Coord.toIndex("c1"), dest);

		dest = Castling.BLACK_QUEEN_SIDE.getKingDestination(Dimension.STANDARD);
		assertEquals(Coord.toIndex("c8"), dest);

		dest = Castling.WHITE_KING_SIDE.getKingDestination(Dimension.STANDARD);
		assertEquals(Coord.toIndex("g1"), dest);

		dest = Castling.BLACK_KING_SIDE.getKingDestination(Dimension.STANDARD);
		assertEquals(Coord.toIndex("g8"), dest);
		
		// Test with Capablanca's chess
		final Dimension capa = new Dimension(10, 8);
		dest = Castling.BLACK_QUEEN_SIDE.getKingDestination(capa);
		assertEquals(2, dest);

		dest = Castling.BLACK_KING_SIDE.getKingDestination(capa);
		assertEquals(8, dest);

		dest = Castling.WHITE_QUEEN_SIDE.getKingDestination(capa);
		assertEquals(72, dest);

		dest = Castling.WHITE_KING_SIDE.getKingDestination(capa);
		assertEquals(78, dest);
	}

}
