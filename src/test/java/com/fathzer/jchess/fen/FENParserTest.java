package com.fathzer.jchess.fen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;

class FENParserTest {

	@Test
	void test() {
		final String fen = "r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1";
		final Board<Move> board = FENParser.from(fen);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("e1")));
		assertEquals(Piece.WHITE_QUEEN, board.getPiece(cs.getIndex("e4")));
		assertEquals(Piece.BLACK_BISHOP, board.getPiece(cs.getIndex("h6")));
		
		assertEquals(fen, FENParser.to(board));
		
		// Missing rook test
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("rnbqkbnr/pppppppp/8/8/8/R7/PPPPPPPP/1NBQKBNR w Q - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("rnbqkbnr/pppppppp/8/8/8/7R/PPPPPPPP/RNBQKBN1 w K - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("rnbqkbn1/pppppppp/7r/8/8/8/PPPPPPPP/RNBQKBNR w k - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("1nbqkbnr/pppppppp/r7/8/8/8/PPPPPPPP/RNBQKBNR w q - 0 1"));
	}

	@Test
	void bug20230518() {
		// Parser failed if not all rooks were in place
		assertEquals(Color.WHITE, FENParser.from("4k3/8/8/8/8/8/3PP3/r3K2R w K - 0 1").getActiveColor());
	}
	
	@Test
	void bug20230601() {
		// Parser failed
		assertEquals(Color.BLACK, FENParser.from("r4br1/4p3/k1p2npp/PpnQ4/P5b1/2PPP1qP/5P2/RNB1KB2 b Q - 0 1").getActiveColor());
	}
	
	@Test
	void test960() {
		final String fen = "nbbqrknr/pppppppp/8/8/8/8/PPPPPPPP/NBBQRKNR w KQkq - 0 1";
		Board<Move> board = FENParser.from(fen);
		assertEquals(fen, FENParser.to(board));
	}
}
