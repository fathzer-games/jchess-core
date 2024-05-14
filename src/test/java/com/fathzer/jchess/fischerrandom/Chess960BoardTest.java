package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;

class Chess960BoardTest implements MoveBuilder {
	@Test
	void test() {
		final List<PieceWithPosition> pieces = new FENParser("rnbqkbnr/pppppppp/8/8/8/2PP4/PP2PPPP/2RK3R w - - 0 1").getPieces();
		final Chess960Board board = new Chess960Board(pieces);
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		assertTrue(board.makeMove(move(board, "d1", "c1"), UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		final long key = board.getHashKey();
		board.unmakeMove();
		assertTrue(board.makeMove(move(board, "d1", "d2"), UNSAFE));
		assertTrue(board.makeMove(move(board, "b8", "c6"), UNSAFE));
		assertTrue(board.makeMove(move(board, "d2", "c2"), UNSAFE));
		assertTrue(board.makeMove(move(board, "c6", "b8"), UNSAFE));
		assertTrue(board.makeMove(move(board, "c1", "e1"), UNSAFE));
		assertTrue(board.makeMove(move(board, "b8", "c6"), UNSAFE));
		assertTrue(board.makeMove(move(board, "c2", "c1"), UNSAFE));
		assertTrue(board.makeMove(move(board, "c6", "b8"), UNSAFE));
		assertTrue(board.makeMove(move(board, "e1", "d1"), UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		assertEquals(key, board.getHashKey());
	}
	
	@Test
	void testDangerousCastling() {
		// Test castling where king seems safe ... but is not because he does not move and the rook does not defend him anymore
		final Board<Move> board = FENUtils.from("nrk1brnb/pp1ppppp/8/2p5/3P4/1N1Q1N2/1PP1PPPP/qRK1BR1B w KQkq - 2 10");
		final Move move = move(board,"c1","b1");
		assertFalse(board.makeMove(move, UNSAFE));
		assertFalse(board.makeMove(move, PSEUDO_LEGAL));
		assertFalse(board.getLegalMoves().contains(move));
	}
	
	@Test
	void testTrickyLegalCastling() {
		// Rook is attacked, but the castling is legal
		final Board<Move> board = FENUtils.from("nrk2rnb/pp1ppppp/6b1/q1p5/3P2Q1/1N3N2/1P2PPPP/1RK1BR1B w KQkq - 2 10");
		final Move move = move(board,"c1","b1");
		assertTrue(board.makeMove(move, UNSAFE));
		board.unmakeMove();
		assertTrue(board.makeMove(move, PSEUDO_LEGAL));
		board.unmakeMove();
		assertTrue(board.getLegalMoves().contains(move));
	}
	
	@Test
	void notRightRookCastling() {
		final String fenWithRook = "rn2k2r/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Kkq - 4 11";
		var board = FENUtils.from(fenWithRook);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
//		// Verify e1-g1 which is a castling with the wrong rook is not in legal moves
//		assertFalse(board.getLegalMoves().stream().map(m->m.toString(cs)).toList().contains("e1-g1"));
//		// Can't castling with the wrong rook
//		final BasicMove wrongMove = new BasicMove(board.getKingPosition(Color.WHITE), cs.getIndex("g1"));
//		assertFalse(board.makeMove(wrongMove, MoveConfidence.UNSAFE));
//		if (board.getMoves().stream().map(m->m.toString(cs)).toList().contains(wrongMove.toString(cs))) {
//			assertFalse(board.makeMove(wrongMove, MoveConfidence.PSEUDO_LEGAL));
//		}
		
		final String fenWithInnerRook = "rn2k1r1/ppp1pp1p/3p2p1/5bn1/P7/2N2B2/1PPPPP2/2BNK1RR w Gkq - 4 11";
		board = FENUtils.from(fenWithInnerRook);
		assertTrue(board.hasCastling(Castling.WHITE_KING_SIDE));     
		assertFalse(board.hasCastling(Castling.WHITE_QUEEN_SIDE));     
		assertTrue(board.hasCastling(Castling.BLACK_KING_SIDE));     
		assertTrue(board.hasCastling(Castling.BLACK_QUEEN_SIDE));
		
		assertEquals(cs.getIndex("g1"), board.getInitialRookPosition(Castling.WHITE_KING_SIDE));
		// Verify e1-h1 which is a castling with the wrong rook is not in legal moves
		assertFalse(board.getLegalMoves().stream().map(m->m.toString(cs)).toList().contains("e1-h1"));
		// Verify making e1-h1 move fails
		assertFalse(board.makeMove(new BasicMove(board.getKingPosition(Color.WHITE), cs.getIndex("h1")), MoveConfidence.UNSAFE));
		// Verify castling with the right rook succeeds
		assertTrue(board.makeMove(new BasicMove(board.getKingPosition(Color.WHITE), cs.getIndex("g1")), MoveConfidence.UNSAFE));
		board.unmakeMove();
		// Verify it still succeeds if wrong rook moves
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("h1"), cs.getIndex("h2")), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("h7"), cs.getIndex("h6")), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(new BasicMove(board.getKingPosition(Color.WHITE), cs.getIndex("g1")), MoveConfidence.UNSAFE));
	}
}
