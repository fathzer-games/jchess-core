package com.fathzer.jchess.generic;

import static com.fathzer.games.MoveGenerator.MoveConfidence.PSEUDO_LEGAL;
import static com.fathzer.games.MoveGenerator.MoveConfidence.UNSAFE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator;

abstract class GenericMovesCheckerTest<M> {
	public abstract MoveGenerator<M> fromFEN(String fen);
	protected String toString(M move) {
		return move.toString();
	}
	public M toMove(String from, String to) {
		return toMove(from, to, null);
	}
	protected abstract M toMove(String from, String to, String promotion);
	
	
	@Test
	void testMakeMoveWithUnsafeAcceptsLegal() {
		testAllLegalMoves("rn1qk2r/1ppb1ppp/P2Bpn2/3p4/3P4/4P3/P1P2PPP/RN1QKBNR b KQkq - 0 7");
		testAllLegalMoves("3rk2r/PR3p1p/4pn2/3p2pP/3P2bb/4P2N/P1P2PP1/1N2K2R w k - 0 7");
		testAllLegalMoves("8/2p5/3p4/KP5r/1R3pPk/8/4P3/8 b - g3 0 1");
		
		// Check king can castle (was a bug of first version)
		String fen = "3rk2r/PR3p1p/4pn2/3p2pP/3P2bb/4P2N/P1P2PP1/1N2K2R w Kk - 0 7";
		MoveGenerator<M> mvg = fromFEN(fen);
		M move = toMove("E1", "G1");
		assertTrue(mvg.makeMove(move, UNSAFE), "Error for "+toString(move)+" on "+fen);

		// Check pinned piece can move in pinned direction or opposite
		fen = "rnbqk1nr/pppp1ppp/4p3/b7/3P4/2B5/PPP1PPPP/RN1QKBNR w KQkq - 0 1";
		mvg = fromFEN(fen);
		move = toMove("C3", "B4");
		assertTrue(mvg.makeMove(move, UNSAFE), "Error for "+toString(move)+" on "+fen);
		mvg.unmakeMove();
		
		move = toMove("C3", "D2");
		assertTrue(mvg.makeMove(move, UNSAFE), "Error for "+toString(move)+" on "+fen);
	}
	
	private void testAllLegalMoves(String fen) {
		final MoveGenerator<M> mvg = fromFEN(fen);
		for (M move:mvg.getLegalMoves()) {
//			assertEquals(Piece.WHITE_PAWN, mvg.getPiece(21));
//			System.out.println(move.toString(cs)+"="+move.getFrom()+" -> "+move.getTo());
			assertTrue(mvg.makeMove(move, UNSAFE), "Error for "+toString(move)+" on "+fen);
			mvg.unmakeMove();
//			assertEquals(Piece.WHITE_PAWN, mvg.getPiece(21), "After move "+move.toString(cs));
		}
	}

	@Test
	void testInvalidMovesWithUnsafeFindIllegal() {
		MoveGenerator<M> mvg = fromFEN("rn1qk2r/1ppb1ppp/P2Bpn2/3p4/3P4/4P3/P1P2PPP/RN1QKBNR b KQkq - 0 7");
		
		// Move an non existing piece
		M move = toMove("H6", "H5");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Move a piece of the wrong color
		move = toMove("D1", "D3");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Move a piece through another piece 
		move = toMove("D8", "B6");
		assertFalse(mvg.makeMove(move, UNSAFE));
		move = toMove("F7", "F5");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		
		// Pawn goes to unreachable cell
		move = toMove("H7", "H4");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Piece takes own piece
		move = toMove("F6", "D5");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Queen goes to unreachable cell
		move = toMove("D5", "A5");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// King goes to unreachable cell
		move = toMove("E8", "B6");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pawn takes no piece
		move = toMove("H7", "G6");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Pawn goes on opponent piece
		move = toMove("D5", "D4");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pawn goes on own piece
		move = toMove("F7", "F6");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Imaginary promotion
		move = toMove("G7", "G6", "BLACK_QUEEN");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Castling with free cells in check
		move = toMove("e8", "g8");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Castling with piece between rook and king  
		move = toMove("e8", "c8");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		mvg = fromFEN("3rk2r/PR3p1p/4pn2/3p2pP/3P2bb/4P2N/P1P2PP1/1N2K2R w k - 0 7");

		// Promotion to a pawn
		move = toMove("A7", "A8", "WHITE_PAWN");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Promotion of wrong color
		move = toMove("A7", "A8", "BLACK_QUEEN");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Promotion of a piece that is not a pawn
		move = toMove("B7", "B8", "WHITE_QUEEN");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// imaginary en-passant
		move = toMove("H5", "G6");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pinned piece
		move = toMove("F2", "F3");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// King goes to in check cell
		move = toMove("E1", "E2");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Illegal castling because castling is not available
		move = toMove("E1", "G1");
		assertFalse(mvg.makeMove(move, UNSAFE));

		mvg = fromFEN("3rk2r/PR3p1p/4pn2/3p2pP/1b1P2b1/4P2N/P1P2PP1/1N2K2R w Kk - 0 7");
		
		// Illegal castling because king is in check
		move = toMove("E1", "G1");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// En passant that leads to check
		mvg = fromFEN("8/2p5/3p4/KP5r/1R3pPk/8/4P3/8 b - g3 0 1");
		move = toMove("F4", "G3");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// King takes attacking piece ... on an protected cell
		mvg = fromFEN("rnb1k1nr/pppp1ppp/4p3/B7/1q1P4/8/PPPbPPPP/RN1QKBNR w KQkq - 0 1");
		move = toMove("E1","D2");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// King is in check ... and remains in check
		move = toMove("C2","C3");
		assertFalse(mvg.makeMove(move, UNSAFE));
	}
	
	@Test
	void testTrickyLegalCastling() {
		// Rook is attacked, but the castling is legal
		MoveGenerator<M> mvg = fromFEN("r3k2r/1p1pppp1/2nb1nb1/2p5/3P4/3B1N2/1PNBPPP1/R3K2R w KQkq - 2 10");
		final M move = toMove("E1","C1");
		assertTrue(mvg.makeMove(move, UNSAFE));
		mvg.unmakeMove();
		assertTrue(mvg.makeMove(move, PSEUDO_LEGAL));
		mvg.unmakeMove();
		assertTrue(mvg.getLegalMoves().contains(move));
	}

}
