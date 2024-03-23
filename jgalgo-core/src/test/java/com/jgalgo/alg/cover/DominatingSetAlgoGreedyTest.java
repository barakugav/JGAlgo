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
package com.jgalgo.alg.cover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.EdgeDirection;
import com.jgalgo.gen.EmptyGraphGenerator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class DominatingSetAlgoGreedyTest extends TestBase {

	@Test
	public void testRandGraphsDirectedInEdges() {
		final long seed = 0xc43010845ce88b92L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DominatingSetAlgoGreedy algo = new DominatingSetAlgoGreedy();
		algo.setSeed(seedGen.nextSeed());
		DominatingSetAlgosTest.testRandGraphs(algo, true, EdgeDirection.In, seedGen.nextSeed());
	}

	@Test
	public void testRandGraphsDirectedOutEdges() {
		final long seed = 0xb0d3d52a1884afb7L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DominatingSetAlgoGreedy algo = new DominatingSetAlgoGreedy();
		algo.setSeed(seedGen.nextSeed());
		DominatingSetAlgosTest.testRandGraphs(algo, true, EdgeDirection.Out, seedGen.nextSeed());
	}

	@Test
	public void testRandGraphsDirectedAllEdges() {
		final long seed = 0x82b30d5d9b44570bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DominatingSetAlgoGreedy algo = new DominatingSetAlgoGreedy();
		algo.setSeed(seedGen.nextSeed());
		DominatingSetAlgosTest.testRandGraphs(algo, true, EdgeDirection.All, seedGen.nextSeed());
	}

	@Test
	public void testRandGraphsUndirected() {
		final long seed = 0xe6a74fe1b5d909c7L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		DominatingSetAlgoGreedy algo = new DominatingSetAlgoGreedy();
		algo.setSeed(seedGen.nextSeed());
		DominatingSetAlgosTest.testRandGraphs(algo, false, null, seedGen.nextSeed());
	}

	@Test
	public void testRandEmptyGraph() {
		DominatingSetAlgoGreedy algo = new DominatingSetAlgoGreedy();
		Graph<Integer, Integer> g = new EmptyGraphGenerator<>(IntGraphFactory.undirected()).generate();
		Set<Integer> dominatingSet = algo.computeMinimumDominationSet(g, null);
		assertEquals(Set.of(), dominatingSet);
	}

}
