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

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class TopologicalOrderTest extends TestBase {

	@Test
	public void testTopologicalSortUnconnected() {
		final long seed = 0x858cb81cf8e5b5c7L;
		topologicalSort(false, seed);
	}

	@Test
	public void testTopologicalSortConnected() {
		final long seed = 0xef5ef391b897c354L;
		topologicalSort(true, seed);
	}

	private static void topologicalSort(boolean connected, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16, 16), phase(128, 32, 64), phase(2, 1024, 2048));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(true)
					.selfEdges(false).cycles(false).connected(connected).build();

			IntIterator topolSort = new TopologicalOrderAlgoImpl().computeTopologicalSorting(g).verticesIterator();

			Set<Integer> seenVertices = new ObjectOpenHashSet<>(n);
			while (topolSort.hasNext()) {
				int u = topolSort.nextInt();
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
					assertFalse(seenVertices.contains(Integer.valueOf(v)));
				}
				seenVertices.add(Integer.valueOf(u));
			}
		});
	}

}
