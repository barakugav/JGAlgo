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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.internal.util.TestBase;

public class GnmGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		final long seed = 0x2b73d420f1841822L;
		GnmGraphGenerator<String, Integer> g = GnmGraphGenerator.newInstance();
		g.setSeed(seed);
		g.setEdges(5, new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of("a", "b", "c"));
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		final long seed = 0xb814112bc86db5e5L;
		GnmGraphGenerator<Integer, Integer> g = GnmGraphGenerator.newIntInstance();
		g.setSeed(seed);
		g.setEdges(5, new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of(17, 86, 5));
		assertEquals(Set.of(17, 86, 5), g.generate().vertices());
		assertEquals(Set.of(17, 86, 5), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		foreachBoolConfig((intGraph, directed, selfEdges) -> {
			final long seed = 0x247dbba1a14aabacL;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setDirected(directed);
			g.setSelfEdges(selfEdges);
			g.setVertices(range(10));

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
			final long seed = 0x188be689e819c512L;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(10, new AtomicInteger()::getAndIncrement);
			g.setEdges(5, new AtomicInteger()::getAndIncrement);

			/* check default */
			assertFalse(g.generate().isDirected());

			/* check directed */
			g.setDirected(true);
			assertTrue(g.generate().isDirected());

			/* check undirected */
			g.setDirected(false);
			assertFalse(g.generate().isDirected());
		});
	}

	@Test
	public void testEdgeNum() {
		foreachBoolConfig(intGraph -> {
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setVertices(5, new AtomicInteger()::getAndIncrement);
			g.setParallelEdges(false);
			assertThrows(IllegalArgumentException.class, () -> g.setEdges(-1, new AtomicInteger()::getAndIncrement));
			assertThrows(IllegalArgumentException.class, () -> {
				g.setEdges(500, new AtomicInteger()::getAndIncrement);
				g.generate();
			});
			g.setEdges(5, new AtomicInteger()::getAndIncrement);
			assertNotNull(g.generate());
		});
	}

	@Test
	public void testSelfEdges() {
		foreachBoolConfig(intGraph -> {
			final long seed = 0x113bd1ca591efcd8L;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setVertices(20, new AtomicInteger()::getAndIncrement);
			g.setEdges(190, new AtomicInteger()::getAndIncrement);

			/* check default */
			Graph<Integer, Integer> g1 = g.generate();
			assertFalse(g1.edges().stream().anyMatch(e -> g1.edgeSource(e).equals(g1.edgeTarget(e))));

			/* check self-edges enabled */
			g.setSelfEdges(true);
			Graph<Integer, Integer> g2 = g.generate();
			assertTrue(g2.edges().stream().anyMatch(e -> g2.edgeSource(e).equals(g2.edgeTarget(e))));

			/* check self-edges disabled */
			g.setSelfEdges(false);
			Graph<Integer, Integer> g3 = g.generate();
			assertFalse(g3.edges().stream().anyMatch(e -> g3.edgeSource(e).equals(g3.edgeTarget(e))));
		});
	}

	@Test
	public void testParallelEdges() {
		foreachBoolConfig((intGraph, directed, selfEdges) -> {
			final long seed = 0x22026362f398235fL;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setSeed(seed);
			g.setDirected(directed);
			g.setSelfEdges(selfEdges);

			final int n = 4;
			g.setVertices(n, new AtomicInteger()::getAndIncrement);

			int maxNumberOfEdges = n * (n - 1);
			if (!directed)
				maxNumberOfEdges /= 2;
			if (selfEdges)
				maxNumberOfEdges += n;

			/* check default */
			g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g1 = g.generate();
			assertTrue(Graphs.containsParallelEdges(g1));

			/* check parallel-edges enabled */
			for (int repeat = 0; repeat < 20; repeat++) {
				g.setParallelEdges(false);
				g.setEdges(maxNumberOfEdges, new AtomicInteger()::getAndIncrement);
				Graph<Integer, Integer> g2 = g.generate();
				assertFalse(Graphs.containsParallelEdges(g2));
			}

			/* check parallel-edges disabled */
			g.setParallelEdges(true);
			g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g3 = g.generate();
			assertTrue(Graphs.containsParallelEdges(g3));

			/* Too much edges to enforce non-parallel edges */
			g.setParallelEdges(false);
			g.setEdges(maxNumberOfEdges + 1, new AtomicInteger()::getAndIncrement);
			assertThrows(IllegalArgumentException.class, () -> g.generate());
		});
	}

	@Test
	public void denseGraphs() {
		final long seed = 0xfcf449779582301dL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, directed, selfEdges, parallelEdges) -> {
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? GnmGraphGenerator.newIntInstance() : GnmGraphGenerator.newInstance();
			g.setSeed(rand.nextLong());
			g.setDirected(directed);
			g.setSelfEdges(selfEdges);
			g.setParallelEdges(parallelEdges);

			final int n = 100;
			g.setVertices(n, new AtomicInteger()::getAndIncrement);

			int maxNumberOfEdges = n * (n - 1);
			if (!directed)
				maxNumberOfEdges /= 2;
			if (selfEdges)
				maxNumberOfEdges += n;

			for (int repeat = 50; repeat-- > 0;) {
				final int m = rand.nextInt(maxNumberOfEdges + 1);
				g.setEdges(m, new AtomicInteger()::getAndIncrement);
				Graph<Integer, Integer> g1 = g.generate();
				assertEquals(n, g1.vertices().size());
				assertEquals(m, g1.edges().size());
				assertEqualsBool(directed, g1.isDirected());
				if (!selfEdges)
					assertTrue(Graphs.selfEdges(g1).isEmpty());
				if (!parallelEdges)
					assertFalse(Graphs.containsParallelEdges(g1));
			}
		});
	}

}
