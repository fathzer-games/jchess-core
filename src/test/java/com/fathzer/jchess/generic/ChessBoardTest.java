package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENParser;

class ChessBoardTest {

	@Test
	void castlingsTest() {
		final Board<Move> board = FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1P1/R3K2R w KQkq a6 2 10");
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		// Test King move erases castling possibility
		board.makeMove(new SimpleMove(cs, "e8","d8"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		assertEquals(Piece.BLACK_KING,board.getPiece(cs.getIndex("d8")));
		assertNull(board.getPiece(cs.getIndex("e8")));
		board.unmakeMove();

		// Test castling moves the pieces has it should
		board.makeMove(new SimpleMove(cs, "e1","g1"));
		assertEquals(Piece.WHITE_KING,board.getPiece(cs.getIndex("g1")));
		assertEquals(cs.getIndex("g1"), board.getKingPosition(Color.WHITE));
		assertEquals(Piece.WHITE_ROOK,board.getPiece(cs.getIndex("f1")));
		assertNull(board.getPiece(cs.getIndex("h1")));
		assertNull(board.getPiece(cs.getIndex("e1")));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		board.unmakeMove();

		int moveNumber = board.getMoveNumber();
		int halfMoveCount = board.getHalfMoveCount();
		board.makeMove(new SimpleMove(cs, "e1","c1"));
		assertEquals(Piece.WHITE_KING,board.getPiece(cs.getIndex("c1")));
		assertEquals(cs.getIndex("c1"), board.getKingPosition(Color.WHITE));
		assertEquals(Piece.WHITE_ROOK,board.getPiece(cs.getIndex("d1")));
		assertNull(board.getPiece(cs.getIndex("a1")));
		assertNull(board.getPiece(cs.getIndex("e1")));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertTrue(board.hasCastling(Castling.BLACK_KING_SIDE));
		assertTrue(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		// Test castling counts for one move (so does not increment move counter as White are playing), increments halfmoves, clears 'en-passant' and changes next player
		assertEquals(moveNumber, board.getMoveNumber());
		assertEquals(halfMoveCount+1,board.getHalfMoveCount());
		assertTrue(board.getEnPassant()<0);
		assertEquals(Color.BLACK, board.getActiveColor());
		board.unmakeMove();

		// Rook move erases castling possibility
		board.makeMove(new SimpleMove(cs, "a1","b1"));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_KING_SIDE));
		board.unmakeMove();
		board.makeMove(new SimpleMove(cs, "h1","h2"));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		board.makeMove(new SimpleMove(cs, "a8","a7"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		assertTrue(board.hasCastling(Castling.BLACK_KING_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		board.makeMove(new SimpleMove(cs, "h8","g8"));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
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
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		// Test valid enPassant is set
		assertEquals(board.getCoordinatesSystem().getIndex("a6"), board.getEnPassant());
		// Test en-passant catch clears the caught pawn and enPassant position
		board.makeMove(new SimpleMove(cs, "b5","a6"));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(cs.getIndex("a6")));
		assertNull(board.getPiece(cs.getIndex("b5")));
		assertNull(board.getPiece(cs.getIndex("a5")));
		assertTrue(board.getEnPassant()<0);
		assertEquals(0, board.getHalfMoveCount());
		
		// Test promotion
		assertEquals(Color.BLACK, board.getActiveColor());
		board.makeMove(new SimpleMove(cs, "g2","h1",Piece.BLACK_QUEEN));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(cs.getIndex("h1")));
		assertNull(board.getPiece(cs.getIndex("g2")));
		assertEquals(Color.WHITE, board.getActiveColor());
		
		board = FENParser.from("4k1r1/1P6/5p2/p1Np1P2/5B1p/5Q1P/1q3PPK/8 w - - 4 42");
		board.makeMove(new SimpleMove(cs, "b7","b8", Piece.WHITE_QUEEN));
		assertEquals(Color.BLACK, board.getActiveColor());
		
		// Test enPassant is set when pawn moves two rows
		board = FENParser.from("4k1r1/2p5/5p2/NP1p1P2/5B1p/5Q1P/1q3PPK/8 b - - 4 42");
		board.makeMove(new SimpleMove(cs, "c7","c5"));
		assertEquals(cs.getIndex("c6"), board.getEnPassant());
		assertEquals(0, board.getHalfMoveCount());
		assertNull(board.getPiece(cs.getIndex("c7")));
		assertEquals(Piece.BLACK_PAWN, board.getPiece(cs.getIndex("c5")));
	}
	
	@Test
	void testRookCaptureAndCastlings() {
		ChessBoard board = (ChessBoard) FENParser.from("r3k3/1K6/8/8/8/8/8/8 w q - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new SimpleMove(cs, "b7", "a8"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENParser.from("4k2r/6K1/8/8/8/8/8/8 w k - 0 1");
		board.makeMove(new SimpleMove(cs, "g7", "h8"));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.makeMove(new SimpleMove(cs, "h8", "h1"));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.makeMove(new SimpleMove(cs, "g7", "a1"));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testRookMoveAndCastlings() {
		ChessBoard board = (ChessBoard) FENParser.from("r3k3/1K6/8/8/8/8/8/8 b q - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new SimpleMove(cs, "a8","d8"));
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENParser.from("4k2r/6K1/8/8/8/8/8/8 b k - 0 1");
		board.makeMove(new SimpleMove(cs, "h8","f8"));
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.makeMove(new SimpleMove(cs, "h1", "g2"));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENParser.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.makeMove(new SimpleMove(cs, "a1","a8"));
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testZobristKey() {
		Board<Move> board = FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		long initial = board.getKey();
		
		// Test turn
		assertNotEquals(initial, FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getKey());
		
		// Test useless moves (moves that leads to the same position)
		board.makeMove(new SimpleMove(cs, "a3","c4"));
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/2N5/3PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getKey(), board.getKey());
		board.makeMove(new SimpleMove(cs, "g8","f6"));
		board.makeMove(new SimpleMove(cs, "c4","a3"));
		board.makeMove(new SimpleMove(cs, "f6","g8"));
		assertEquals(initial, board.getKey());
		
		// Test castling erase
		// 1 - Rook move
		board.makeMove(new SimpleMove(cs, "h1","g1"));
		board.makeMove(new SimpleMove(cs, "g8","f6"));
		board.makeMove(new SimpleMove(cs, "g1","h1"));
		board.makeMove(new SimpleMove(cs, "f6","g8"));
		long withoutRCastling = board.getKey();
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w Qkq - 0 1").getKey(), withoutRCastling);
		assertNotEquals(initial, withoutRCastling);
		// 2 - King move
		initial = board.getKey();
		board.makeMove(new SimpleMove(cs, "e1","d1"));
		board.makeMove(new SimpleMove(cs, "g8","f6"));
		board.makeMove(new SimpleMove(cs, "d1","e1"));
		board.makeMove(new SimpleMove(cs, "f6","g8"));
		assertEquals(FENParser.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w kq - 0 1").getKey(), board.getKey());
		assertNotEquals(withoutRCastling, board.getKey());
		
		// Test capture
		board.makeMove(new SimpleMove(cs, "b5", "d7"));
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
		board.makeMove(new SimpleMove(cs, "g4", "h3"));
		assertNull(board.getPiece(board.getCoordinatesSystem().getIndex("h4")));
		assertEquals(FENParser.from("rnbqkbnr/pppppp1p/8/8/8/7p/PPPPPPP1/RNBQKBNR w KQkq - 0 1").getKey(), board.getKey());
		
		// Test promotion
		board = FENParser.from("rnbqkb2/pppppppP/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R w KQq - 0 1");
		board.makeMove(new SimpleMove(cs, "h7", "h8", Piece.WHITE_QUEEN));
		assertEquals(FENParser.from("rnbqkb1Q/ppppppp1/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R b KQq - 0 1").getKey(), board.getKey());
		
		// Test castling
		// 1 - Q
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e1","c1"));
		assertEquals(FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/2KR3R b kq - 0 1").getKey(), board.getKey());
		// 1 - K
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e1","g1"));
		assertEquals(FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R4RK1 b kq - 0 1").getKey(), board.getKey());
		// 1 - q
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e8","c8"));
		assertEquals(FENParser.from("2kr3r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getKey(), board.getKey());
		// 1 - K
		board = FENParser.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e8","g8"));
		assertEquals(FENParser.from("r4rk1/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getKey(), board.getKey());
	}
}
