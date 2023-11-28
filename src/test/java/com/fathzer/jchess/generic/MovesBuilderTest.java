package com.fathzer.jchess.generic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.util.U;

class MovesBuilderTest {
	private static List<Move> filterFrom(List<Move> moves, String coord, CoordinatesSystem cs) {
		final int index = cs.getIndex(coord);
		return moves.stream().filter(m -> m.getFrom()==index).collect(Collectors.toList());
	}
	
	@Test
	void test() {
		ChessBoard board = (ChessBoard) FENUtils.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		
		MovesBuilder explorer = new MovesBuilder(board);
		List<Move> moves = explorer.getLegalMoves();
		
		// The rook in a1 can't move
		List<Move> rookMoves = filterFrom(moves, "a1", cs);
		assertEquals(0, rookMoves.size(), U.to(rookMoves, cs).toString());

		// The bishop in d3 can only move to c4
		List<Move> bishopMoves = filterFrom(moves, "d3", cs);
		assertEquals(Set.of("c4"), U.to(bishopMoves, cs));

		// The knight in c6 can make 6 moves
		explorer = new MovesBuilder((ChessBoard) FENUtils.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R b KQq - 0 1"));
		moves = filterFrom(explorer.getLegalMoves(), "c6", cs);
		assertEquals(Set.of("a7","b8","d8","e5","d4","b4"), U.to(moves, cs));
	}
}
