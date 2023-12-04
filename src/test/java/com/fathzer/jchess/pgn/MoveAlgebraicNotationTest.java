package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;

class MoveAlgebraicNotationTest implements MoveBuilder {

	@Test
	void test() {
		final MoveAlgebraicNotationBuilder san = new MoveAlgebraicNotationBuilder();
		
		Board<Move> board = FENUtils.from("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals("exd4", san.get(board, move(board, "e5","d4")));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(cs.getIndex("d4")));
		
		board = FENUtils.from("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
		san.withPlayMove(true);
		assertEquals("Qxd2+", san.get(board, move(board, "b4","d2")));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(cs.getIndex("d2")));
		
		board = FENUtils.from("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
		assertEquals("Qa3#", san.get(board, move(board, "c1","a3")));
		
		board = FENUtils.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("O-O", san.get(board, move(board, "e8","g8")));
		board = FENUtils.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("O-O-O", san.get(board, move(board, "e8","c8")));
		
		board = FENUtils.from("rnbqkbnr/p1pppppp/8/PpP5/8/8/1P1PPPPP/RNBQKBNR w KQkq b6 0 1");
		assertEquals("axb6 e.p.", san.get(board, move(board, "a5","b6")));
		assertNull(board.getPiece(cs.getIndex("b5")));
		
		board = FENUtils.from("2kr3r/Ppp1pppp/3p4/8/2P5/1P3K2/2PP2PP/R1B1Q3 w - - 0 1");
		assertEquals("a8=Q+",san.get(board, move(board, "a7","a8", Piece.WHITE_QUEEN)));
		
		board = FENUtils.from("4k3/8/8/8/8/8/r5q1/4K3 b - - 0 1");
		assertEquals("Rd2",san.get(board, move(board, "a2","d2")),"Problem with DRAW");
		
		// Disambiguation
		board = FENUtils.from("2kr3r/pppppppp/8/R7/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
		san.withPlayMove(false);
		assertEquals("R1a3", san.get(board, move(board, "a1","a3")));
		san.withPlayMove(true);
		assertEquals("Qh4e1", san.get(board, move(board, "h4","e1")));
		assertEquals("Rdf8", san.get(board, move(board, "d8","f8")));
		
		// Illegal moves
		final Board<Move> board2 = board.create();
		board2.copy(board);
		final Move move2 = move(board, "f3","g2");
		assertThrows(IllegalArgumentException.class, () -> san.get(board2, move2));
		
		final Board<Move> board3 = FENUtils.from("2kr3r/Rppppppp/8/8/2P1Q2Q/1P3K2/2PP2PP/RNB4Q w - - 0 1");
		final Move move3 = move(board, "a7","a8", Piece.WHITE_QUEEN);
		assertThrows(IllegalArgumentException.class, () -> san.get(board3, move3));
	}
	
	@Test
	void testCustom() {
		final MoveAlgebraicNotationBuilder san = new MoveAlgebraicNotationBuilder();
		san.withCaptureSymbol(':').withCastlingSymbolBuilder(s -> s==Castling.Side.KING?"0-0":"0-0-0");
		san.withCheckSymbol("ch").withCheckmateSymbol("++");
		san.withEnPassantSymbol("").withPromotionSymbolBuilder(p->p.getNotation().toUpperCase());
		
		Board<Move> board = FENUtils.from("rnbqkbnr/pppp1ppp/8/4p3/3P3P/8/PPP1PPP1/RNBQKBNR b KQkq d3 0 2");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		assertEquals("e:d4", san.get(board, move(board, "e5","d4")));
		assertEquals(Piece.WHITE_PAWN, board.getPiece(cs.getIndex("d4")));
		
		board = FENUtils.from("r1b1k2r/ppp2ppp/5n2/7P/Pq1n4/6P1/1P1Q1P2/1R2KBNR b Kkq - 1 13");
		san.withPlayMove(true);
		assertEquals("Q:d2ch", san.get(board, move(board, "b4","d2")));
		assertEquals(Piece.BLACK_QUEEN, board.getPiece(cs.getIndex("d2")));
		
		board = FENUtils.from("r5k1/pp3ppp/2p2n2/P5PP/KP3P2/2r5/8/1bq5 b - - 0 28");
		assertEquals("Qa3++", san.get(board, move(board, "c1","a3")));
		
		board = FENUtils.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("0-0", san.get(board, move(board, "e8","g8")));
		board = FENUtils.from("r3k2r/pppnqppp/3b1n2/5b2/8/4P3/PPP2PPP/RNBQKBNR b KQkq - 3 6");
		assertEquals("0-0-0", san.get(board, move(board, "e8","c8")));
		
		board = FENUtils.from("rnbqkbnr/p1pppppp/8/PpP5/8/8/1P1PPPPP/RNBQKBNR w KQkq b6 0 1");
		assertEquals("a:b6", san.get(board, move(board, "a5","b6")));
		assertNull(board.getPiece(cs.getIndex("b5")));
		
		board = FENUtils.from("2kr3r/Ppp1pppp/3p4/8/2P5/1P3K2/2PP2PP/R1B1Q3 w - - 0 1");
		assertEquals("a8Qch",san.get(board, move(board, "a7","a8", Piece.WHITE_QUEEN)));
	}
}
