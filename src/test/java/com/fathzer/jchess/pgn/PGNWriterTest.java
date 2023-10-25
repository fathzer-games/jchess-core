package com.fathzer.jchess.pgn;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.GameBuilders;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.SimpleMove;

class PGNWriterTest {

	@Test
	void test() {
		final Board<Move> board = GameBuilders.STANDARD.newGame();
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final GameHistory history = new GameHistory(board);
		history.add(new SimpleMove(cs, "e2", "e4"));
		history.add(new SimpleMove(cs, "e7", "e5"));
		history.add(new SimpleMove(cs, "d1", "h5"));
		history.add(new SimpleMove(cs, "d7", "d6"));
		history.add(new SimpleMove(cs, "f1", "c4"));
		history.add(new SimpleMove(cs, "b8", "c6"));
		history.add(new SimpleMove(cs, "h5", "f7"));
		
		final PGNWriter writer = new PGNWriter();
		final PGNHeaders headers = new PGNHeaders.Builder()
				.setWhiteName("M. White").setBlackName("M. Black")
				.setEvent("A competition").setRound(5L).setSite("there").setDate(LocalDate.of(2020, 1, 8))
				.build();
		final List<String> pgn = writer.getPGN(headers, history);
		pgn.forEach(System.out::println);
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
		final Board<Move> board = GameBuilders.STANDARD.newGame();
		final CoordinatesSystem cs = board.getCoordinatesSystem();
		final GameHistory history = new GameHistory(board);
		history.add(new SimpleMove(cs, "c2", "c4"));
		history.add(new SimpleMove(cs, "g8", "f6"));
		history.add(new SimpleMove(cs, "c4", "c5"));
		history.add(new SimpleMove(cs, "b7", "b5"));
		history.add(new SimpleMove(cs, "c5", "b6"));
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
}
