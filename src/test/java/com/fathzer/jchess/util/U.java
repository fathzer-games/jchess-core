package com.fathzer.jchess.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;

import lombok.experimental.UtilityClass;

@UtilityClass
public class U {
	public static Set<String> to(List<Move> moves, CoordinatesSystem cs) {
		return IntStream.range(0, moves.size()).mapToObj(i -> cs.getAlgebraicNotation(moves.get(i).getTo())).collect(Collectors.toSet());
	}

	public static Set<String> from(List<Move> moves, CoordinatesSystem cs) {
		return IntStream.range(0, moves.size()).mapToObj(i -> cs.getAlgebraicNotation(moves.get(i).getFrom())).collect(Collectors.toSet());
	}
}
