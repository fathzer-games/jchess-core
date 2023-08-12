package com.fathzer.jchess.ai;

import java.util.Collections;
import java.util.List;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class JChessDeepeningPolicy implements DeepeningPolicy {
	private final long maxTime;
	private final long start;
	
	protected JChessDeepeningPolicy(long maxTimeMs) {
		this.maxTime = maxTimeMs;
		this.start = System.currentTimeMillis();
	}
	
	@Override
	public int getNextDepth(int currentDepth) {
		return currentDepth < 5 ? currentDepth+2 : currentDepth+1;
	}

	@Override
	public <M> List<M> getMovesToDeepen(int depth, List<EvaluatedMove<M>> evaluations, List<EvaluatedMove<M>> ended) {
		long spent = System.currentTimeMillis()-start;
		if (depth<5 || spent<maxTime/2) {
			return DeepeningPolicy.super.getMovesToDeepen(depth, evaluations, ended);
		} else {
			log.info("{}ms seems not enough to deepen the search",spent);
			return Collections.emptyList();
		}
	}

	@Override
	public <M> void mergeInterrupted(SearchResult<M> bestMoves, int bestMovesDepth, List<EvaluatedMove<M>> partialList, int interruptionDepth) {
		if ((interruptionDepth - bestMovesDepth)%2==0) {
			//TODO Remove when quiesce will be implemented
			// Do not merge results if depth are optimistic and pessimistic. 
			DeepeningPolicy.super.mergeInterrupted(bestMoves, bestMovesDepth, partialList, interruptionDepth);
		}
	}
}