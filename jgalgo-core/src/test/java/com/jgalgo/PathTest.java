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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;

public class PathTest extends TestBase {

	@Test
	public void testFindPath() {
		final long seed = 0x03afc698ec4c71ccL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		ShortestPathSingleSource validationAlgo = new ShortestPathSingleSourceDijkstra();
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(true).build();
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];
			int target = vs[rand.nextInt(vs.length)];

			Path actual = Path.findPath(g, source, target);
			Path expected = validationAlgo.computeCardinalityShortestPaths(g, source).getPath(target);
			if (expected == null) {
				assertNull(actual, "found non existing path");
			} else {
				assertNotNull(actual, "failed to found a path");
				assertEquals(expected.size(), actual.size(), "failed to find shortest path");
			}
		});
	}

}
