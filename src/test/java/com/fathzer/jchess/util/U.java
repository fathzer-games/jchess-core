package com.fathzer.jchess.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;

import lombok.experimental.UtilityClass;

@UtilityClass
public class U {
	public static Set<String> to(ChessGameState moves, CoordinatesSystem cs) {
		return IntStream.range(0, moves.size()).mapToObj(i -> cs.getAlgebraicNotation(moves.get(i).getTo())).collect(Collectors.toSet());
	}

	public static Set<String> from(ChessGameState moves, CoordinatesSystem cs) {
		return IntStream.range(0, moves.size()).mapToObj(i -> cs.getAlgebraicNotation(moves.get(i).getFrom())).collect(Collectors.toSet());
	}
}
