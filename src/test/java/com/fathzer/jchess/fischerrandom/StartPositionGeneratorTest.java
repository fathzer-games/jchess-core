package com.fathzer.jchess.fischerrandom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.chess960.Chess960Board;
import com.fathzer.jchess.chess960.StartPositionGenerator;
import com.fathzer.jchess.fen.FENUtils;

class StartPositionGeneratorTest {

	@Test
	void test() {
		// Test 518 is standard board
		String fen518 = FENUtils.to(new Chess960Board(StartPositionGenerator.INSTANCE.fromPositionNumber(518)));
		assertEquals(FENUtils.NEW_STANDARD_GAME, fen518);
		
		// Test there is 960 different positions
		final Set<String> fens = IntStream.range(0, 960).mapToObj(i -> FENUtils.to(new Chess960Board(StartPositionGenerator.INSTANCE.fromPositionNumber(i)))).collect(Collectors.toSet());
		assertEquals(960,fens.size());
	}

}
