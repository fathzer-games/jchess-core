package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.standard.Coord;

class ChessBoardTest {

	@Test
	void castlingsTest() {
		final Board<Move> board = FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1P1/R3K2R w KQkq a6 2 10");
		final Board<Move> copy = board.create();

		// Test King move erases castling possibility
		copy.copy(board);
		copy.move(new SimpleMove("e8","d8"));
		assertFalse(copy.hasCastling(Castling.BLACK_QUEEN_SIDE));
		assertFalse(copy.hasCastling(Castling.BLACK_KING_SIDE));
		assertEquals(Piece.BLACK_KING,copy.getPiece(Coord.toIndex("d8")));
		assertNull(copy.getPiece(Coord.toIndex("e8")));

		// Test castling moves the pieces has it should
		copy.copy(board);
		copy.move(new SimpleMove("e1","g1"));
		assertEquals(Piece.WHITE_KING,copy.getPiece(Coord.toIndex("g1")));
		assertEquals(Coord.toIndex("g1"), copy.getKingPosition(Color.WHITE));
		assertEquals(Piece.WHITE_ROOK,copy.getPiece(Coord.toIndex("f1")));
		assertNull(copy.getPiece(Coord.toIndex("h1")));
		assertNull(copy.getPiece(Coord.toIndex("e1")));
		assertFalse(copy.hasCastling(Castling.WHITE_KING_SIDE));
		assertFalse(copy.hasCastling(Castling.WHITE_QUEEN_SIDE));

		copy.copy(board);
		copy.move(new SimpleMove("e1","c1"));
		assertEquals(Piece.WHITE_KING,copy.getPiece(Coord.toIndex("c1")));
		assertEquals(Coord.toIndex("c1"), copy.getKingPosition(Color.WHITE));
		assertEquals(Piece.WHITE_ROOK,copy.getPiece(Coord.toIndex("d1")));
		assertNull(copy.getPiece(Coord.toIndex("a1")));
		assertNull(copy.getPiece(Coord.toIndex("e1")));
		assertFalse(copy.hasCastling(Castling.WHITE_KING_SIDE));
		assertFalse(copy.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertTrue(copy.hasCastling(Castling.BLACK_KING_SIDE));
		assertTrue(copy.hasCastling(Castling.BLACK_QUEEN_SIDE));
		// Test castling counts for one move (so does not increment move counter as White are playing), increments halfmoves, clears 'en-passant' and changes next player
		assertEquals(board.getMoveNumber(),copy.getMoveNumber());
		assertEquals(board.getHalfMoveCount()+1,copy.getHalfMoveCount());
		assertTrue(copy.getEnPassant()<0);
		assertEquals(Color.BLACK, copy.getActiveColor());

		// Rook move erases castling possibility
		copy.copy(board);
		copy.move(new SimpleMove("a1","b1"));
		assertFalse(copy.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertTrue(copy.hasCastling(Castling.WHITE_KING_SIDE));
		copy.copy(board);
		copy.move(new SimpleMove("h1","h2"));
		assertFalse(copy.hasCastling(Castling.WHITE_KING_SIDE));
		assertTrue(copy.hasCastling(Castling.WHITE_QUEEN_SIDE));
		copy.move(new SimpleMove("a8","a7"));
		assertFalse(copy.hasCastling(Castling.BLACK_QUEEN_SIDE));
		assertTrue(copy.hasCastling(Castling.BLACK_KING_SIDE));
		assertTrue(copy.hasCastling(Castling.WHITE_QUEEN_SIDE));
		copy.move(new SimpleMove("h8","g8"));
		assertFalse(copy.hasCastling(Castling.BLACK_KING_SIDE));
	}
	
	@Test
	void invalidEnPassantArgs() {
		// Not the right side to move
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k2r/1pppqp1p/2n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R b KQkq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pP/B2B4/P2PPN2/R3K2R w KQq h3 2 10"));
		// The en-Passant cell is not empty
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/r1n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R w KQq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pP/B2B3R/P2PPN2/R3K3 b Qq h3 2 10"));
		// The piece at the row after (or before depending on the color) is not occupied by a pawn
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R w KQq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pR/B2B4/P2PPN2/R3K3 b Qq h3 2 10"));
		// Not the right row
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k2r/1pppqp1p/2n2n1b/8/pPN1Q3/B2B3N/P2PPPp1/R3K2R w KQkq a5 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENParser.from("r1b1k3/1pppqp1p/2n2n1b/rP4pP/2N1Q3/B2B4/P2PPN2/R3K3 b Qq h4 2 10"));
	}
	
	@Test
	void pawnMoveTest() {
		// Test Pawn move
		Board<Move> board = FENParser.from("r1b1k2r/1pppqp1p/2n2n1b/pP6/2N1Q3/B2B3N/P2PPPp1/R3K2R w KQkq a6 2 10");
		final Board<Move> copy = board.create();
		// Test valid enPassant is set
		assertEquals(16, board.getEnPassant());
		// Test en-passant catch clears the catched pawn and enPassant position
		copy.copy(board);
		copy.move(new SimpleMove("b5","a6"));
		assertEquals(Piece.WHITE_PAWN, copy.getPiece(Coord.toIndex("a6")));
		assertNull(copy.getPiece(Coord.toIndex("b5")));
		assertNull(copy.getPiece(Coord.toIndex("a5")));
		assertTrue(copy.getEnPassant()<0);
		assertEquals(0, copy.getHalfMoveCount());
		// Test promotion
		assertEquals(Color.BLACK, copy.getActiveColor());
		copy.move(new SimpleMove("g2","h1",Piece.BLACK_QUEEN));
		assertEquals(Piece.BLACK_QUEEN, copy.getPiece(Coord.toIndex("h1")));
		assertNull(copy.getPiece(Coord.toIndex("g2")));
		assertEquals(Color.WHITE, copy.getActiveColor());
		
		board = FENParser.from("4k1r1/1P6/5p2/p1Np1P2/5B1p/5Q1P/1q3PPK/8 w - - 4 42");
		board.move(new SimpleMove("b7","b8", Piece.WHITE_QUEEN));
		assertEquals(Color.BLACK, board.getActiveColor());
		
		// Test enPassant is set when pawn moves two rows
		board = FENParser.from("4k1r1/2p5/5p2/NP1p1P2/5B1p/5Q1P/1q3PPK/8 b - - 4 42");
		board.move(new SimpleMove("c7","c5"));
		assertEquals(Coord.toIndex("c6"), board.getEnPassant());
		assertEquals(0, board.getHalfMoveCount());
		assertNull(board.getPiece(Coord.toIndex("c7")));
		assertEquals(Piece.BLACK_PAWN, board.getPiece(Coord.toIndex("c5")));
	}
	
	@Test
	void testRookCaptureAndCastlings() {
		ChessBoard board = (ChessBoard) FENParser.from("r3k3/1K6/8/8/8/8/8/8 w q - 0 1");
		board.move(new SimpleMove("b7", "a8"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENParser.from("4k2r/6K1/8/8/8/8/8/8 w k - 0 1");
		board.move(new SimpleMove("g7", "h8"));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.move(new SimpleMove("h8", "h1"));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.move(new SimpleMove("g7", "a1"));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testRookMoveAndCastlings() {
		ChessBoard board = (ChessBoard) FENParser.from("r3k3/1K6/8/8/8/8/8/8 b q - 0 1");
		board.move(new SimpleMove("a8","d8"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENParser.from("4k2r/6K1/8/8/8/8/8/8 b k - 0 1");
		board.move(new SimpleMove("h8","f8"));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.move(new SimpleMove("h1", "g2"));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.move(new SimpleMove("a1","a8"));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testZobristKey() {
		Board<Move> board = FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w KQkq - 0 1");
		long initial = board.getKey();
		
		// Test turn
		assertNotEquals(initial, FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getKey());
		
		// Test useless moves (moves that leads to the same position)
		board.move(new SimpleMove("a3","c4"));
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/2N5/3PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getKey(), board.getKey());
		board.move(new SimpleMove("g8","f6"));
		board.move(new SimpleMove("c4","a3"));
		board.move(new SimpleMove("f6","g8"));
		assertEquals(initial, board.getKey());
		
		// Test castling erase
		// 1 - Rook move
		board.move(new SimpleMove("h1","g1"));
		board.move(new SimpleMove("g8","f6"));
		board.move(new SimpleMove("g1","h1"));
		board.move(new SimpleMove("f6","g8"));
		long withoutRCastling = board.getKey();
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w Qkq - 0 1").getKey(), withoutRCastling);
		assertNotEquals(initial, withoutRCastling);
		// 2 - King move
		initial = board.getKey();
		board.move(new SimpleMove("e1","d1"));
		board.move(new SimpleMove("g8","f6"));
		board.move(new SimpleMove("d1","e1"));
		board.move(new SimpleMove("f6","g8"));
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w kq - 0 1").getKey(), board.getKey());
		assertNotEquals(withoutRCastling, board.getKey());
		
		// Test capture
		board.move(new SimpleMove("b5", "d7"));
		assertEquals(FENParser.from("rnbqkbnr/pppBpppp/8/8/8/N2PPN2/PPPQBPPP/R3K2R b kq - 0 1").getKey(), board.getKey());

		// Test enPassant
		// 1 - real en passant
		long enPassantKey = FENParser.from("rnbqkbnr/pppppp1p/8/1B6/6pP/N2PPN2/PPPQBPP1/R3K2R b KQkq h3 0 1").getKey();
		long notEnPassantKey = FENParser.from("rnbqkbnr/pppppp1p/8/1B6/6pP/N2PPN2/PPPQBPP1/R3K2R b KQkq - 0 1").getKey();
		assertNotEquals(enPassantKey, notEnPassantKey);
		// 2 - useless en passant
		enPassantKey = FENParser.from("rnbqkbnr/pppppppp/8/1B6/7P/N2PPN2/PPPQBPP1/R3K2R b KQkq h3 0 1").getKey();
		notEnPassantKey = FENParser.from("rnbqkbnr/pppppppp/8/1B6/7P/N2PPN2/PPPQBPP1/R3K2R b KQkq - 0 1").getKey();
		assertEquals(enPassantKey, notEnPassantKey);
		// 3 - capture en passant
		board = FENParser.from("rnbqkbnr/pppppp1p/8/8/6pP/8/PPPPPPP1/RNBQKBNR b KQkq h3 0 1");
		board.move(new SimpleMove("g4", "h3"));
		assertNull(board.getPiece(Coord.toIndex("h4")));
		assertEquals(FENParser.from("rnbqkbnr/pppppp1p/8/8/8/7p/PPPPPPP1/RNBQKBNR w KQkq - 0 1").getKey(), board.getKey());
		
		// Test promotion
		board = FENParser.from("rnbqkb2/pppppppP/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R w KQq - 0 1");
		board.move(new SimpleMove("h7", "h8", Piece.WHITE_QUEEN));
		assertEquals(FENParser.from("rnbqkb1Q/ppppppp1/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R b KQq - 0 1").getKey(), board.getKey());
		
		// Test castling
		// 1 - Q
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.move(new SimpleMove("e1","c1"));
		assertEquals(FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/2KR3R b kq - 0 1").getKey(), board.getKey());
		// 1 - K
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.move(new SimpleMove("e1","g1"));
		assertEquals(FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R4RK1 b kq - 0 1").getKey(), board.getKey());
		// 1 - q
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.move(new SimpleMove("e8","c8"));
		assertEquals(FENParser.from("2kr3r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getKey(), board.getKey());
		// 1 - K
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.move(new SimpleMove("e8","g8"));
		assertEquals(FENParser.from("r4rk1/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getKey(), board.getKey());
	}
}
