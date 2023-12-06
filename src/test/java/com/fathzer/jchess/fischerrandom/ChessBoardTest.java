package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.MoveGenerator.MoveConfidence.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.fen.FENUtils;

class ChessBoardTest implements MoveBuilder {
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
}
