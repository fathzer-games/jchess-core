package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.GameBuilders;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.MoveBuilder;
import com.fathzer.jchess.fen.FENUtils;

class PGNWriterTest implements MoveBuilder {

	@Test
	void test() {
		final Board<Move> board = GameBuilders.STANDARD.get();
		final GameHistory history = new GameHistory(board);
		history.add(move(board, "e2", "e4"));
		history.add(move(board, "e7", "e5"));
		history.add(move(board, "d1", "h5"));
		history.add(move(board, "d7", "d6"));
		history.add(move(board, "f1", "c4"));
		history.add(move(board, "b8", "c6"));
		history.add(move(board, "h5", "f7"));
		
		final PGNWriter writer = new PGNWriter();
		final PGNHeaders headers = new PGNHeaders.Builder()
				.setWhiteName("M. White").setBlackName("M. Black")
				.setEvent("A competition").setRound(5L).setSite("there").setDate(LocalDate.of(2020, 1, 8))
				.build();
		final List<String> pgn = writer.getPGN(headers, history);
		assertEquals("[Event \"A competition\"]", pgn.get(0));
		assertEquals("[Site \"there\"]", pgn.get(1));
		assertEquals("[Date \"2020.01.08\"]", pgn.get(2));
		assertEquals("[Round \"5\"]", pgn.get(3));
		assertEquals("[White \"M. White\"]", pgn.get(4));
		assertEquals("[Black \"M. Black\"]", pgn.get(5));
		assertEquals("[Result \"1-0\"]", pgn.get(6));
		assertTrue(pgn.get(7).isEmpty());
		assertEquals("4. Qxf7#",pgn.get(11));
	}
	
	@Test
	void bug20231025() {
		final Board<Move> board = GameBuilders.STANDARD.get();
		final GameHistory history = new GameHistory(board);
		history.add(move(board, "c2", "c4"));
		history.add(move(board, "g8", "f6"));
		history.add(move(board, "c4", "c5"));
		history.add(move(board, "b7", "b5"));
		history.add(move(board, "c5", "b6"));
		final PGNWriter writer = new PGNWriter();
		final List<String> pgn = writer.getPGN(new PGNHeaders.Builder().build(), history);
		
		//skip header
		while (!pgn.get(0).isEmpty()) {
			pgn.remove(0);
		}
		pgn.remove(0);
		assertEquals("1. c4 Nf6", pgn.get(0));
		assertEquals("2. c5 b5", pgn.get(1));
		assertEquals("3. cxb6", pgn.get(2));
	}
	
	@Test
	void nonStandardStartFENTest() {
		final var board = FENUtils.from("1r2k1r1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Q - 4 11");
		final var history = new GameHistory(board);
		final var writer = new PGNWriter();
		final List<String> pgn = writer.getPGN(new PGNHeaders.Builder().build(), history);
		final var setUpIndex = pgn.indexOf("[SetUp \"1\"]");
		assertTrue(setUpIndex>=0);
		final var fenIndex = pgn.indexOf("[FEN \"1r2k1r1/ppp1pp2/3p2pp/5bn1/P7/2N2B2/1PPPPP2/RR2K3 w Q - 4 11\"]");
		assertTrue(fenIndex>setUpIndex);
	}
}
