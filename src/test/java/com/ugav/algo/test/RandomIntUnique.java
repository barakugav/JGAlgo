package com.ugav.algo.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class RandomIntUnique {

	private final Random rand;
	private final int minWeight;
	private final int maxWeight;
	private final Set<Integer> usedWeights;

	RandomIntUnique(int minWeight, int maxWeight, long seed) {
		rand = new Random(seed);
		this.minWeight = minWeight;
		this.maxWeight = maxWeight;
		usedWeights = new HashSet<>();
	}

	int next() {
		int w;
		do {
			w = rand.nextInt(minWeight, maxWeight);
		} while (usedWeights.contains(w));

		usedWeights.add(w);

		return w;
	}
}
