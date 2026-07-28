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
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class RecursiveMatrixGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		RecursiveMatrixGraphGenerator<String, Integer> g = new RecursiveMatrixGraphGenerator<>();
		g.seed(0x69bd6fdb73f97870L);
		g.edges(1, IdBuilderInt.defaultBuilder());

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
		RecursiveMatrixGraphGenerator<Integer, Integer> g =
				new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected());
		g.seed(0xa542c7970abb70b9L);
		g.edges(1, IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.vertices(Set.of(88, 99, 1678));
		assertEquals(Set.of(88, 99, 1678), g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(Set.of(88, 99, 1678), g.generate().vertices());

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
		foreachBoolConfig((intGraph, directed) -> {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
							: new RecursiveMatrixGraphGenerator<>();
			g.seed(0x28522dc13436389fL);
			g.directed(directed);
			g.vertices(range(100));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.edges(5, IdBuilderInt.defaultBuilder());
			assertEquals(range(1, 6), g.generate().edges());
			assertEquals(range(6, 11), g.generate().edges());

			g.edges(5); /* default vertex builder */
			if (intGraph) {
				assertEquals(5, g.generate().edges().size());
			} else {
				assertThrows(IllegalStateException.class, () -> g.generate());
			}
			assertThrows(IllegalArgumentException.class, () -> g.edges(-5));
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
							: new RecursiveMatrixGraphGenerator<>();
			g.seed(0x86c8f3658c4c34f6L);
			g.vertices(range(100));
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
	public void testEdgeProbabilities() {
		foreachBoolConfig(intGraph -> {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
							: new RecursiveMatrixGraphGenerator<>();
			g.seed(0x7d5f093080604661L);
			g.vertices(range(10));
			g.edges(30, IdBuilderInt.defaultBuilder());
			assertThrows(IllegalArgumentException.class, () -> g.edgeProbabilities(0.2, 0.2, 0.2, 0.2));
			assertThrows(IllegalArgumentException.class,
					() -> g.edgeProbabilities(-1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0));
			assertThrows(IllegalArgumentException.class,
					() -> g.edgeProbabilities(1.0 / 3.0, -1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0));
			assertThrows(IllegalArgumentException.class,
					() -> g.edgeProbabilities(1.0 / 3.0, 1.0 / 3.0, -1.0 / 3.0, 1.0 / 3.0));
			assertThrows(IllegalArgumentException.class,
					() -> g.edgeProbabilities(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, -1.0 / 3.0));

			g.edgeProbabilities(0.3, 0.3, 0.3, 0.1);
			assertNotNull(g.generate());

			/* b and c must be equal in undirected graphs */
			g.directed(false);
			g.edgeProbabilities(0.3, 0.2, 0.4, 0.1);
			assertThrows(IllegalArgumentException.class, () -> g.generate());

			g.directed(true);
			g.edgeProbabilities(0.3, 0.2, 0.4, 0.1);
			assertNotNull(g.generate());
		});
	}

	@Test
	public void testTooManyEdges() {
		foreachBoolConfig(intGraph -> {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
							: new RecursiveMatrixGraphGenerator<>();
			g.seed(0x28522dc13436389fL);
			g.vertices(range(10));
			g.edges(100, IdBuilderInt.defaultBuilder());

			assertThrows(IllegalArgumentException.class, () -> g.generate());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
							: new RecursiveMatrixGraphGenerator<>();
			g.seed(0x29362bc2fa3dddc3L);
			g.vertices(range(10));
			g.edges(30, IdBuilderInt.defaultBuilder());

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new RecursiveMatrixGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		RecursiveMatrixGraphGenerator<Integer, Integer> g = new RecursiveMatrixGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
