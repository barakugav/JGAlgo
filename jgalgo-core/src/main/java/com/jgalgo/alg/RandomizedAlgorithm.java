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
package com.jgalgo.alg;

/**
 * Randomized algorithm interface.
 *
 * <p>
 * A randomized algorithm is an algorithm that employs a degree of randomness as part of its logic or procedure. The
 * algorithm typically uses uniformly random bits as an auxiliary input to guide its behavior, in the hope of achieving
 * good performance in the "average case" over all possible choices of random determined by the random bits; thus either
 * the running time, or the output (or both) are random variables. In many cases randomized algorithm are much simpler
 * than their deterministic counterparts, and can achieve same or better performance.
 *
 * <p>
 * One drawback of randomized algorithms is that they are more complicated to debug and analyze. In particular, their
 * output is not deterministic, and may vary each time the algorithm is run. Therefore, randomized algorithms can be
 * forced to use the same <i>seed</i> for the random number generator, in order to get the same output each time the
 * algorithm is run. The seed can be set using the {@link #setSeed(long)} method.
 *
 * <p>
 * One can check if a JGAlgo algorithm is indeed a randomized algorithm by checking if it implements this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Randomized_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public interface RandomizedAlgorithm {

	/**
	 * Sets the seed for the random number generator.
	 *
	 * <p>
	 * The algorithm will use the same seed for the random number generator, in order to perform deterministically. Note
	 * that if methods of the algorithm are called multiple times, the seed should be set before each call.
	 *
	 * @param seed the seed for the random number generator.
	 */
	void setSeed(long seed);

}
