package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.games.GameState;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ChessRules;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.standard.Coord;

class StandardChessRulesTest {
	@Test
	void pawnsTest() {
		final Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2n2n1b/pP6/4QN2/B2B1P1N/P1pPP1P1/R3K2R b KQkq - 0 1");
		final List<Move> blackMoves = getMoves(board);
		assertEquals(Set.of("a4"), getTo(getMoves(blackMoves, "a5")));
		assertEquals(Set.of("g5","g6"), getTo(getMoves(blackMoves, "g7")));
		assertEquals(Set.of("b6"), getTo(getMoves(blackMoves, "b7")));
		final List<Move> promotions = getMoves(blackMoves, "c2");
		assertEquals(Set.of("c1"),getTo(promotions));
		assertEquals(Set.of(Piece.BLACK_ROOK,Piece.BLACK_BISHOP,Piece.BLACK_KNIGHT,Piece.BLACK_QUEEN), promotions.stream().map(Move::promotedTo).collect(Collectors.toSet()));

		board.copy(FENParser.from("r1b1k2r/1p1pqppp/2n2n1b/pP6/4QN2/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1"));
		final List<Move> whiteMoves = getMoves(board);
		assertEquals(Set.of("a6","b6","c6"), getTo(getMoves(whiteMoves, "b5")));

		
		board.copy(FENParser.from("rK1k4/1P6/8/8/8/8/8/8 w - - 0 1"));
		final List<Move> checkMoves = getMoves(board, "b7");
		assertEquals(Set.of("a8"), getTo(checkMoves));
		assertEquals(Set.of(Piece.WHITE_ROOK,Piece.WHITE_BISHOP,Piece.WHITE_KNIGHT,Piece.WHITE_QUEEN), checkMoves.stream().map(Move::promotedTo).collect(Collectors.toSet()));

		// En-passant capture is not possible (king would be in check)
		board.copy(FENParser.from("4K3/8/8/8/R4pPk/8/8/8 b - g3 0 1"));
		assertEquals(Set.of("f3"), getTo(getMoves(board, "f4")));
	}
	
	private List<Move> getMoves(Board<Move> board) {
		GameState<Move> moves = StandardChessRules.INSTANCE.getState(board);
		return IntStream.range(0,moves.size()).mapToObj(moves::get).collect(Collectors.toList());
	}

	private List<Move> getMoves(Board<Move> board, String from) {
		return getMoves(getMoves(board),from);
	}

	private List<Move> getMoves(List<Move> moves, String from) {
		final int fromIndex = Coord.toIndex(from);
		return moves.stream().filter(m-> m.getFrom()==fromIndex).collect(Collectors.toList());
	}

	private Set<String> getTo(List<Move> moves) {
		return moves.stream().map(m -> Coord.toString(m.getTo())).collect(Collectors.toSet());
	}
	
	private Set<String> getFrom(List<Move> moves) {
		return moves.stream().map(m -> Coord.toString(m.getFrom())).collect(Collectors.toSet());
	}

	@Test
	void kingsTest() {
		final Board<Move> board = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		assertEquals(Set.of("f1","f2","g1"), getTo(getMoves(board, "e1")), "Problem in king move");
		
		// Making a standard move that does not make position safe should not work
		assertEquals(Set.of("f2"), getTo(getMoves(FENParser.from("4k3/8/8/8/8/8/3PP3/r3K2R w K - 0 1"), "e1")), "Problem in king move");
		
		// Should not castle when in check
		assertEquals(Set.of("d1","f1"), getTo(getMoves(FENParser.from("4k3/8/8/8/8/3n4/3PP3/4K2R w K - 0 1"), "e1")), "Problem in king move");
		
		// Can castle
		List<Move> moves = getMoves(FENParser.from("r3kb1r/ppp2ppp/2nqb2n/P2p4/2P1p3/6R1/1PQPPPPP/1NB1KBNR b Kkq - 1 8"));
		moves = moves.stream().filter(m->"e8".equals(Coord.toString(m.getFrom()))).collect(Collectors.toList());
		assertEquals(Set.of("c8","d8", "d7", "e7"), getTo(moves));
		assertEquals(4, moves.size(), moves.toString());
	}

