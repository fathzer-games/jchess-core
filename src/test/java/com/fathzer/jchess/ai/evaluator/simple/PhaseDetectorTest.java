package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.jchess.ai.evaluator.simple.Phase.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;

class PhaseDetectorTest {

	@Test
	void test() {
		// Queen + rook => middle game
		assertEquals(MIDDLE_GAME, getPhase("r2qk3/8/8/8/8/8/8/4K3 w q - 0 1"));
		// Queen + bishop vs two rooks => end game
		assertEquals(END_GAME, getPhase("3qk3/7b/8/8/8/8/8/R3K2R w - - 0 1"));
		// 2 Queens => end game
		assertEquals(END_GAME, getPhase("3qk3/8/8/8/8/7Q/8/4K3 w - - 0 1"));
	}
	
	private Phase getPhase(String fen) {
		final PhaseDetector pd = new PhaseDetector();
		final BoardExplorer explorer = FENUtils.from(fen).getExplorer();
		do {
			final Piece p = explorer.getPiece();
			if (p!=null) {
				pd.add(p);
			}
		} while (explorer.next());
		return pd.getPhase();
	}
}
