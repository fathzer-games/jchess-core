package com.fathzer.jchess.ai.evaluator;

import com.fathzer.chess.utils.adapters.BoardExplorer;
import com.fathzer.chess.utils.evaluators.AbstractNaiveEvaluator;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.chessutils.BasicMoveDecoder;
import com.fathzer.jchess.chessutils.JChessBoardExplorer;

public class NaiveEvaluator extends AbstractNaiveEvaluator<Move, Board<Move>> {
	public NaiveEvaluator() {
		super();
	}
	
	protected NaiveEvaluator(int score) {
		super(score);
	}


	@Override
	protected AbstractNaiveEvaluator<Move, Board<Move>> fork(int score) {
		return new NaiveEvaluator(score);
	}

	@Override
	public BoardExplorer getExplorer(Board<Move> board) {
		return new JChessBoardExplorer(board);
	}

	@Override
	protected int getCapturedType(Board<Move> board, Move move) {
		return BasicMoveDecoder.getCapturedType(board, move);
	}

	@Override
	protected int getPromotionType(Board<Move> board, Move move) {
		return BasicMoveDecoder.getPromotionType(board, move);
	}
}
