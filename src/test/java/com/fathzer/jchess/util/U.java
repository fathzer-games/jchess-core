package com.fathzer.jchess.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.standard.Coord;

import lombok.experimental.UtilityClass;

@UtilityClass
public class U {

	public static Set<String> to(ChessGameState moves) {
		return IntStream.range(0, moves.size()).mapToObj(i -> Coord.toString(moves.get(i).getTo())).collect(Collectors.toSet());
	}

	public static Set<String> from(ChessGameState moves) {
		return IntStream.range(0, moves.size()).mapToObj(i -> Coord.toString(moves.get(i).getFrom())).collect(Collectors.toSet());
	}
}
