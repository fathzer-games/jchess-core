package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.MoveGenerator.MoveConfidence.*;
import static com.fathzer.jchess.SimpleMove.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.fen.FENParser;

class ChessBoardTest {

	@Test
	void test() {
		final List<PieceWithPosition> pieces = new FENParser("rnbqkbnr/pppppppp/8/8/8/2PP4/PP2PPPP/2RK3R w - - 0 1").getPieces();
		final Chess960Board board = new Chess960Board(pieces);
		final CoordinatesSystem cs = board.getCoordinatesSystem();

		assertTrue(board.makeMove(get(board, "d1", "c1"), UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		final long key = board.getHashKey();
		board.unmakeMove();
		assertTrue(board.makeMove(get(board, "d1", "d2"), UNSAFE));
		assertTrue(board.makeMove(get(board, "b8", "c6"), UNSAFE));
		assertTrue(board.makeMove(get(board, "d2", "c2"), UNSAFE));
		assertTrue(board.makeMove(get(board, "c6", "b8"), UNSAFE));
		assertTrue(board.makeMove(get(board, "c1", "e1"), UNSAFE));
		assertTrue(board.makeMove(get(board, "b8", "c6"), UNSAFE));
		assertTrue(board.makeMove(get(board, "c2", "c1"), UNSAFE));
		assertTrue(board.makeMove(get(board, "c6", "b8"), UNSAFE));
		assertTrue(board.makeMove(get(board, "e1", "d1"), UNSAFE));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
		assertEquals(key, board.getHashKey());
	}
}
