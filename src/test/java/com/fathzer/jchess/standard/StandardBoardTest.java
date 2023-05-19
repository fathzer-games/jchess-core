package com.fathzer.jchess.standard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.fathzer.jchess.Castling.*;
import static com.fathzer.games.Color.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Castling;
import com.fathzer.jchess.PieceWithPosition;
import com.fathzer.jchess.fen.FENParser;

class StandardBoardTest {

	@Test
	void test() {
		// King not in the right position
		testIllegal("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR",BLACK_KING_SIDE);
		testIllegal("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR",WHITE_KING_SIDE);
		testIllegal("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR",BLACK_QUEEN_SIDE);
		testIllegal("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR",WHITE_QUEEN_SIDE);

		// Rook not in the right position
		testIllegal("rnbqkbnr/pppppppp/8/8/8/R7/PPPPPPPP/1NBQKBNR",WHITE_QUEEN_SIDE);
		testIllegal("rnbqkbnr/pppppppp/8/8/8/7R/PPPPPPPP/RNBQKBN1",WHITE_KING_SIDE);
		testIllegal("rnbqkbn1/pppppppp/7r/8/8/8/PPPPPPPP/RNBQKBNR",BLACK_KING_SIDE);
		testIllegal("1nbqkbnr/pppppppp/r7/8/8/8/PPPPPPPP/RNBQKBNR",BLACK_QUEEN_SIDE);
	}

	private void testIllegal(String pieces, Castling castling) {
		final List<PieceWithPosition> withPos = FENParser.getPieces(pieces);
		final Set<Castling> castlings = Collections.singleton(castling);
		assertThrows(IllegalArgumentException.class, () -> new StandardBoard(withPos, WHITE, castlings, -1, 0, 1));
	}
}
