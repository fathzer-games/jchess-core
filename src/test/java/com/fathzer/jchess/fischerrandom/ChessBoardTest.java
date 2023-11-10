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

	@Test
	void test() {
		final List<PieceWithPosition> pieces = new FENParser("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/2RK3R w - - 0 1").getPieces();
		final Chess960Board board = new Chess960Board(pieces);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		board.makeMove(new BasicMove(cs.getIndex("d1"), cs.getIndex("c1")), MoveConfidence.LEGAL);
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
	}
	
	@Test
	void testInsufficientMaterial() {
		// white: 1 pawn, a queen, one knight vs black: 1 pawn, a rook, two knights, white is playing 
		Board<Move> board = FENUtils.from("rn2k3/3N4/5n2/pP4K1/8/3Q4/8/8 w - a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertFalse(board.isInsufficientMaterial());
		// Take black pawn
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("b5"), cs.getIndex("a6")), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take white pawn
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("a8"), cs.getIndex("a6")), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take black rook
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("d3"), cs.getIndex("a6")), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take white queen
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("b8"), cs.getIndex("a6")), MoveConfidence.UNSAFE));
		assertFalse(board.isInsufficientMaterial());
		// Take black knight (it remains 1 knight vs 1 knight)
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("d7"), cs.getIndex("f6")), MoveConfidence.UNSAFE));
		assertTrue(board.isInsufficientMaterial());
		
		
		// white: 1 pawn vs black: nothing, black is playing 
		board = FENUtils.from("8/8/8/6k1/6P1/4K3/8/8 b - - 0 1");
		System.out.println(board.getLegalMoves());
		assertFalse(board.isInsufficientMaterial());
		// Take white pawn
		assertTrue(board.makeMove(new BasicMove(cs.getIndex("g5"), cs.getIndex("g4")), MoveConfidence.UNSAFE));
		System.out.println(board.getLegalMoves());
		assertTrue(board.isInsufficientMaterial());
		board.unmakeMove();
		// Is move revert working?
		assertFalse(board.isInsufficientMaterial());
		System.out.println(board.getStatus());
		System.out.println(board.getLegalMoves());
	}
}
