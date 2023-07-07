package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.fen.FENParser;

class ChessBoardTest {

	@Test
	void test() {
		final List<PieceWithPosition> pieces = FENParser.getPieces("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/2RK3R");
		final Chess960Board board = new Chess960Board(pieces);
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final ChessGameState moves = board.newMoveList();
		moves.add(cs.getIndex("d1"), cs.getIndex("c1"));
		board.makeMove(moves.get(0));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(cs.getIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(cs.getIndex("c1")));
	}
}
