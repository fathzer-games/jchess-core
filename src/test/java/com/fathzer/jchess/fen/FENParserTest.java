package com.fathzer.jchess.fen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.standard.Coord;

class FENParserTest {

	@Test
	void test() {
		final String fen = "r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1";
		final Board<Move> board = FENParser.from(fen);
		assertEquals(Piece.WHITE_KING, board.getPiece(Coord.toIndex("e1")));
		assertEquals(Piece.WHITE_QUEEN, board.getPiece(Coord.toIndex("e4")));
		assertEquals(Piece.BLACK_BISHOP, board.getPiece(Coord.toIndex("h6")));
		
		assertEquals(fen, FENParser.to(board));
	}

}
