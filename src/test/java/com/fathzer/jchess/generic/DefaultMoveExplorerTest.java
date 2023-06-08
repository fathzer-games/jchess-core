package com.fathzer.jchess.generic;

import static com.fathzer.jchess.generic.DefaultMoveExplorer.*;
import static com.fathzer.jchess.Direction.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.util.BiIntPredicate;
import com.fathzer.jchess.util.U;

class DefaultMoveExplorerTest {

	@Test
	void test() {
		final Board<Move> board = FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1");
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		
		DefaultMoveExplorer explorer = new DefaultMoveExplorer();
		final BiIntPredicate v = (s,d) -> board.getPiece(d)==null || !board.getPiece(d).getColor().equals(board.getPiece(s).getColor());
		
		ChessGameState moves = new BasicMoveList();
		// The rook in a1 can't move
		int index = cs.getIndex("a1");
		final BoardExplorer exp = board.getExplorer();
		explorer.addMoves(moves, exp, index, NORTH, Integer.MAX_VALUE, v);
		explorer.addMoves(moves, exp, index, SOUTH, Integer.MAX_VALUE, v);
		explorer.addMoves(moves, exp, index, WEST, Integer.MAX_VALUE, v);
		explorer.addMoves(moves, exp, index, EAST, Integer.MAX_VALUE, v);
		assertEquals(0, moves.size(), U.to(moves, cs).toString());

		// The bishop in d3 can only move to c4
		index = cs.getIndex("d3");
		explorer.addMoves(moves, exp, index, NORTH_WEST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, index, SOUTH_WEST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, index, SOUTH_EAST, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, index, NORTH_EAST, Integer.MAX_VALUE, v, DEFAULT);
		assertEquals(Set.of("c4"), U.to(moves, cs));

		moves = new BasicMoveList();
		// The bishop in c6 can make 6 moves
		index = cs.getIndex("c6");
		explorer.addMoves(moves, exp, index, KNIGHT1, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT2, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT3, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT4, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT5, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT6, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT7, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, index, KNIGHT8, 1, v, DEFAULT);
		assertEquals(Set.of("a7","b8","d8","e5","d4","b4"), U.to(moves, cs));
	}
}
