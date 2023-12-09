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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GnmBipartiteGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		final long seed = 0xc01d2657d68af779L;
		GnmBipartiteGraphGenerator<String, Integer> g = GnmBipartiteGraphGenerator.newInstance();
		g.setSeed(seed);
		g.setEdges(5, new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of("a", "b"), Set.of("c", "d"));
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(2, 2, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		final long seed = 0x608e465602fdc995L;
		GnmBipartiteGraphGenerator<Integer, Integer> g = GnmBipartiteGraphGenerator.newIntInstance();
		g.setSeed(seed);
		g.setEdges(5, new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of(17, 86, 5), Set.of(2, 22));
		assertEquals(Set.of(17, 86, 5, 2, 22), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(17, 86, 5, 2, 22), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(1, 3, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		foreachBoolConfig(intGraph -> {
			final long seed = 0xb03f507f9a5e6db0L;
			GnmBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnmBipartiteGraphGenerator.newIntInstance() : GnmBipartiteGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(range(5), range(5, 10));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setEdges(5, new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(5), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			final long seed = 0x2c4aa9b55709eaceL;
			GnmBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnmBipartiteGraphGenerator.newIntInstance() : GnmBipartiteGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(5, 5, new AtomicInteger()::getAndIncrement);
			g.setEdges(5, new AtomicInteger()::getAndIncrement);

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
			g.setDirectedAll();
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
			g.setUndirected();
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
			g.setDirectedLeftToRight();
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
			g.setDirectedRightToLeft();;
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
	public void testEdgeNum() {
		foreachBoolConfig(intGraph -> {
			GnmBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnmBipartiteGraphGenerator.newIntInstance() : GnmBipartiteGraphGenerator.newInstance();
			g.setSeed(0xb8c895d35088b0cL);
			g.setVertices(5, 5, new AtomicInteger()::getAndIncrement);
			g.setParallelEdges(false);
			assertThrows(IllegalArgumentException.class, () -> g.setEdges(-1, new AtomicInteger()::getAndIncrement));
			g.setEdges(500, new AtomicInteger()::getAndIncrement);
			assertThrows(IllegalArgumentException.class, () -> g.generate());
			g.setEdges(5, new AtomicInteger()::getAndIncrement);
			assertNotNull(g.generate());
		});
	}

	@Test
	public void testParallelEdges() {
		foreachBoolConfig(intGraph -> {
			final int Undirected = 0;
			final int DirectedAll = 1;
			final int DirectedLeftToRight = 2;
			final int DirectedRightToLeft = 3;
			for (int direction : IntList.of(Undirected, DirectedAll, DirectedLeftToRight, DirectedRightToLeft)) {
				final long seed = 0x9ce94054e6b1bd41L;
				GnmBipartiteGraphGenerator<Integer, Integer> g = intGraph ? GnmBipartiteGraphGenerator.newIntInstance()
						: GnmBipartiteGraphGenerator.newInstance();
				g.setSeed(seed);
				if (direction == Undirected) {
					g.setUndirected();
				} else if (direction == DirectedAll) {
					g.setDirectedAll();
				} else if (direction == DirectedLeftToRight) {
					g.setDirectedLeftToRight();
				} else if (direction == DirectedRightToLeft) {
					g.setDirectedRightToLeft();
				} else {
					throw new AssertionError();
				}

				final int n1 = 4, n2 = 5;
				g.setVertices(n1, n2, new AtomicInteger()::getAndIncrement);

				int maxNumberOfEdges = n1 * n2 * (direction == DirectedAll ? 2 : 1);

				/* check default */
				g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
				Graph<Integer, Integer> g1 = g.generate();
				assertTrue(Graphs.containsParallelEdges(g1));

				/* check parallel-edges disabled */
				for (int repeat = 0; repeat < 20; repeat++) {
					g.setParallelEdges(false);
					g.setEdges(maxNumberOfEdges, new AtomicInteger()::getAndIncrement);
					Graph<Integer, Integer> g2 = g.generate();
					assertFalse(Graphs.containsParallelEdges(g2));
				}

				/* check parallel-edges enabled */
				g.setParallelEdges(true);
				g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
				Graph<Integer, Integer> g3 = g.generate();
				assertTrue(Graphs.containsParallelEdges(g3));

				/* Too much edges to enforce non-parallel edges */
				g.setParallelEdges(false);
				g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
				assertThrows(IllegalArgumentException.class, () -> g.generate());
			}
		});
	}

	@Test
	public void denseGraphs() {
		final long seed = 0xce3625df14f40f2fL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, parallelEdges) -> {
			final int Undirected = 0;
			final int DirectedAll = 1;
			final int DirectedLeftToRight = 2;
			final int DirectedRightToLeft = 3;
			for (int direction : IntList.of(Undirected, DirectedAll, DirectedLeftToRight, DirectedRightToLeft)) {
				GnmBipartiteGraphGenerator<Integer, Integer> g = intGraph ? GnmBipartiteGraphGenerator.newIntInstance()
						: GnmBipartiteGraphGenerator.newInstance();
				g.setSeed(rand.nextLong());
				g.setParallelEdges(parallelEdges);
				if (direction == Undirected) {
					g.setUndirected();
				} else if (direction == DirectedAll) {
					g.setDirectedAll();
				} else if (direction == DirectedLeftToRight) {
					g.setDirectedLeftToRight();
				} else if (direction == DirectedRightToLeft) {
					g.setDirectedRightToLeft();
				} else {
					throw new AssertionError();
				}

				final int n1 = 40, n2 = 60;
				g.setVertices(n1, n2, new AtomicInteger()::getAndIncrement);

				int maxNumberOfEdges = n1 * n2 * (direction == DirectedAll ? 2 : 1);

				for (int repeat = 50; repeat-- > 0;) {
					final int m = rand.nextInt(maxNumberOfEdges + 1);
					g.setEdges(m, new AtomicInteger()::getAndIncrement);
					Graph<Integer, Integer> g1 = g.generate();
					assertEquals(n1 + n2, g1.vertices().size());
					assertEquals(m, g1.edges().size());
					assertEqualsBool(direction != Undirected, g1.isDirected());
					if (!parallelEdges)
						assertFalse(Graphs.containsParallelEdges(g1));
				}
			}
		});
	}

	@Test
	public void emptyLeftVerticesSetAndNonEmptyEdgeSet() {
		GnmBipartiteGraphGenerator<Integer, Integer> g = GnmBipartiteGraphGenerator.newInstance();
		g.setVertices(Set.of(), IntSet.of(1, 2, 3));
		g.setEdges(5, new AtomicInteger()::getAndIncrement);
		assertThrows(IllegalArgumentException.class, () -> g.generate());
		g.setEdges(0, new AtomicInteger()::getAndIncrement);
		assertDoesNotThrow(() -> g.generate());
	}

	@Test
	public void emptyRightVerticesSetAndNonEmptyEdgeSet() {
		GnmBipartiteGraphGenerator<Integer, Integer> g = GnmBipartiteGraphGenerator.newInstance();
		g.setVertices(IntSet.of(1, 2, 3), Set.of());
		g.setEdges(5, new AtomicInteger()::getAndIncrement);
		assertThrows(IllegalArgumentException.class, () -> g.generate());
		g.setEdges(0, new AtomicInteger()::getAndIncrement);
		assertDoesNotThrow(() -> g.generate());
	}

}
