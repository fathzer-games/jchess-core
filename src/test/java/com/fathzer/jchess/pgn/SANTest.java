package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.StandardChessRules;
import com.fathzer.jchess.standard.Coord;

class SANTest {

	@Test
	void test() {
		SAN san = new SAN(StandardChessRules.INSTANCE);
		
		Board<Move> board = FENParser.from("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
		assertEquals("exd4", san.get(board, new SimpleMove("e5","d4")));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(Coord.toIndex("d4")));
		
		board = FENParser.from("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
		san.withPlayMove(true);
		assertEquals("Qxd2+", san.get(board, new SimpleMove("b4","d2")));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(Coord.toIndex("d2")));
		
		board = FENParser.from("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
		assertEquals("Qa3#", san.get(board, new SimpleMove("c1","a3")));
		
		
	}

}
