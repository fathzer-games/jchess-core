package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.fen.FENParser;
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
}
