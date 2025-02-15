package com.fathzer.jchess.fen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.Variant;

class FENParserTest {

	@Test
	void test() {
		final String fen = "r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1";
		final Board<Move> board = FENUtils.from(fen);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("e1")));
		assertEquals(Piece.WHITE_QUEEN, board.getPiece(cs.getIndex("e4")));
		assertEquals(Piece.BLACK_BISHOP, board.getPiece(cs.getIndex("h6")));
		assertEquals(cs.getIndex("h1"), board.getInitialRookPosition(Castling.WHITE_KING_SIDE));
		assertEquals(cs.getIndex("a1"), board.getInitialRookPosition(Castling.WHITE_QUEEN_SIDE));
		assertEquals(cs.getIndex("a8"), board.getInitialRookPosition(Castling.BLACK_QUEEN_SIDE));
		
		assertEquals(fen, FENUtils.to(board));
		
		// Missing rook test
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("rnbqkbnr/pppppppp/8/8/8/R7/PPPPPPPP/1NBQKBNR w Q - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("rnbqkbnr/pppppppp/8/8/8/7R/PPPPPPPP/RNBQKBN1 w K - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("rnbqkbn1/pppppppp/7r/8/8/8/PPPPPPPP/RNBQKBNR w k - 0 1"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("1nbqkbnr/pppppppp/r7/8/8/8/PPPPPPPP/RNBQKBNR w q - 0 1"));
	}

	@Test
	void bug20230518() {
		// Parser failed if not all rooks were in place
		assertEquals(Color.WHITE, FENUtils.from("4k3/8/8/8/8/8/3PP3/r3K2R w K - 0 1").getActiveColor());
	}
	
	@Test
	void bug20230601() {
		// Parser failed
		assertEquals(Color.BLACK, FENUtils.from("r4br1/4p3/k1p2npp/PpnQ4/P5b1/2PPP1qP/5P2/RNB1KB2 b Q - 0 1").getActiveColor());
	}
	
	@Test
	void test960() {
		final var fen = "nbbqrknr/pppppppp/8/8/8/8/PPPPPPPP/NBBQRKNR w KQkq - 0 1";
		var board = FENUtils.from(fen, Variant.CHESS960);
		assertEquals(fen, FENUtils.to(board));
		
		final String fenWithInnerRook = "rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Gkq - 4 11";
		board = FENUtils.from(fenWithInnerRook, Variant.CHESS960);
		assertTrue(board.hasCastling(Castling.WHITE_KING_SIDE));     
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));     
		assertTrue(board.hasCastling(Castling.BLACK_KING_SIDE));     
		assertTrue(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals(cs.getIndex("g1"), board.getInitialRookPosition(Castling.WHITE_KING_SIDE));
		assertEquals(cs.getIndex("g8"), board.getInitialRookPosition(Castling.BLACK_KING_SIDE));
		assertEquals(cs.getIndex("a8"), board.getInitialRookPosition(Castling.BLACK_QUEEN_SIDE));

		assertEquals(fenWithInnerRook, FENUtils.to(board));
		
		final String otherFenWithInnerRook = "1r2k1r1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Bkq - 4 11";
		board = FENUtils.from(otherFenWithInnerRook, Variant.CHESS960);
		assertEquals(otherFenWithInnerRook, FENUtils.to(board));
	}

	@Test
	void testIllegalVariant() {
		final var fen = "rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Gkq - 4 11";
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from(fen));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from(fen, Variant.STANDARD));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from(fen, null));
	}
}
