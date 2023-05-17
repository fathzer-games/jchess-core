package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.SimpleMove;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.StandardChessRules;
import com.fathzer.jchess.standard.Coord;

class MoveAlgebraicNotationTest {

	@Test
	void test() {
		final MoveAlgebraicNotation san = new MoveAlgebraicNotation(StandardChessRules.INSTANCE);
		
		Board<Move> board = FENParser.from("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
		assertEquals("exd4", san.get(board, new SimpleMove("e5","d4")));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(Coord.toIndex("d4")));
		
		board = FENParser.from("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
		san.withPlayMove(true);
		assertEquals("Qxd2+", san.get(board, new SimpleMove("b4","d2")));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(Coord.toIndex("d2")));
		
		board = FENParser.from("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
		assertEquals("Qa3#", san.get(board, new SimpleMove("c1","a3")));
		
		board = FENParser.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("O-O", san.get(board, new SimpleMove("e8","g8")));
		board = FENParser.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("O-O-O", san.get(board, new SimpleMove("e8","c8")));
		
		board = FENParser.from("rnbqkbnr/p1pppppp/8/PpP5/8/8/1P1PPPPP/RNBQKBNR w KQkq b6 0 1");
		assertEquals("axb6 e.p", san.get(board, new SimpleMove("a5","b6")));
		assertNull(board.getPiece(Coord.toIndex("b5")));
		
		board = FENParser.from("2kr3r/Ppp1pppp/3p4/8/2P5/1P3K2/2PP2PP/R1B1Q3 w - - 0 1");
		assertEquals("a8=Q+",san.get(board, new SimpleMove("a7","a8", Piece.WHITE_QUEEN)));
		
		board = FENParser.from("4k3/8/8/8/8/8/r5q1/4K3 b - - 0 1");
		assertEquals("Rd2",san.get(board, new SimpleMove("a2","d2")),"Problem with DRAW");
		
		// Disambiguation
		board = FENParser.from("2kr3r/pppppppp/8/R7/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
		san.withPlayMove(false);
		assertEquals("R1a3", san.get(board, new SimpleMove("a1","a3")));
		san.withPlayMove(true);
		assertEquals("Qh4e1", san.get(board, new SimpleMove("h4","e1")));
		assertEquals("Rdf8", san.get(board, new SimpleMove("d8","f8")));
		
		// Illegal moves
		final Board<Move> board2 = board.create();
		board2.copy(board);
		final SimpleMove move2 = new SimpleMove("f3","g2");
		assertThrows(IllegalArgumentException.class, () -> san.get(board2, move2));
		
		final Board<Move> board3 = FENParser.from("2kr3r/Rppppppp/8/8/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
		final SimpleMove move3 = new SimpleMove("a7","a8", Piece.WHITE_QUEEN);
		assertThrows(IllegalArgumentException.class, () -> san.get(board3, move3));
	}
	
	@Test
	void testCustom() {
		final MoveAlgebraicNotation san = new MoveAlgebraicNotation(StandardChessRules.INSTANCE);
		san.withCaptureSymbol(':').withCastlingSymbolBuilder(s -> s==Castling.Side.KING?"0-0":"0-0-0");
		san.withCheckSymbol("ch").withCheckmateSymbol("++");
		san.withEnPassantSymbol("").withPromotionSymbolBuilder(p->p.getNotation().toUpperCase());
		
		Board<Move> board = FENParser.from("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
		assertEquals("e:d4", san.get(board, new SimpleMove("e5","d4")));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(Coord.toIndex("d4")));
		
		board = FENParser.from("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
		san.withPlayMove(true);
		assertEquals("Q:d2ch", san.get(board, new SimpleMove("b4","d2")));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(Coord.toIndex("d2")));
		
		board = FENParser.from("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
		assertEquals("Qa3++", san.get(board, new SimpleMove("c1","a3")));
		
		board = FENParser.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("0-0", san.get(board, new SimpleMove("e8","g8")));
		board = FENParser.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("0-0-0", san.get(board, new SimpleMove("e8","c8")));
		
		board = FENParser.from("rnbqkbnr/p1pppppp/8/PpP5/8/8/1P1PPPPP/RNBQKBNR w KQkq b6 0 1");
		assertEquals("a:b6", san.get(board, new SimpleMove("a5","b6")));
		assertNull(board.getPiece(Coord.toIndex("b5")));
		
		board = FENParser.from("2kr3r/Ppp1pppp/3p4/8/2P5/1P3K2/2PP2PP/R1B1Q3 w - - 0 1");
		assertEquals("a8Qch",san.get(board, new SimpleMove("a7","a8", Piece.WHITE_QUEEN)));
	}
}
