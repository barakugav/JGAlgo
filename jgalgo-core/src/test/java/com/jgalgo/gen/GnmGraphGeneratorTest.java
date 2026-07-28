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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class GnmGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		final long seed = 0x2b73d420f1841822L;
		GnmGraphGenerator<String, Integer> g = new GnmGraphGenerator<>();
		g.seed(seed);
		g.edges(5, IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(Set.of("a", "b", "c"));
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());

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
		final long seed = 0xb814112bc86db5e5L;
		GnmGraphGenerator<Integer, Integer> g = new GnmGraphGenerator<>(IntGraphFactory.undirected());
		g.seed(seed);
		g.edges(5, IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(Set.of(17, 86, 5));
		assertEquals(Set.of(17, 86, 5), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(17, 86, 5), g.generate().vertices());

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
			final long seed = 0x247dbba1a14aabacL;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.seed(seed);
			g.directed(directed);
			g.selfEdges(selfEdges);
			g.vertices(range(10));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.edges(5, IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(1, 1 + 5), g1.edges());

			g.edges(5); /* default vertex builder */
			if (intGraph) {
				assertEquals(5, g.generate().edges().size());
			} else {
				assertThrows(IllegalStateException.class, () -> g.generate());
			}
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			final long seed = 0x188be689e819c512L;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.seed(seed);
			g.vertices(10, IdBuilderInt.defaultBuilder());
			g.edges(5, IdBuilderInt.defaultBuilder());

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

	@Test
	public void testEdgeNum() {
		foreachBoolConfig(intGraph -> {
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.vertices(5, IdBuilderInt.defaultBuilder());
			g.parallelEdges(false);
			assertThrows(IllegalArgumentException.class, () -> g.edges(-1, IdBuilderInt.defaultBuilder()));
			g.edges(500, IdBuilderInt.defaultBuilder());
			assertThrows(IllegalArgumentException.class, () -> g.generate());
			g.edges(5, IdBuilderInt.defaultBuilder());
			assertNotNull(g.generate());
		});
	}

	@Test
	public void testSelfEdges() {
		foreachBoolConfig(intGraph -> {
			final long seed = 0x113bd1ca591efcd8L;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.seed(seed);
			g.vertices(20, IdBuilderInt.defaultBuilder());
			g.edges(190, IdBuilderInt.defaultBuilder());

			/* check default */
			Graph<Integer, Integer> g1 = g.generate();
			assertFalse(g1.edges().stream().anyMatch(e -> g1.edgeSource(e).equals(g1.edgeTarget(e))));

			/* check self-edges enabled */
			g.selfEdges(true);
			Graph<Integer, Integer> g2 = g.generate();
			assertTrue(g2.edges().stream().anyMatch(e -> g2.edgeSource(e).equals(g2.edgeTarget(e))));

			/* check self-edges disabled */
			g.selfEdges(false);
			Graph<Integer, Integer> g3 = g.generate();
			assertFalse(g3.edges().stream().anyMatch(e -> g3.edgeSource(e).equals(g3.edgeTarget(e))));
		});
	}

	@Test
	public void testParallelEdges() {
		foreachBoolConfig((intGraph, directed, selfEdges) -> {
			final long seed = 0x22026362f398235fL;
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.seed(seed);
			g.directed(directed);
			g.selfEdges(selfEdges);

			final int n = 4;
			g.vertices(n, IdBuilderInt.defaultBuilder());

			int maxNumberOfEdges = n * (n - 1);
			if (!directed)
				maxNumberOfEdges /= 2;
			if (selfEdges)
				maxNumberOfEdges += n;

			/* check default */
			g.edges(maxNumberOfEdges + 1, IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g1 = g.generate();
			assertTrue(Graphs.containsParallelEdges(g1));

			/* check parallel-edges disabled */
			for (int repeat = 0; repeat < 20; repeat++) {
				g.parallelEdges(false);
				g.edges(maxNumberOfEdges, IdBuilderInt.defaultBuilder());
				Graph<Integer, Integer> g2 = g.generate();
				assertFalse(Graphs.containsParallelEdges(g2));
			}

			/* check parallel-edges enabled */
			g.parallelEdges(true);
			g.edges(maxNumberOfEdges + 1, IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g3 = g.generate();
			assertTrue(Graphs.containsParallelEdges(g3));

			/* Too much edges to enforce non-parallel edges */
			g.parallelEdges(false);
			g.edges(maxNumberOfEdges + 1, IdBuilderInt.defaultBuilder());
			assertThrows(IllegalArgumentException.class, () -> g.generate());
		});
	}

	@Test
	public void denseGraphs() {
		final long seed = 0xfcf449779582301dL;
		final Random rand = new Random(seed);
		foreachBoolConfig((intGraph, directed, selfEdges, parallelEdges) -> {
			GnmGraphGenerator<Integer, Integer> g =
					intGraph ? new GnmGraphGenerator<>(IntGraphFactory.undirected()) : new GnmGraphGenerator<>();
			g.seed(rand.nextLong());
			g.directed(directed);
			g.selfEdges(selfEdges);
			g.parallelEdges(parallelEdges);

			final int n = 100;
			g.vertices(n, IdBuilderInt.defaultBuilder());

			int maxNumberOfEdges = n * (n - 1);
			if (!directed)
				maxNumberOfEdges /= 2;
			if (selfEdges)
				maxNumberOfEdges += n;

			for (int repeat = 50; repeat-- > 0;) {
				final int m = rand.nextInt(maxNumberOfEdges + 1);
				g.edges(m, IdBuilderInt.defaultBuilder());
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

	@Test
	public void emptyVerticesSetAndNonEmptyEdgeSet() {
		GnmGraphGenerator<Integer, Integer> g = new GnmGraphGenerator<>(IntGraphFactory.undirected());
		g.vertices(Set.of());
		g.edges(6, IdBuilderInt.defaultBuilder());
		assertThrows(IllegalArgumentException.class, () -> g.generate());
		g.edges(0, IdBuilderInt.defaultBuilder());
		assertDoesNotThrow(() -> g.generate());
	}

	@Test
	public void graphFactory() {
		assertNotNull(new GnmGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		GnmGraphGenerator<Integer, Integer> g = new GnmGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
