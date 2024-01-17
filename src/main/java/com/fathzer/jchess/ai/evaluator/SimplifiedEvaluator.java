package com.fathzer.jchess.ai.evaluator;

import com.fathzer.chess.utils.adapters.BoardExplorer;
import com.fathzer.chess.utils.adapters.MoveData;
import com.fathzer.chess.utils.evaluators.simplified.AbstractIncrementalSimplifiedEvaluator;
import com.fathzer.chess.utils.evaluators.simplified.IncrementalState;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.chessutils.JChessBoardExplorer;
import com.fathzer.jchess.chessutils.JChessMoveData;

public class SimplifiedEvaluator extends AbstractIncrementalSimplifiedEvaluator<Move, Board<Move>> {

	public SimplifiedEvaluator() {
		super();
	}

	private SimplifiedEvaluator(IncrementalState state) {
		super(state);
	}

	@Override
	public MoveData<Move, Board<Move>> get() {
		return new JChessMoveData();
	}

	@Override
	public BoardExplorer getExplorer(Board<Move> board) {
		return new JChessBoardExplorer(board);
	}

	@Override
	protected AbstractIncrementalSimplifiedEvaluator<Move, Board<Move>> fork(IncrementalState state) {
		return new SimplifiedEvaluator(state);
	}
}
