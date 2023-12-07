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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.TestBase;

public class GnpBipartiteGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		GnpBipartiteGraphGenerator<String, Integer> g = GnpBipartiteGraphGenerator.newInstance();
		g.setSeed(0xf28cd577e4d753b2L);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of("a", "b"), Set.of("c", "d"));
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());
		assertEquals(Set.of("a", "b", "c", "d"), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(2, 2, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		final long seed = 0x5aa45d619a1262edL;
		GnpBipartiteGraphGenerator<Integer, Integer> g = GnpBipartiteGraphGenerator.newIntInstance();
		g.setSeed(seed);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of(17, 86), Set.of(5, 55));
		assertEquals(Set.of(17, 86, 5, 55), g.generate().vertices());
		assertEquals(Set.of(17, 86, 5, 55), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(2, 3, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3, 4), g.generate().vertices());
		assertEquals(Set.of(0, 1, 2, 3, 4), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		final long seed = 0x66054b864cf83d5dL;
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnpBipartiteGraphGenerator.newIntInstance() : GnpBipartiteGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(range(5), range(5, 10));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setEdges(new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(g1.edges().size()), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		final long seed = 0xa42a8b1ef0ba4412L;
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnpBipartiteGraphGenerator.newIntInstance() : GnpBipartiteGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(6, 4, new AtomicInteger()::getAndIncrement);
			g.setEdges(new AtomicInteger()::getAndIncrement);

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
	public void testEdgeProbabilities() {
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnpBipartiteGraphGenerator.newIntInstance() : GnpBipartiteGraphGenerator.newInstance();
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbability(1.1));
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbability(-0.1));
			g.setEdgeProbability(0.5);
		});
	}

	@Test
	public void probabilityZero() {
		foreachBoolConfig(intGraph -> {
			GnpBipartiteGraphGenerator<Integer, Integer> g =
					intGraph ? GnpBipartiteGraphGenerator.newIntInstance() : GnpBipartiteGraphGenerator.newInstance();
			g.setEdgeProbability(0);
			g.setVertices(range(5), range(5, 10));
			g.setEdges(new AtomicInteger()::getAndIncrement);

			for (int repeat = 0; repeat < 20; repeat++)
				assertTrue(g.generate().edges().isEmpty());
		});
	}

}
