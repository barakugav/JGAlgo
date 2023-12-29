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
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class CompleteBipartiteGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		CompleteBipartiteGraphGenerator<String, Integer> g = new CompleteBipartiteGraphGenerator<>();
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> leftVertices = range(7).mapToObj(String::valueOf).collect(toSet());
		Set<String> rightVertices = range(7, 14).mapToObj(String::valueOf).collect(toSet());
		Set<String> vertices = range(14).mapToObj(String::valueOf).collect(toSet());
		g.vertices(leftVertices, rightVertices);
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(2, 2, existingIds -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("4", "5", "6", "7"), g.generate().vertices());

		g.vertices(5, 4); /* default vertex builder */
		assertThrows(IllegalStateException.class, () -> g.generate());
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesIntGraph() {
		CompleteBipartiteGraphGenerator<Integer, Integer> g =
				new CompleteBipartiteGraphGenerator<>(IntGraphFactory.undirected());
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(range(7), range(7, 14));
		assertEquals(range(14), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(range(14), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(1, 3, existingIds -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(4, 5, 6, 7), g.generate().vertices());

		g.vertices(5, 4); /* default vertex builder */
		assertEquals(9, g.generate().vertices().size());

		assertThrows(IllegalArgumentException.class, () -> g.vertices(-3, 3));
		assertThrows(IllegalArgumentException.class, () -> g.vertices(3, -3));
	}

	@Test
	public void edges() {
		foreachBoolConfig(intGraph -> {
			final int Undirected = 0;
			final int DirectedAll = 1;
			final int DirectedLeftToRight = 2;
			final int DirectedRightToLeft = 3;
			for (int direction : IntList.of(Undirected, DirectedAll, DirectedLeftToRight, DirectedRightToLeft)) {
				CompleteBipartiteGraphGenerator<Integer, Integer> g0 =
						intGraph ? new CompleteBipartiteGraphGenerator<>(IntGraphFactory.undirected())
								: new CompleteBipartiteGraphGenerator<>();
				final int n1 = 3, n2 = 4;
				g0.vertices(range(n1), range(n1, n1 + n2));

				/* edges were not set yet */
				if (!intGraph) {
					assertThrows(IllegalStateException.class, () -> g0.generate());
				} else {
					/* int graph factory should have a default id builder */
					assertNotNull(g0.generate());
				}

				g0.edges(IdBuilderInt.defaultBuilder());

				if (direction == Undirected) {
					g0.undirected();
				} else if (direction == DirectedAll) {
					g0.directedAll();
				} else if (direction == DirectedLeftToRight) {
					g0.directedLeftToRight();
				} else if (direction == DirectedRightToLeft) {
					g0.directedRightToLeft();
				} else {
					throw new AssertionError();
				}

				Graph<Integer, Integer> g = g0.generate();

				assertEqualsBool(intGraph, g instanceof IntGraph);

				int expectedNumberOfEdges = (n1 * n2) * (direction == DirectedAll ? 2 : 1);
				assertEquals(range(1, 1 + expectedNumberOfEdges), g.edges());

				Set<Pair<Integer, Integer>> edges = new ObjectOpenHashSet<>();
				for (Integer e : g.edges()) {
					Integer u = g.edgeSource(e), v = g.edgeTarget(e);
					if (direction == Undirected && u.intValue() > v.intValue()) {
						Integer tmp = u;
						u = v;
						v = tmp;
					}
					boolean dupEdge = !edges.add(Pair.of(u, v));
					assertFalse(dupEdge, "duplicate edge: (" + u + ", " + v + ")");
				}
				for (Integer u : range(n1)) {
					for (Integer v : range(n1, n2)) {
						if (direction == Undirected) {
							assertTrue(edges.contains(Pair.of(u, v)) || edges.contains(Pair.of(v, u)));

						} else if (direction == DirectedAll) {
							assertTrue(edges.contains(Pair.of(u, v)));
							assertTrue(edges.contains(Pair.of(v, u)));

						} else if (direction == DirectedLeftToRight) {
							assertTrue(edges.contains(Pair.of(u, v)));

						} else if (direction == DirectedRightToLeft) {
							assertTrue(edges.contains(Pair.of(v, u)));

						} else {
							throw new AssertionError();
						}
					}
				}
			}
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			CompleteBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? new CompleteBipartiteGraphGenerator<>(IntGraphFactory.undirected())
							: new CompleteBipartiteGraphGenerator<>();
			g.vertices(IntList.of(1, 9, 3, 4, 5), IntList.of(2, 22, 38));
			g.edges(IdBuilderInt.defaultBuilder());

			/* check default */
			Graph<Integer, Integer> g1 = g.generate();
			assertFalse(g1.isDirected());
			assertTrue(BipartiteGraphs.isBipartite(g1));
			Optional<VertexBiPartition<Integer, Integer>> g1Partition0 = BipartiteGraphs.getExistingPartition(g1);
			assertTrue(g1Partition0.isPresent());
			VertexBiPartition<Integer, Integer> g1Partition = g1Partition0.get();
			for (Integer e : g1.edges()) {
				boolean sourceIsLeft = g1Partition.leftVertices().contains(g1.edgeSource(e));
				boolean targetIsLeft = g1Partition.leftVertices().contains(g1.edgeTarget(e));
				assertNotEqualsBool(sourceIsLeft, targetIsLeft);
			}

			/* check directed all */
			g.directedAll();
			Graph<Integer, Integer> g2 = g.generate();
			assertTrue(g2.isDirected());
			assertTrue(BipartiteGraphs.isBipartite(g2));
			Optional<VertexBiPartition<Integer, Integer>> g2Partition0 = BipartiteGraphs.getExistingPartition(g2);
			assertTrue(g2Partition0.isPresent());
			VertexBiPartition<Integer, Integer> g2Partition = g2Partition0.get();
			for (Integer e : g2.edges()) {
				boolean sourceIsLeft = g2Partition.leftVertices().contains(g2.edgeSource(e));
				boolean targetIsLeft = g2Partition.leftVertices().contains(g2.edgeTarget(e));
				assertNotEqualsBool(sourceIsLeft, targetIsLeft);
			}

			/* check undirected */
			g.undirected();
			Graph<Integer, Integer> g3 = g.generate();
			assertFalse(g3.isDirected());
			assertTrue(BipartiteGraphs.isBipartite(g3));
			Optional<VertexBiPartition<Integer, Integer>> g3Partition0 = BipartiteGraphs.getExistingPartition(g3);
			assertTrue(g3Partition0.isPresent());
			VertexBiPartition<Integer, Integer> g3Partition = g3Partition0.get();
			for (Integer e : g3.edges()) {
				boolean sourceIsLeft = g3Partition.leftVertices().contains(g3.edgeSource(e));
				boolean targetIsLeft = g3Partition.leftVertices().contains(g3.edgeTarget(e));
				assertNotEqualsBool(sourceIsLeft, targetIsLeft);
			}

			/* check directed with edges left to right */
			g.directedLeftToRight();
			Graph<Integer, Integer> g4 = g.generate();
			assertTrue(g4.isDirected());
			assertTrue(BipartiteGraphs.isBipartite(g4));
			Optional<VertexBiPartition<Integer, Integer>> g4Partition0 = BipartiteGraphs.getExistingPartition(g4);
			assertTrue(g4Partition0.isPresent());
			VertexBiPartition<Integer, Integer> g4Partition = g4Partition0.get();
			for (Integer e : g4.edges()) {
				assertTrue(g4Partition.leftVertices().contains(g4.edgeSource(e)));
				assertTrue(g4Partition.rightVertices().contains(g4.edgeTarget(e)));
			}

			/* check directed with edges right to left */
			g.directedRightToLeft();
			Graph<Integer, Integer> g5 = g.generate();
			assertTrue(g5.isDirected());
			assertTrue(BipartiteGraphs.isBipartite(g5));
			Optional<VertexBiPartition<Integer, Integer>> g5Partition0 = BipartiteGraphs.getExistingPartition(g5);
			assertTrue(g5Partition0.isPresent());
			VertexBiPartition<Integer, Integer> g5Partition = g5Partition0.get();
			for (Integer e : g5.edges()) {
				assertTrue(g5Partition.rightVertices().contains(g5.edgeSource(e)));
				assertTrue(g5Partition.leftVertices().contains(g5.edgeTarget(e)));
			}
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new CompleteBipartiteGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		CompleteBipartiteGraphGenerator<Integer, Integer> g = new CompleteBipartiteGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
