package com.fathzer.jchess.chessutils;

import com.fathzer.chess.utils.adapters.BoardExplorer;
import com.fathzer.chess.utils.adapters.BoardExplorerBuilder;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

public interface JChessBoardExplorerBuilder extends BoardExplorerBuilder<Board<Move>> {

	@Override
	default BoardExplorer getExplorer(Board<Move> board) {
		return new JChessBoardExplorer(board);
	}
}
