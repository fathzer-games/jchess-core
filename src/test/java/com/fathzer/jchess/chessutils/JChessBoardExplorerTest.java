package com.fathzer.jchess.chessutils;

import com.fathzer.chess.utils.adapters.BoardExplorerBuilder;
import com.fathzer.chess.utils.test.AbstractBoardExplorerBuilderTest;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

class JChessBoardExplorerTest extends AbstractBoardExplorerBuilderTest<Board<Move>> {
	@Override
	protected BoardExplorerBuilder<Board<Move>> getBuilder() {
		return new JChessBoardExplorerBuilder() {};
	}
	protected Board<Move> toBoard(String fen) {
		return FENUtils.from(fen);
	}
}
