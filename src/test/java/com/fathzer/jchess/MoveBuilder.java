package com.fathzer.jchess;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fathzer.jchess.generic.BasicMove;

public interface MoveBuilder {
	default Move move(Board<Move> board, String from, String to) {
		return move(board, from, to, null);
	}
	default Move move(Board<Move> board, String from, String to, Piece promoted) {
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		return new BasicMove(cs.getIndex(from.toLowerCase()), cs.getIndex(to.toLowerCase()), promoted);
	}
	default String asString(Collection<Move> moves, Board<Move> board) {
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		return moves.stream().map(m -> m.toString(cs)).collect(Collectors.joining(", ", "[", "]"));
	}
}