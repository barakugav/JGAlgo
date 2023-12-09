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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class TopologicalOrderTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0x858cb81cf8e5b5c7L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		TopologicalOrderAlgo algo = new TopologicalOrderAlgoImpl();
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(256);
		tester.addPhase().withArgs(32, 64).repeat(128);
		tester.addPhase().withArgs(1024, 2048).repeat(2);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randDag(n, m, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			TopologicalOrderAlgo.Result<Integer, Integer> res = algo.computeTopologicalSorting(g);

			Set<Integer> seenVertices = new ObjectOpenHashSet<>(n);
			for (Integer u : res.orderedVertices()) {
				for (EdgeIter<Integer, Integer> eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.next();
					Integer v = eit.target();
					assertFalse(seenVertices.contains(v));
				}
				seenVertices.add(u);
			}

			for (Integer v : g.vertices())
				assertEquals(v, res.orderedVertices().get(res.vertexOrderIndex(v)));

			assertEquals(res.orderedVertices(),
					g.vertices().stream().sorted(res.orderComparator()).collect(Collectors.toList()));
		});
	}

	@Test
	public void testNonDagGraph() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 3);
		g.addEdge(3, 1);
		g.addEdge(3, 4);

		TopologicalOrderAlgo algo = new TopologicalOrderAlgoImpl();
		assertThrows(IllegalArgumentException.class, () -> algo.computeTopologicalSorting(g));

	}

	@Test
	public void testDefaultImpl() {
		TopologicalOrderAlgo algo = TopologicalOrderAlgo.newInstance();
		assertEquals(TopologicalOrderAlgoImpl.class, algo.getClass());
	}

}