	@Test
	void checkTest() {
		// Piece not involved in check can't move
		// Queen can't move, catching white knight does not fix check
		// Only king can move
		List<Move> moves = getMoves(FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R b KQkq - 0 1"));
		assertEquals(Set.of("e8"), getFrom(moves), "Only king can move");
		assertEquals(Set.of("d8", "f8"), getTo(moves));
	}
	
	@Test
	void stateTest() {
		assertEquals(Status.BLACK_WON, StandardChessRules.INSTANCE.getState(FENParser.from("rnb1k1nr/pppp1ppp/3bp3/8/3P4/6B1/PPP1PPPP/RNq1KBNR w KQkq - 6 5")).getStatus());
		assertEquals(Status.WHITE_WON, StandardChessRules.INSTANCE.getState(FENParser.from("R3k3/7R/8/8/8/8/8/K7 b - - 6 5")).getStatus());
		assertEquals(Status.DRAW, StandardChessRules.INSTANCE.getState(FENParser.from("6k1/8/7Q/8/8/8/8/K4R2 b - - 6 5")).getStatus());
		assertEquals(Status.PLAYING, StandardChessRules.INSTANCE.getState(FENParser.from("6k1/8/7Q/8/8/8/8/K4R2 w - - 6 5")).getStatus());
		// Only one knight at each side
		assertEquals(Status.DRAW, StandardChessRules.INSTANCE.getState(FENParser.from("1n2k3/8/8/8/8/8/8/1N2K3 w - - 0 1")).getStatus());
		// Only kings
		assertEquals(Status.DRAW, StandardChessRules.INSTANCE.getState(FENParser.from("8/8/8/8/8/6k1/8/4K3 w - - 0 1")).getStatus());
		// If one pawn remains, no draw
		assertEquals(Status.PLAYING, StandardChessRules.INSTANCE.getState(FENParser.from("8/8/8/8/8/6k1/7p/1N2K3 w - - 0 1")).getStatus());
	}
	
//	@Test
	void speedTest() {
		ChessRules rules = StandardChessRules.INSTANCE;
		final Board<Move> wBoard = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R w KQkq a6 0 1");
		final Board<Move> bBoard = FENParser.from("r1b1k2r/1p1pqppp/2nN1n1b/pP6/4Q3/B2B1P1N/P1pPP1P1/R3K2R b KQkq a6 0 1");
		assertEquals(48, rules.getState(wBoard).size());
		assertEquals(2, rules.getState(bBoard).size());
		for (int i=0;i<500000;i++) {
			rules.getState(wBoard);
			rules.getState(bBoard);
		}
		int nb = 50000;
		long start = System.currentTimeMillis();
		for (int i=0;i<nb;i++) {
			rules.getState(wBoard);
		}
		long duration = System.currentTimeMillis()-start;
		System.out.println(String.format("Nombre de calculs de la totalité des coups blancs possibles par seconde: %d", nb*1000L/duration));
		start = System.currentTimeMillis();
		for (int i=0;i<nb;i++) {
			rules.getState(bBoard);
		}
		duration = System.currentTimeMillis()-start;
		System.out.println(String.format("Nombre de calcul de la totalité des coups noirs possibles par seconde: %d", nb*1000L/duration));
		
		final List<Move> whiteMoves = getMoves(wBoard);
		System.out.println(whiteMoves.size() + "/" +getMoves(bBoard).size());
		System.out.println(whiteMoves.stream().map(this::toString).collect(Collectors.toList()));
	}
	
	private String toString(Move m) {
		return Coord.toString(m.getFrom())+"-"+Coord.toString(m.getTo());
	}
}
