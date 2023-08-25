package com.fathzer.jchess.ai;

import java.util.List;
import java.util.stream.Collectors;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.iterativedeepening.FirstBestMoveSelector;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggedSelector extends FirstBestMoveSelector<Move> {
	private final Board<Move> board;
	
	public LoggedSelector(Board<Move> board) {
		this.board = board;
	}

	@Override
	protected List<EvaluatedMove<Move>> filter(List<SearchResult<Move>> history, List<EvaluatedMove<Move>> bestMoves) {
		log.info("Filtering with the best moves history:");
		return super.filter(history, bestMoves);
	}

	@Override
	protected void log(int index, List<Move> cut, List<EvaluatedMove<Move>> result) {
		log.info(cut.stream().map(m->m.toString(board.getCoordinatesSystem())).collect(Collectors.joining(",")));
		log.info("  -> {}", EvaluatedMove.toString(result, m-> m.toString(board.getCoordinatesSystem())));
	}
}
