package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.GameBuilders;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;

class ChessBoardTest {

	@Test
	void castlingsMoveTest() {
		final Board<Move> board = FENUtils.from("r1b1k2r/1pppqppp/2n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1P1/R3K2R w KQkq a6 2 10");
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		// Test King move erases castling possibility
		board.makeMove(new SimpleMove(cs, "e1","d1"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		assertEquals(Piece.WHITE_KING,board.getPiece(cs.getIndex("d1")));
		assertNull(board.getPiece(cs.getIndex("e1")));
		board.unmakeMove();

		// Test castling moves the pieces has it should
		board.makeMove(new SimpleMove(cs, "e1","g1"), MoveConfidence.LEGAL);
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
		board.makeMove(new SimpleMove(cs, "e1","c1"), MoveConfidence.LEGAL);
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
		board.makeMove(new SimpleMove(cs, "a1","b1"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_KING_SIDE));
		board.unmakeMove();
		board.makeMove(new SimpleMove(cs, "h1","h2"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		board.makeMove(new SimpleMove(cs, "a8","a7"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		assertTrue(board.hasCastling(Castling.BLACK_KING_SIDE));
		assertTrue(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
		board.makeMove(new SimpleMove(cs, "h8","g8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
	}
	
	@Test
	void invalidEnPassantArgs() {
		// Not the right side to move
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k2r/1pppqp1p/2n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R b KQkq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pP/B2B4/P2PPN2/R3K2R w KQq h3 2 10"));
		// The en-Passant cell is not empty
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/r1n2n1b/pP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R w KQq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pP/B2B3R/P2PPN2/R3K3 b Qq h3 2 10"));
		// The piece at the row after (or before depending on the color) is not occupied by a pawn
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q3/B2B1P1N/P2PP1p1/R3K2R w KQq a6 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/2n2n1b/rP6/2N1Q1pR/B2B4/P2PPN2/R3K3 b Qq h3 2 10"));
		// Not the right row
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k2r/1pppqp1p/2n2n1b/8/pPN1Q3/B2B3N/P2PPPp1/R3K2R w KQkq a5 2 10"));
		assertThrows(IllegalArgumentException.class, () -> FENUtils.from("r1b1k3/1pppqp1p/2n2n1b/rP4pP/2N1Q3/B2B4/P2PPN2/R3K3 b Qq h4 2 10"));
	}
	
	@Test
	void pawnMakeMoveTest() {
		// Test Pawn move
		Board<Move> board = FENUtils.from("r1b1k2r/1pppqp1p/2n2n1b/pP6/2N1Q3/B2B3N/P2PPPp1/R3K2R w KQkq a6 2 10");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		// Test valid enPassant is set
		assertEquals(board.getCoordinatesSystem().getIndex("a6"), board.getEnPassant());
		// Test en-passant catch clears the caught pawn and enPassant position
		board.makeMove(new SimpleMove(cs, "b5","a6"), MoveConfidence.LEGAL);
		assertEquals(Piece.WHITE_PAWN, board.getPiece(cs.getIndex("a6")));
		assertNull(board.getPiece(cs.getIndex("b5")));
		assertNull(board.getPiece(cs.getIndex("a5")));
		assertTrue(board.getEnPassant()<0);
		assertEquals(0, board.getHalfMoveCount());
		
		// Test promotion
		assertEquals(Color.BLACK, board.getActiveColor());
		board.makeMove(new SimpleMove(cs, "g2","h1",Piece.BLACK_QUEEN), MoveConfidence.LEGAL);
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(cs.getIndex("h1")));
		assertNull(board.getPiece(cs.getIndex("g2")));
		assertEquals(Color.WHITE, board.getActiveColor());
		
		board = FENUtils.from("4k1r1/1P6/5p2/p1Np1P2/5B1p/5Q1P/1q3PPK/8 w - - 4 42");
		board.makeMove(new SimpleMove(cs, "b7","b8", Piece.WHITE_QUEEN), MoveConfidence.LEGAL);
		assertEquals(Color.BLACK, board.getActiveColor());
		
		// Test enPassant is set when pawn moves two rows
		board = FENUtils.from("4k1r1/2p5/5p2/NP1p1P2/5B1p/5Q1P/1q3PPK/8 b - - 4 42");
		board.makeMove(new SimpleMove(cs, "c7","c5"), MoveConfidence.LEGAL);
		assertEquals(cs.getIndex("c6"), board.getEnPassant());
		assertEquals(0, board.getHalfMoveCount());
		assertNull(board.getPiece(cs.getIndex("c7")));
		assertEquals(Piece.BLACK_PAWN, board.getPiece(cs.getIndex("c5")));
	}
	
	@Test
	void testRookCaptureAndCastlings() {
		ChessBoard board = (ChessBoard) FENUtils.from("r3k3/1K6/8/8/8/8/8/8 w q - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new SimpleMove(cs, "b7", "a8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENUtils.from("4k2r/6K1/8/8/8/8/8/8 w k - 0 1");
		board.makeMove(new SimpleMove(cs, "g7", "h8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENUtils.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.makeMove(new SimpleMove(cs, "h8", "h1"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENUtils.from("4k2r/6b1/8/8/8/8/8/R3K2R b k - 0 1");
		board.makeMove(new SimpleMove(cs, "g7", "a1"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testRookMoveAndCastlings() {
		ChessBoard board = (ChessBoard) FENUtils.from("r3k3/1K6/8/8/8/8/8/8 b q - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new SimpleMove(cs, "a8","d8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_QUEEN_SIDE));

		board = (ChessBoard) FENUtils.from("4k2r/6K1/8/8/8/8/8/8 b k - 0 1");
		board.makeMove(new SimpleMove(cs, "h8","f8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.BLACK_KING_SIDE));
		
		board = (ChessBoard) FENUtils.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.makeMove(new SimpleMove(cs, "h1", "g2"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_KING_SIDE));
		
		board = (ChessBoard) FENUtils.from("4k2r/6b1/8/8/8/8/8/R3K2R w KQk - 0 1");
		board.makeMove(new SimpleMove(cs, "a1","a8"), MoveConfidence.LEGAL);
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));
	}
	
	@Test
	void testEnPassantZobristKey() {
		// Test enPassant
		// 1 - real en passant
		long enPassantKey = FENUtils.from("rnbqkbnr/pppppp1p/8/1B6/6pP/N2PPN2/PPPQBPP1/R3K2R b KQkq h3 0 1").getHashKey();
		final String notEnPassantFen = "rnbqkbnr/pppppp1p/8/1B6/6pP/N2PPN2/PPPQBPP1/R3K2R b KQkq - 0 2";
		long notEnPassantKey = FENUtils.from(notEnPassantFen).getHashKey();
		assertNotEquals(enPassantKey, notEnPassantKey);
		Board<Move> board = FENUtils.from("rnbqkbnr/pppppp1p/8/1B6/6p1/N2PPN2/PPPQBPPP/R3K2R w KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new SimpleMove(cs, "h2", "h4"), MoveConfidence.LEGAL);
		assertEquals(enPassantKey, board.getHashKey());
		board = FENUtils.from("rnbqkbnr/pppppp1p/8/1B4p1/8/N2PPN2/PPPQBPPP/R3K2R w KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "h2", "h3"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "g5", "g4"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "h3", "h4"), MoveConfidence.LEGAL);
		assertEquals(notEnPassantFen, FENUtils.to(board));
		assertEquals(notEnPassantKey, board.getHashKey());
		
		// 2 - useless en passant
		enPassantKey = FENUtils.from("rnbqkbnr/pppppppp/8/1B6/7P/N2PPN2/PPPQBPP1/R3K2R b KQkq h3 0 1").getHashKey();
		notEnPassantKey = FENUtils.from("rnbqkbnr/pppppppp/8/1B6/7P/N2PPN2/PPPQBPP1/R3K2R b KQkq - 0 1").getHashKey();
		assertEquals(enPassantKey, notEnPassantKey);
		// 3 - capture en passant
		board = FENUtils.from("rnbqkbnr/pppppp1p/8/8/6pP/8/PPPPPPP1/RNBQKBNR b KQkq h3 0 1");
		board.makeMove(new SimpleMove(cs, "g4", "h3"), MoveConfidence.LEGAL);
		assertNull(board.getPiece(board.getCoordinatesSystem().getIndex("h4")));
		assertEquals(FENUtils.from("rnbqkbnr/pppppp1p/8/8/8/7p/PPPPPPP1/RNBQKBNR w KQkq - 0 1").getHashKey(), board.getHashKey());
	}
	
	@Test
	void testZobristKey() {
		Board<Move> board = FENUtils.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		long initial = board.getHashKey();
		
		// Test turn
		assertNotEquals(initial, FENUtils.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getHashKey());
		
		// Test useless moves (moves that leads to the same position)
		board.makeMove(new SimpleMove(cs, "a3","c4"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("rnbqkbnr/pppppppp/8/1B6/2N5/3PPN2/PPPQBPPP/R3K2R b KQkq - 0 1").getHashKey(), board.getHashKey());
		board.makeMove(new SimpleMove(cs, "g8","f6"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "c4","a3"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "f6","g8"), MoveConfidence.LEGAL);
		assertEquals(initial, board.getHashKey());
		
		// Test castling erase
		// 1 - Rook move
		board.makeMove(new SimpleMove(cs, "h1","g1"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "g8","f6"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "g1","h1"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "f6","g8"), MoveConfidence.LEGAL);
		long withoutRCastling = board.getHashKey();
		assertEquals(FENUtils.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w Qkq - 0 1").getHashKey(), withoutRCastling);
		assertNotEquals(initial, withoutRCastling);
		// 2 - King move
		initial = board.getHashKey();
		board.makeMove(new SimpleMove(cs, "e1","d1"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "g8","f6"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "d1","e1"), MoveConfidence.LEGAL);
		board.makeMove(new SimpleMove(cs, "f6","g8"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("rnbqkbnr/pppppppp/8/1B6/8/N2PPN2/PPPQBPPP/R3K2R w kq - 0 1").getHashKey(), board.getHashKey());
		assertNotEquals(withoutRCastling, board.getHashKey());
		
		// Test capture
		board.makeMove(new SimpleMove(cs, "b5", "d7"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("rnbqkbnr/pppBpppp/8/8/8/N2PPN2/PPPQBPPP/R3K2R b kq - 0 1").getHashKey(), board.getHashKey());

		// Test promotion
		board = FENUtils.from("rnbqkb2/pppppppP/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R w KQq - 0 1");
		board.makeMove(new SimpleMove(cs, "h7", "h8", Piece.WHITE_QUEEN), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("rnbqkb1Q/ppppppp1/5r1n/1B6/8/N2PPN2/PPPQBPP1/R3K2R b KQq - 0 1").getHashKey(), board.getHashKey());
		
		// Test castling
		// 1 - Q
		board = FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e1","c1"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/2KR3R b kq - 0 1").getHashKey(), board.getHashKey());
		// 1 - K
		board = FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e1","g1"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R4RK1 b kq - 0 1").getHashKey(), board.getHashKey());
		// 1 - q
		board = FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e8","c8"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("2kr3r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getHashKey(), board.getHashKey());
		// 1 - K
		board = FENUtils.from("r3k2r/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R b KQkq - 0 1");
		board.makeMove(new SimpleMove(cs, "e8","g8"), MoveConfidence.LEGAL);
		assertEquals(FENUtils.from("r4rk1/ppp1qp1p/2n1pnp1/3p1b2/1b1P1B2/2NBPN2/PPP1QPPP/R3K2R w KQ - 0 1").getHashKey(), board.getHashKey());
	}
	
	@Test
	void pawnsMoveGenerationTest() {
		final Board<Move> board = FENUtils.from("r1b1k2r/1p1pqppp/2n2n1b/pP6/4QN2/B2B1P1N/P1pPP1P1/R3K2R b KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final List<Move> blackMoves = board.getMoves(false);
		assertEquals(Set.of("a4"), getTo(cs, getMoves(cs, blackMoves, "a5")));
		assertEquals(Set.of("g5","g6"), getTo(cs, getMoves(cs, blackMoves, "g7")));
		assertEquals(Set.of("b6"), getTo(cs, getMoves(cs, blackMoves, "b7")));
		final List<Move> promotions = getMoves(cs, blackMoves, "c2");
		assertEquals(Set.of("c1"),getTo(cs, promotions));
		assertEquals(Set.of(Piece.BLACK_ROOK,Piece.BLACK_BISHOP,Piece.BLACK_KNIGHT,Piece.BLACK_QUEEN), promotions.stream().map(Move::getPromotion).collect(Collectors.toSet()));

		final String fen = "r1b1k2r/1p1pqppp/2n2n1b/pP6/4QN2/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1";
		board.copy(FENUtils.from(fen));
		assertEquals(fen, FENUtils.to(board));
		final List<Move> whiteMoves = board.getMoves(false);
		assertEquals(Set.of("a6","b6","c6"), getTo(cs, getMoves(cs, whiteMoves, "b5")), whiteMoves.stream().map(m -> m.toString(cs)).collect(Collectors.joining(",")));

		
		board.copy(FENUtils.from("rK1k4/1P6/8/8/8/8/8/8 w - - 0 1"));
		final List<Move> checkMoves = getMoves(board, "b7");
		assertEquals(Set.of("a8"), getTo(cs, checkMoves));
		assertEquals(Set.of(Piece.WHITE_ROOK,Piece.WHITE_BISHOP,Piece.WHITE_KNIGHT,Piece.WHITE_QUEEN), checkMoves.stream().map(Move::getPromotion).collect(Collectors.toSet()));

		// En-passant capture is not possible (king would be in check)
		board.copy(FENUtils.from("4K3/8/8/8/R4pPk/8/8/8 b - g3 0 1"));
		assertEquals(Set.of("f3"), getTo(cs, getMoves(board, "f4")));
	}
	
	private List<Move> getMoves(Board<Move> board, String from) {
		return getMoves(board.getCoordinatesSystem(), board.getMoves(false),from);
	}

	private List<Move> getMoves(CoordinatesSystem cs, List<Move> moves, String from) {
		final int fromIndex = cs.getIndex(from);
		return moves.stream().filter(m-> m.getFrom()==fromIndex).collect(Collectors.toList());
	}

	private Set<String> getTo(CoordinatesSystem cs, List<Move> moves) {
		return moves.stream().map(m -> cs.getAlgebraicNotation(m.getTo())).collect(Collectors.toSet());
	}
	
	private Set<String> getFrom(CoordinatesSystem cs, List<Move> moves) {
		return moves.stream().map(m -> cs.getAlgebraicNotation(m.getFrom())).collect(Collectors.toSet());
	}

	@Test
	void kingsMoveGenerationTest() {
		final Board<Move> board = FENUtils.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals(Set.of("f1","f2","g1"), getTo(cs, getMoves(board, "e1")), "Problem in king move");
		
		// Making a standard move that does not make position safe should not work
		assertEquals(Set.of("f2"), getTo(cs, getMoves(FENUtils.from("4k3/8/8/8/8/8/3PP3/r3K2R w K - 0 1"), "e1")), "Problem in king move");
		
		// Should not castle when in check
		assertEquals(Set.of("d1","f1"), getTo(cs ,getMoves(FENUtils.from("4k3/8/8/8/8/3n4/3PP3/4K2R w K - 0 1"), "e1")), "Problem in king move");
		
		// Can castle
		List<Move> moves = FENUtils.from("r3kb1r/ppp2ppp/2nqb2n/P2p4/2P1p3/6R1/1PQPPPPP/1NB1KBNR b Kkq - 1 8").getMoves(false);
		moves = moves.stream().filter(m->"e8".equals(cs.getAlgebraicNotation(m.getFrom()))).collect(Collectors.toList());
		assertEquals(Set.of("c8","d8", "d7", "e7"), getTo(cs, moves));
		assertEquals(4, moves.size(), toString(moves, cs));
	}
	
	private static String toString(Collection<Move> moves, CoordinatesSystem cs) {
		return moves.stream().map(m -> m.toString(cs)).collect(Collectors.joining(", ", "[", "]"));
	}
	
	@Test
	void enPassantPinned() {
		final Board<Move> board = FENUtils.from("2Q1B3/8/8/KP4p1/1R3pPk/5R2/4P3/8 b - g3 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		List<Move> moves = board.getMoves(false);
		assertEquals(0, moves.size(), "Should have no possible moves but obtain "+toString(moves, cs));
	}
	
	@Test
	void checkTest() {
		// Piece not involved in check can't move
		// Queen can't move, catching white knight does not fix check
		// Only king can move
		final Board<Move> board = FENUtils.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R b KQkq - 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		List<Move> moves = board.getMoves(false);
		assertEquals(Set.of("e8"), getFrom(cs, moves), "Only king can move");
		assertEquals(Set.of("d8", "f8"), getTo(cs, moves));
	}
	
	@Test
	void statusTest() {
		assertEquals(Status.BLACK_WON, FENUtils.from("rnb1k1nr/pppp1ppp/3bp3/8/3P4/6B1/PPP1PPPP/RNq1KBNR w KQkq - 6 5").getStatus());
		assertEquals(Status.WHITE_WON, FENUtils.from("R3k3/7R/8/8/8/8/8/K7 b - - 6 5").getStatus());
		assertEquals(Status.DRAW, FENUtils.from("6k1/8/7Q/8/8/8/8/K4R2 b - - 6 5").getStatus());
		assertEquals(Status.PLAYING, FENUtils.from("6k1/8/7Q/8/8/8/8/K4R2 w - - 6 5").getStatus());
		// Only one knight at each side
		assertEquals(Status.DRAW, FENUtils.from("1n2k3/8/8/8/8/8/8/1N2K3 w - - 0 1").getStatus());
		// Only kings
		assertEquals(Status.DRAW, FENUtils.from("8/8/8/8/8/6k1/8/4K3 w - - 0 1").getStatus());
		// If one pawn remains, no draw
		assertEquals(Status.PLAYING, FENUtils.from("8/8/8/8/8/6k1/7p/1N2K3 w - - 0 1").getStatus());
	}
	
	@Test
	void drawByRepetitionTest() {
		Board<Move> board = GameBuilders.STANDARD.newGame();
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final Move kwf = new BasicMove(cs.getIndex("b1"),cs.getIndex("c3"));
		final Move kwb = new BasicMove(cs.getIndex("c3"),cs.getIndex("b1"));
		final Move kbf = new BasicMove(cs.getIndex("b8"),cs.getIndex("c6"));
		final Move kbb = new BasicMove(cs.getIndex("c6"),cs.getIndex("b8"));
		board.makeMove(kwf, MoveConfidence.LEGAL);
		board.makeMove(kbf, MoveConfidence.LEGAL);
		board.makeMove(kwb, MoveConfidence.LEGAL);
		board.makeMove(kbb, MoveConfidence.LEGAL); // first repetition
		assertEquals(Status.PLAYING, board.getStatus());
		board.makeMove(kwf, MoveConfidence.LEGAL);
		board.makeMove(kbf, MoveConfidence.LEGAL);
		board.makeMove(kwb, MoveConfidence.LEGAL);
		assertEquals(Status.PLAYING, board.getStatus());
		board.makeMove(kbb, MoveConfidence.LEGAL); // second repetition (so, third time with start position)
		assertEquals(Status.DRAW, board.getStatus());
		
		board.unmakeMove(); // Replace last move another reversible move
		assertEquals(Status.PLAYING, board.getStatus());
		final Move k2bf = new BasicMove(cs.getIndex("g8"),cs.getIndex("f6"));
		final Move k2bb = new BasicMove(cs.getIndex("f6"),cs.getIndex("g8"));
		board.makeMove(k2bf, MoveConfidence.LEGAL);
		board.makeMove(kwf, MoveConfidence.LEGAL);
		assertEquals(Status.PLAYING, board.getStatus());
		board.makeMove(k2bb, MoveConfidence.LEGAL); // third repetition
		assertEquals(Status.DRAW, board.getStatus());
		
		// Test it also working with copy
		Board<Move> copy = board.create();
		copy.copy(board);
		assertEquals(Status.DRAW, copy.getStatus());
		board.unmakeMove();
		assertEquals(Status.PLAYING, board.getStatus());
		assertEquals(Status.DRAW, copy.getStatus());
	}
}
