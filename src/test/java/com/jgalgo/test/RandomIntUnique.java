package com.jgalgo.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class RandomIntUnique {

	private final Random rand;
	private final int min;
	private final int max;
	private final Set<Integer> usedVals;

	RandomIntUnique(int min, int max, long seed) {
		if (min >= max)
			throw new IllegalArgumentException();

		rand = new Random(seed);
		this.min = min;
		this.max = max;
		usedVals = new HashSet<>();
	}

	int next() {
		if (usedVals.size() > 0.9 * (max - min))
			throw new IllegalStateException();

		Integer x;
		do {
			x = Integer.valueOf(min + rand.nextInt(max - min));
		} while (usedVals.contains(x));

		usedVals.add(x);

		return x.intValue();
	}
}
