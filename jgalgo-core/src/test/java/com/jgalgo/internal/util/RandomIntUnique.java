/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.internal.util;

import java.util.Random;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class RandomIntUnique {

	private final Random rand;
	private final int min;
	private final int max;
	private final IntSet usedVals;

	public RandomIntUnique(int min, int max, long seed) {
		if (min >= max)
			throw new IllegalArgumentException();

		rand = new Random(seed);
		this.min = min;
		this.max = max;
		usedVals = new IntOpenHashSet();
	}

	public int next() {
		if (usedVals.size() > 0.9 * (max - min))
			throw new IllegalStateException();

		int x;
		do {
			x = min + rand.nextInt(max - min);
		} while (usedVals.contains(x));

		usedVals.add(x);

		return x;
	}
}
