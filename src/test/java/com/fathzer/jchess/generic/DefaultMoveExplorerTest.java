package com.fathzer.jchess.generic;

import static com.fathzer.jchess.generic.DefaultMoveExplorer.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Dimension.Explorer;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.standard.Coord;
import com.fathzer.jchess.util.BiIntPredicate;
import com.fathzer.jchess.util.U;

class DefaultMoveExplorerTest {

	@Test
	void test() {
		final Board<Move> board = FENParser.from("r1b1k2r/1pppqppp/2n2n1b/pP6/4Q3/3B1P1N/P1PPP1P1/RNB1K2R w KQq a6 0 1");
		
		DefaultMoveExplorer explorer = new DefaultMoveExplorer(board);
		final BiIntPredicate v = (s,d) -> board.getPiece(d)==null || !board.getPiece(d).getColor().equals(board.getPiece(s).getColor());
		
		ChessGameState moves = new BasicMoveList();
		// The rook in a1 can't move
		Explorer exp = board.getDimension().new Explorer(Coord.toIndex("a1"));
		explorer.addMoves(moves, exp, -1, 0, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, 1, 0, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, 0, -1, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, 0, 1, Integer.MAX_VALUE, v, DEFAULT);
		assertEquals(0, moves.size(), U.to(moves).toString());

		// The bishop in d3 can only move to c4
		exp = board.getDimension().new Explorer(Coord.toIndex("d3"));
		explorer.addMoves(moves, exp, -1, -1, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, 1, -1, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, 1, 1, Integer.MAX_VALUE, v, DEFAULT);
		explorer.addMoves(moves, exp, -1, 1, Integer.MAX_VALUE, v, DEFAULT);
		assertEquals(Set.of("c4"), U.to(moves));

		moves = new BasicMoveList();
		// The bishop in c6 can make 6 moves
		exp = board.getDimension().new Explorer(Coord.toIndex("c6"));
		explorer.addMoves(moves, exp, -2, -1, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, -1, -2, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, 1, -2, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, 2, -1, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, 2, 1, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, 1, 2, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, -1, 2, 1, v, DEFAULT);
		explorer.addMoves(moves, exp, -2, 1, 1, v, DEFAULT);
		assertEquals(Set.of("a7","b8","d8","e5","d4","b4"), U.to(moves));
	}
}
