package com.fathzer.jchess.ai;

import java.util.List;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class JChessDeepeningPolicy extends DeepeningPolicy {
	
	protected JChessDeepeningPolicy(int maxDepth) {
		super(maxDepth);
	}
	
	@Override
	public int getNextDepth(int currentDepth) {
		return currentDepth < 5 ? currentDepth+2 : currentDepth+1;
	}

	@Override
	public boolean isEnoughTimeToDeepen(int depth) {
		boolean enoughTime = depth<5 || getSpent()<getMaxTime()/3;
		if (!enoughTime) {
			log.info("{}ms seems not enough to deepen the search",getMaxTime()-getSpent());
		}
		return enoughTime;
	}

	@Override
	public <M> void mergeInterrupted(SearchResult<M> bestMoves, int bestMovesDepth, List<EvaluatedMove<M>> partialList, int interruptionDepth) {
		if ((interruptionDepth - bestMovesDepth)%2==0) {
			//TODO Remove when quiesce will be implemented?
			// Do not merge results if depth are optimistic and pessimistic. 
			super.mergeInterrupted(bestMoves, bestMovesDepth, partialList, interruptionDepth);
		}
	}
}