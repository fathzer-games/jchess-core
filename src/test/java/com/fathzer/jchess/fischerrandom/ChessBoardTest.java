package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;

class ChessBoardTest {
	
	public static Move toMove(Board<Move> board, String from, String to) {
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		return new BasicMove(cs.getIndex(from), cs.getIndex(to));
	}

	@Test
	void test() {
		final List<PieceWithPosition> pieces = new FENParser("rnbqkbnr/pppppppp/8/8/8/2PP4/PP2PPPP/2RK3R w - - 0 1").getPieces();
		final Chess960Board board = new Chess960Board(pieces);
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		assertTrue(board.makeMove(toMove(board, "d1", "c1"), MoveConfidence.UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		final long key = board.getHashKey();
		board.unmakeMove();
		assertTrue(board.makeMove(toMove(board, "d1", "d2"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "b8", "c6"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "d2", "c2"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "c6", "b8"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "c1", "e1"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "b8", "c6"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "c2", "c1"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "c6", "b8"), MoveConfidence.UNSAFE));
		assertTrue(board.makeMove(toMove(board, "e1", "d1"), MoveConfidence.UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		assertEquals(key, board.getHashKey());
	}
	
	@Test
	void testInsufficientMaterial() {
		// white: 1 pawn, a queen, one knight vs black: 1 pawn, a rook, two knights, white is playing 
		Board<Move> board = FENUtils.from("rn2k3/3N4/5n2/pP4K1/8/3Q4/8/8 w - a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertFalse(board.isInsufficientMaterial());
		// Take black pawn
		assertTrue(board.makeMove(toMove(board, "b5", "a6"), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take white pawn
		assertTrue(board.makeMove(toMove(board, "a8", "a6"), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take black rook
		assertTrue(board.makeMove(toMove(board, "d3", "a6"), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take white queen
		assertTrue(board.makeMove(toMove(board, "b8", "a6"), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take black knight (it remains 1 knight vs 1 knight)
		assertTrue(board.makeMove(toMove(board, "d7", "f6"), MoveConfidence.UNSAFE));
		assertTrue(board.isInsufficientMaterial());
		
		
		// white: 1 pawn vs black: nothing, black is playing 
		board = FENUtils.from("8/8/8/6k1/6P1/4K3/8/8 b - - 0 1");
		System.out.println(board.getLegalMoves());
		assertFalse(board.isInsufficientMaterial());
		// Take white pawn
		assertTrue(board.makeMove(toMove(board, "g5", "g4"), MoveConfidence.UNSAFE));
		System.out.println(board.getLegalMoves());
		assertTrue(board.isInsufficientMaterial());
		board.unmakeMove();
		// Is move revert working?
		assertFalse(board.isInsufficientMaterial());
		System.out.println(board.getStatus());
		System.out.println(board.getLegalMoves());
	}
}
