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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.bipartite.BipartiteGraphs;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class GnpBipartiteGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		GnpBipartiteGraphGenerator<String, Integer> g = new GnpBipartiteGraphGenerator<>();
		g.seed(0xf28cd577e4d753b2L);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(Set.of("a", "b"), Set.of("c", "d"));
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());

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
		final long seed = 0x5aa45d619a1262edL;
		GnpBipartiteGraphGenerator<Integer, Integer> g = new GnpBipartiteGraphGenerator<>(IntGraphFactory.undirected());
		g.seed(seed);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(Set.of(17, 86), Set.of(5, 55));
		assertEquals(Set.of(17, 86, 5, 55), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(17, 86, 5, 55), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(2, 3, existingIds -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3, 4), g.generate().vertices());
		assertEquals(Set.of(5, 6, 7, 8, 9), g.generate().vertices());

		g.vertices(5, 4); /* default vertex builder */
		assertEquals(9, g.generate().vertices().size());

		assertThrows(IllegalArgumentException.class, () -> g.vertices(-3, 3));
		assertThrows(IllegalArgumentException.class, () -> g.vertices(3, -3));
	}

	@Test
	public void edges() {
		final long seed = 0x66054b864cf83d5dL;
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpBipartiteGraphGenerator<>(IntGraphFactory.undirected())
							: new GnpBipartiteGraphGenerator<>();
			g.seed(seed);
			g.vertices(range(5), range(5, 10));

			/* edges were not set yet */
			if (!intGraph) {
				assertThrows(IllegalStateException.class, () -> g.generate());
			} else {
				/* int graph factory should have a default id builder */
				assertNotNull(g.generate());
			}

			g.edges(IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(1, 1 + g1.edges().size()), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		final long seed = 0xa42a8b1ef0ba4412L;
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpBipartiteGraphGenerator<>(IntGraphFactory.undirected())
							: new GnpBipartiteGraphGenerator<>();
			g.seed(seed);
			g.vertices(6, 4, IdBuilderInt.defaultBuilder());
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
	public void testEdgeProbabilities() {
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpBipartiteGraphGenerator<>(IntGraphFactory.undirected())
							: new GnpBipartiteGraphGenerator<>();
			assertThrows(IllegalArgumentException.class, () -> g.edgeProbability(1.1));
			assertThrows(IllegalArgumentException.class, () -> g.edgeProbability(-0.1));
			g.edgeProbability(0.5);
		});
	}

	@Test
	public void probabilityZero() {
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpBipartiteGraphGenerator<>(IntGraphFactory.undirected())
							: new GnpBipartiteGraphGenerator<>();
			g.edgeProbability(0);
			g.vertices(range(5), range(5, 10));
			g.edges(IdBuilderInt.defaultBuilder());

			for (int repeat = 0; repeat < 20; repeat++)
				assertTrue(g.generate().edges().isEmpty());
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new GnpBipartiteGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		GnpBipartiteGraphGenerator<Integer, Integer> g = new GnpBipartiteGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
