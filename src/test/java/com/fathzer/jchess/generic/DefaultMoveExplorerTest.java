package com.fathzer.jchess.generic;

import static com.fathzer.jchess.generic.DefaultMoveExplorer.*;
import static com.fathzer.jchess.Direction.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.util.U;

class DefaultMoveExplorerTest {
	private static void reset(DefaultMoveExplorer explorer, String coord) {
		final int index = explorer.getBoard().getCoordinatesSystem().getIndex(coord);
		explorer.getFrom().reset(index);
		explorer.getTo().reset(index);
		explorer.clearMoves();
	}
	
	@Test
	void test() {
		Board<Move> board = FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		
		DefaultMoveExplorer explorer = new DefaultMoveExplorer(board);
		final BiPredicate<BoardExplorer, BoardExplorer> v = (s,d) -> d.getPiece()==null || !d.getPiece().getColor().equals(s.getPiece().getColor());
		
		// The rook in a1 can't move
		reset(explorer, "a1");
		explorer.addAllMoves(NORTH, v);
		explorer.addAllMoves(SOUTH, v);
		explorer.addAllMoves(WEST, v);
		explorer.addAllMoves(EAST,  v);
		ChessGameState moves = explorer.getMoves();
		assertEquals(0, moves.size(), U.to(moves, cs).toString());

		// The bishop in d3 can only move to c4
		reset(explorer, "d3");
		explorer.addMoves(NORTH_WEST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(SOUTH_WEST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(SOUTH_EAST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(NORTH_EAST, Integer.MAX_VALUE, v, DEFAULT);
		assertEquals(Set.of("c4"), U.to(explorer.getMoves(), cs));

		// The knight in c6 can make 6 moves
		explorer = new DefaultMoveExplorer(FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R b KQq - 0 1"));
		reset(explorer, "c6");
		explorer.addMove(KNIGHT1, v, DEFAULT);
		explorer.addMove(KNIGHT2, v, DEFAULT);
		explorer.addMove(KNIGHT3, v, DEFAULT);
		explorer.addMove(KNIGHT4, v, DEFAULT);
		explorer.addMove(KNIGHT5, v, DEFAULT);
		explorer.addMove(KNIGHT6, v, DEFAULT);
		explorer.addMove(KNIGHT7, v, DEFAULT);
		explorer.addMove(KNIGHT8, v, DEFAULT);
		assertEquals(Set.of("a7","b8","d8","e5","d4","b4"), U.to(explorer.getMoves(), cs));
	}
}
