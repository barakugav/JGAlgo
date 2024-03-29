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

package com.jgalgo.alg.match;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.gen.CompleteGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.TestBase;

public class MatchingWeightedGabow1990Test extends TestBase {

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0x2ab1588bd0eb62b2L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingWeightedGabow1990(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0xbffb50ae18bf664cL;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MatchingWeightedGabow1990(), seed);
	}

	// @Test
	// public void testRandBipartiteGraphsWeightedPerfect() {
	// final long seed = 0xf5c0a210842d9f5eL;
	// MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(new MatchingWeightedGabow1990(), seed);
	// }

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		Graph<Integer, Integer> g = new CompleteGraphGenerator<Integer, Integer>()
				.vertices(range(10))
				.edges(IdBuilderInt.defaultBuilder())
				.generate();
		WeightFunction<Integer> w = e -> 5;

		MatchingAlgo algo = new MatchingWeightedGabow1990();
		assertThrows(UnsupportedOperationException.class, () -> algo.computeMaximumPerfectMatching(g, w));
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x67ead1b9c6600229L;
		MatchingUnweightedTestUtils.randGraphs(new MatchingWeightedGabow1990(), seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0x33a1793a0388c73bL;
		MatchingWeightedTestUtils.randGraphsWeighted(new MatchingWeightedGabow1990(), seed);
	}

	// @Test
	// public void testRandGraphsWeightedPerfect() {
	// final long seed = 0x625606329a1eb13cL;
	// MatchingWeightedTestUtils.randGraphsWeightedPerfect(new MatchingWeightedGabow1990(), seed);
	// }

}
