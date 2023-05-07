package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.standard.Coord;

class ChessBoardTest {

	@Test
	void test() {
		final List<PieceWithPosition> pieces = FENParser.getPieces("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/2RK3R");
		final ChessBoard board = new ChessBoard(pieces);
		final ChessGameState moves = board.newMoveList();
		moves.add(Coord.toIndex("d1"), Coord.toIndex("c1"));
		board.move(moves.get(0));
		assertEquals(Piece.WHITE_ROOK, board.getPiece(Coord.toIndex("d1")));
		assertEquals(Piece.WHITE_KING, board.getPiece(Coord.toIndex("c1")));
	}
}
