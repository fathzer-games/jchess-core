package com.fathzer.jchess.chessutils;

import java.util.stream.IntStream;

import com.fathzer.chess.utils.adapters.BoardExplorer;
import com.fathzer.chess.utils.adapters.BoardExplorerBuilder;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

public interface JChessBoardExplorerBuilder extends BoardExplorerBuilder<Board<Move>> {

	@Override
	default BoardExplorer getExplorer(Board<Move> board) {
		return new JChessBoardExplorer(board);
	}

//	@Override
//	default IntStream getPieces(Board<Move> board) {
//		// TODO Auto-generated method stub
//		return BoardExplorerBuilder.super.getPieces(board);
//	}
}
