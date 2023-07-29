package com.fathzer.jchess.ai;

import java.util.concurrent.atomic.AtomicLong;

class Stat {
		final AtomicLong evalCount = new AtomicLong();
		final AtomicLong moveGenerations = new AtomicLong();
		final AtomicLong generatedMoves = new AtomicLong();
		final AtomicLong movesPlayed = new AtomicLong();
//		long evalAgainCount = 0;
//		private Set<String> previous = new HashSet<>();
	}