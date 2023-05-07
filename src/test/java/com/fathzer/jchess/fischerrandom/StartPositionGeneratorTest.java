package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.fen.FENParser;

class StartPositionGeneratorTest {

	@Test
	void test() {
		// Test 518 is standard board
		String fen518 = FENParser.to(new ChessBoard(StartPositionGenerator.INSTANCE.fromPositionNumber(518)));
		assertEquals(FENParser.NEW_STANDARD_GAME, fen518);
		
		// Test there is 960 different positions
		final Set<String> fens = IntStream.range(0, 960).mapToObj(i -> FENParser.to(new ChessBoard(StartPositionGenerator.INSTANCE.fromPositionNumber(i)))).collect(Collectors.toSet());
		assertEquals(960,fens.size());
	}

}
