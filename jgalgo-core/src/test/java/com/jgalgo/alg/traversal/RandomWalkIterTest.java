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
package com.jgalgo.alg.traversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestBase;

@SuppressWarnings("boxing")
public class RandomWalkIterTest extends TestBase {

	@Test
	public void smallGraph() {
		final Random rand = new Random(0x120f10ed1eca3174L);
		foreachBoolConfig((directed, weighted, intGraph, indexGraph) -> {
			Graph<Integer, Integer> g = graph(directed, intGraph, indexGraph);
			g.addVertex(0);
			g.addVertex(1);
			g.addEdge(0, 1, 0);
			g.addEdge(0, 1, 1);
			if (directed) {
				g.addEdge(1, 0, 2);
				g.addEdge(1, 0, 3);
			}
			WeightFunction<Integer> weights = weighted ? (e -> (e.intValue() % 2 == 0 ? 2 : 1)) : null;

			final int iterations = 10000;

			WeightsInt<Integer> visitedCount = g.addEdgesWeights("visited-count", int.class);
			int lastVertex = 0;
			RandomWalkIter<Integer, Integer> iter = RandomWalkIter.newInstance(g, lastVertex, weights);
			iter.setSeed(rand.nextLong());
			for (int iteration = 0; iteration < iterations; iteration++) {
				assertTrue(iter.hasNext());
				int v = iter.next();
				int e = iter.lastEdge();
				if (directed) {
					assertEquals(lastVertex, g.edgeSource(e));
					assertEquals(v, g.edgeTarget(e));
				} else {
					assertEquals(lastVertex, g.edgeEndpoint(e, v));
				}
				visitedCount.set(e, visitedCount.get(e) + 1);
				lastVertex = v;
			}

			for (int e : g.edges()) {
				int expectedCount;
				if (weighted) {
					expectedCount = (iterations / g.edges().size());
					if (e % 2 == 0) {
						expectedCount = expectedCount * 2 / 3 * 2;
					} else {
						expectedCount = expectedCount * 2 / 3 * 1;
					}
				} else {
					expectedCount = iterations / g.edges().size();
				}
				assertTrue(Math.abs(expectedCount - visitedCount.get(e)) < 400);
			}
		});
	}

	@Test
	public void deadEnd() {
		/* unweighted */
		foreachBoolConfig((directed, intGraph, indexGraph) -> {
			Graph<Integer, Integer> g = graph(directed, intGraph, indexGraph);
			g.addVertex(0);
			g.addVertex(1);
			if (directed) {
				g.addEdge(0, 1, 0);
			} else {
				g.addEdge(1, 1, 0);
			}

			RandomWalkIter<Integer, Integer> iter = RandomWalkIter.newInstance(g, 0);
			if (directed) {
				assertTrue(iter.hasNext());
				iter.next();
			}
			assertFalse(iter.hasNext());
			assertThrows(NoSuchElementException.class, () -> iter.next());
		});

		/* weighted */
		foreachBoolConfig((directed, intGraph, indexGraph) -> {
			Graph<Integer, Integer> g = graph(directed, intGraph, indexGraph);
			g.addVertex(0);
			g.addVertex(1);
			g.addEdge(0, 1, 0);

			RandomWalkIter<Integer, Integer> iter = RandomWalkIter.newInstance(g, 0, e -> 0);
			assertFalse(iter.hasNext());
			assertThrows(NoSuchElementException.class, () -> iter.next());
		});
	}

	@Test
	public void negativeWeight() {
		foreachBoolConfig((directed, intGraph, indexGraph) -> {
			Graph<Integer, Integer> g = graph(directed, intGraph, indexGraph);
			g.addVertex(0);
			g.addVertex(1);
			g.addEdge(0, 1, 0);
			assertThrows(IllegalArgumentException.class, () -> RandomWalkIter.newInstance(g, 0, e -> -1));
		});
	}

	private static Graph<Integer, Integer> graph(boolean directed, boolean intGraph, boolean indexGraph) {
		GraphFactory<Integer, Integer> factory =
				intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
		Graph<Integer, Integer> g = factory.allowSelfEdges().allowParallelEdges().newGraph();
		return indexGraph ? g.indexGraph() : g;
	}

}
