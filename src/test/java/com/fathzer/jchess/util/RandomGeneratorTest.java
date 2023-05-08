package com.fathzer.jchess.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;
import java.util.function.LongSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class RandomGeneratorTest {
	// java.security.SecuredRandom implementation is secured but quite slow (>1000s to test on my computer)
	// java.util.Random is secured and fast (about 8s to test on my computer)
	private static final Random JAVA = new Random(0); 
	// All implementations based on Math.random() I've found are not secured.
	// Chesslib XorShiftRandom implementation is secured and blazing fast (about 3s to test on my computer)
	//private static final XorShiftRandom CHESS_LIB = new XorShiftRandom(); 
	
	@Test
	@EnabledIfSystemProperty(named = "rndGenTest", matches = "*")
	void test() {
		final int sampleSize = 2000;
		final int count = 300000000;
		final int[] distArray = buildDistribution(sampleSize, count, () -> JAVA.nextLong());
		final int average = Arrays.stream(distArray).sum()/ distArray.length;
		final int maxDeviation = average / 100;
		for (int j = 0; j < distArray.length; j++) {
			assertTrue(Math.abs(distArray[j]-average)<maxDeviation, j+": "+distArray[j]+" expected about "+average);
		}
	}

	private int[] buildDistribution(final int sampleSize, final int count, LongSupplier randomGenerator) {
		final int[] distArray = new int[sampleSize];
		for (int i = 0; i < count; i++) {
			final long random64 = randomGenerator.getAsLong();
			int index = (int)(random64%sampleSize/2)+sampleSize/2;
			if (index == sampleSize/2 && random64 < 0) {
				index = 0;
			}
			distArray[index]++;
		}
		return distArray;
	}
}
