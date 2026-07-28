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
package com.jgalgo.gen;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CompleteGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		CompleteGraphGenerator<String, Integer> g = new CompleteGraphGenerator<>();
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = range(7).mapToObj(String::valueOf).collect(Collectors.toSet());
		g.vertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(4, existingIds -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("4", "5", "6", "7"), g.generate().vertices());

		g.vertices(5); /* default vertex builder */
		assertThrows(IllegalStateException.class, () -> g.generate());
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesIntGraph() {
		CompleteGraphGenerator<Integer, Integer> g = new CompleteGraphGenerator<>(IntGraphFactory.undirected());
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = range(7);
		g.vertices(range(7));
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(4, existingIds -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(4, 5, 6, 7), g.generate().vertices());

		g.vertices(5); /* default vertex builder */
		assertEquals(5, g.generate().vertices().size());

		assertThrows(IllegalArgumentException.class, () -> g.vertices(-3));
	}

	@Test
	public void edges() {
		foreachBoolConfig((intGraph, directed, selfEdges) -> {
			CompleteGraphGenerator<Integer, Integer> g0 =
					intGraph ? new CompleteGraphGenerator<>(IntGraphFactory.undirected())
							: new CompleteGraphGenerator<>();
			final int n = 7;
			g0.vertices(range(n));

			/* edges were not set yet */
			if (!intGraph) {
				assertThrows(IllegalStateException.class, () -> g0.generate());
			} else {
				/* int graph factory should have a default id builder */
				assertNotNull(g0.generate());
			}

			g0.edges(IdBuilderInt.defaultBuilder());
			g0.directed(directed);
			g0.selfEdges(selfEdges);
			Graph<Integer, Integer> g = g0.generate();

			assertEqualsBool(intGraph, g instanceof IntGraph);

			int expectedNumEdges = 0;
			expectedNumEdges += directed ? n * (n - 1) : n * (n - 1) / 2;
			expectedNumEdges += selfEdges ? n : 0;
			assertEquals(range(1, 1 + expectedNumEdges), g.edges());

			Set<Pair<Integer, Integer>> edges = new ObjectOpenHashSet<>();
			for (Integer e : g.edges()) {
				Integer u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!directed && u.intValue() > v.intValue()) {
					Integer tmp = u;
					u = v;
					v = tmp;
				}
				boolean dupEdge = !edges.add(Pair.of(u, v));
				assertFalse(dupEdge, "duplicate edge: (" + u + ", " + v + ")");
			}
			for (Integer u : range(n)) {
				for (Integer v : range(directed ? 0 : u.intValue(), n)) {
					if (!selfEdges && u.equals(v))
						continue;
					if (directed) {
						assertTrue(edges.contains(Pair.of(u, v)));
					} else {
						assertTrue(edges.contains(Pair.of(u, v)) || edges.contains(Pair.of(v, u)));
					}
				}
			}
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			CompleteGraphGenerator<Integer, Integer> g =
					intGraph ? new CompleteGraphGenerator<>(IntGraphFactory.undirected())
							: new CompleteGraphGenerator<>();
			g.vertices(IntList.of(1, 9, 3, 4, 5));
			g.edges(IdBuilderInt.defaultBuilder());

			/* check default */
			assertFalse(g.generate().isDirected());

			/* check directed */
			g.directed(true);
			assertTrue(g.generate().isDirected());

			/* check undirected */
			g.directed(false);
			assertFalse(g.generate().isDirected());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			CompleteGraphGenerator<Integer, Integer> g =
					intGraph ? new CompleteGraphGenerator<>(IntGraphFactory.undirected())
							: new CompleteGraphGenerator<>();
			g.vertices(IntList.of(1, 9, 3, 4, 5));
			g.edges(IdBuilderInt.defaultBuilder());

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new CompleteGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		CompleteGraphGenerator<Integer, Integer> g = new CompleteGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
