package com.fathzer.jchess.generic;

import static com.fathzer.games.MoveGenerator.MoveConfidence.UNSAFE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.fen.FENUtils;


class MovesBuilderTest {
	@Test
	void testMakeMoveWithUnsafeAcceptsLegal() {
		testAllLegalMoves("rn1qk2r/1ppb1ppp/P2Bpn2/3p4/3P4/4P3/P1P2PPP/RN1QKBNR b KQkq - 0 7");
		testAllLegalMoves("3rk2r/PR3p1p/4pn2/3p2pP/3P2bb/4P2N/P1P2PP1/1N2K2R w k - 0 7");
		testAllLegalMoves("8/2p5/3p4/KP5r/1R3pPk/8/4P3/8 b - g3 0 1");
	}
	
	private void testAllLegalMoves(String fen) {
		final Board<Move> mvg = FENUtils.from(fen);
		final CoordinatesSystem cs = mvg.getCoordinatesSystem();
		for (Move move:mvg.getLegalMoves()) {
			assertTrue(mvg.makeMove(move, UNSAFE), "Error for "+move.toString(cs)+" on "+fen);
			mvg.unmakeMove();
		}
	}

	@Test
	void testInvalidMovesWithUnsafeFindIllegal() {
		Board<Move> mvg = FENUtils.from("rn1qk2r/1ppb1ppp/P2Bpn2/3p4/3P4/4P3/P1P2PPP/RN1QKBNR b KQkq - 0 7");
		final CoordinatesSystem cs = mvg.getCoordinatesSystem();
		
		// Move an non existing piece
		Move move = new SimpleMove(cs, "H6", "H5");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Move a piece of the wrong color
		move = new SimpleMove(cs, "D1", "D3");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Move a piece through another piece 
		move = new SimpleMove(cs, "D6", "B6");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Piece takes own piece
		move = new SimpleMove(cs, "F6", "D5");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Piece goes to unreachable cell
		move = new SimpleMove(cs, "D5", "A5");
//		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pawn takes no piece
		move = new SimpleMove(cs, "H7", "G6");
//		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Pawn goes on opponent piece
		move = new SimpleMove(cs, "D5", "D4");
//		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pawn goes on own piece
		move = new SimpleMove(cs, "F7", "F6");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// Imaginary promotion
		move = new SimpleMove(cs, "G7", "G6", Piece.BLACK_QUEEN);
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Castling with free cells in check
		//TODO Should also test castling in Chess960
		move = new SimpleMove(cs, "e8", "g8");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		mvg = FENUtils.from("3rk2r/PR3p1p/4pn2/3p2pP/3P2bb/4P2N/P1P2PP1/1N2K2R w k - 0 7");

		// Promotion to a pawn
		move = new SimpleMove(cs, "A7", "A8", Piece.WHITE_PAWN);
		assertFalse(mvg.makeMove(move, UNSAFE)); //FIXME

		// Promotion of wrong color
		move = new SimpleMove(cs, "A7", "A8", Piece.BLACK_QUEEN);
		assertFalse(mvg.makeMove(move, UNSAFE)); //FIXME
		
		// Promotion of a piece that is not a pawn
		move = new SimpleMove(cs, "B7", "B8", Piece.WHITE_QUEEN);
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// imaginary en-passant
		move = new SimpleMove(cs, "H5", "G6");
//		assertFalse(mvg.makeMove(move, UNSAFE));

		// Pinned piece
		move = new SimpleMove(cs, "F2", "F3");
//		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// King goes to in check cell
		move = new SimpleMove(cs, "E1", "E2");
		assertFalse(mvg.makeMove(move, UNSAFE));

		// Illegal castling because castling is not available
		move = new SimpleMove(cs, "E1", "G1");
		assertFalse(mvg.makeMove(move, UNSAFE));

		mvg = FENUtils.from("3rk2r/PR3p1p/4pn2/3p2pP/1b1P2b1/4P2N/P1P2PP1/1N2K2R w Kk - 0 7");
		
		// Illegal castling because king is in check
		move = new SimpleMove(cs, "E1", "G1");
		assertFalse(mvg.makeMove(move, UNSAFE));
		
		// En passant that leads to check
		mvg = FENUtils.from("8/2p5/3p4/KP5r/1R3pPk/8/4P3/8 b - g3 0 1");
		move = new SimpleMove(cs, "F4", "G3");
//		assertFalse(mvg.makeMove(move, UNSAFE));
	}
	
	
}
