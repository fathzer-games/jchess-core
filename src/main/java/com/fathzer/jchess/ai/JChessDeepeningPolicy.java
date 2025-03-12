package com.fathzer.jchess.ai;

import java.util.Optional;

import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.SearchHistory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class JChessDeepeningPolicy extends DeepeningPolicy {
	
	protected JChessDeepeningPolicy(int maxDepth) {
		super(maxDepth);
	}
	
	@Override
	public int getNextDepth(int currentDepth) {
		int candidate = currentDepth < 5 ? currentDepth+2 : currentDepth+1;
		return candidate>this.getDepth() ? this.getDepth() : candidate;
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
	public <M> Optional<SearchResult<M>> mergeInterrupted(SearchHistory<M> history, SearchResult<M> interruptedSearch, int interruptionDepth) {
		if ((interruptionDepth - history.getLastDepth())%2==0) {
			//TODO Remove when quiesce will be implemented?
			// Do not merge results if depth are optimistic and pessimistic. 
			return super.mergeInterrupted(history, interruptedSearch, interruptionDepth);
		} else {
			return Optional.empty();
		}
	}
}