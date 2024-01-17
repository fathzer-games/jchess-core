package com.fathzer.jchess.chessutils;

import com.fathzer.chess.utils.adapters.MoveData;
import com.fathzer.chess.utils.test.AbstractMoveDataTest;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.fen.FENUtils;
import com.fathzer.jchess.generic.BasicMove;

class JChessMoveDataTest extends AbstractMoveDataTest<Move, Board<Move>> {

	@Override
	protected MoveData<Move, Board<Move>> buildMoveData() {
		return new JChessMoveData();
	}

	@Override
	protected Board<Move> toBoard(String fen) {
		return FENUtils.from(fen);
	}

	@Override
	protected Move toMove(int from, int to, int promotionType, Board<Move> board) {
		from = toNative(from, board);
		to = toNative(to, board);
		Piece promotion; 
		if (promotionType == 0) {
			promotion = null;
		} else {
			int index = promotionType*2;
			if (board.isWhiteToMove()) {
				index--;
			}
			promotion = Piece.ALL.get(index);
		}
		return new BasicMove(from, to, promotion);
	}
	
	private int toNative(int index, Board<Move> board) {
		return board.getCoordinatesSystem().getIndex(index/8, index%8);
	}
}
