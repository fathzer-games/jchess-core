package com.fathzer.jchess.ai.evaluator.simple;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.IntFunction;

import org.junit.jupiter.api.Test;

import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.fen.FENUtils;

class SimpleEvaluatorTest {

	@Test
	void test() {
		Board<Move> board = FENUtils.from(FENUtils.NEW_STANDARD_GAME);
		assertEquals(0, SimpleEvaluator.getPoints(board));
		board = FENUtils.from("3k4/8/8/3Pp3/8/8/8/4K3 w - - 0 1");
		assertEquals(5, SimpleEvaluator.getPoints(board));
		
		testVerticalSymetry(PieceKind.PAWN);
		testVerticalSymetry(PieceKind.KNIGHT);
		testVerticalSymetry(PieceKind.BISHOP);
		testVerticalSymetry(PieceKind.ROOK);
		
		// Pawel's suggestion with white queen at b3
		board = FENUtils.from("rnbqkbnr/pp1ppppp/8/8/8/1Q6/PP1PPPPP/RNB1KBNR w KQkq - 0 1");
		assertEquals(10, SimpleEvaluator.getPoints(board));
		// Pawel's suggestion with both queen's at b3 and c7
		board = FENUtils.from("rnb1kbnr/ppqppppp/8/8/8/1Q6/PP1PPPPP/RNB1KBNR w KQkq - 0 1");
		assertEquals(0, SimpleEvaluator.getPoints(board));
		
	}
	
	private void testVerticalSymetry(PieceKind kind) {
		testVerticalSymetry(kind.name(), i -> SimpleEvaluator.getPositionValue(kind, i));
}

	private void testVerticalSymetry(String wording, IntFunction<Integer> valueGetter) {
		for (int row=0;row<8;row++) {
			final int startOFrowIndex = row*8;
			for (int col=0;col<4;col++) {
				final int index=startOFrowIndex + col;
				final int sym = startOFrowIndex + 7 - col;
				assertEquals (valueGetter.apply(index), valueGetter.apply(sym), "No symetry for "+wording+" on indexes "+index+" & "+sym);
			}
		}
	}
}
