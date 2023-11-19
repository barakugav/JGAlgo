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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;

public class RecursiveMatrixGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		RecursiveMatrixGraphGenerator<String, Integer> g = RecursiveMatrixGraphGenerator.newInstance();
		g.setSeed(0x69bd6fdb73f97870L);
		g.setEdges(1, new AtomicInteger()::getAndIncrement);

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
		RecursiveMatrixGraphGenerator<Integer, Integer> g = RecursiveMatrixGraphGenerator.newIntInstance();
		g.setSeed(0xa542c7970abb70b9L);
		g.setEdges(1, new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		g.setVertices(Set.of(88, 99, 1678));
		assertEquals(Set.of(88, 99, 1678), g.generate().vertices());
		assertEquals(Set.of(88, 99, 1678), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean directed : BooleanList.of(false, true)) {
				RecursiveMatrixGraphGenerator<Integer, Integer> g =
						intGraph ? RecursiveMatrixGraphGenerator.newIntInstance()
								: RecursiveMatrixGraphGenerator.newInstance();
				g.setSeed(0x28522dc13436389fL);
				g.setDirected(directed);
				g.setVertices(Range.of(100));

				/* edges were not set yet */
				assertThrows(IllegalStateException.class, () -> g.generate());

				g.setEdges(5, new AtomicInteger()::getAndIncrement);
				assertEquals(Range.of(0, 5), g.generate().edges());
				assertEquals(Range.of(5, 10), g.generate().edges());

				assertThrows(IllegalArgumentException.class,
						() -> g.setEdges(-5, new AtomicInteger()::getAndIncrement));
			}
		}
	}

	@Test
	public void testDirected() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? RecursiveMatrixGraphGenerator.newIntInstance()
							: RecursiveMatrixGraphGenerator.newInstance();
			g.setSeed(0x86c8f3658c4c34f6L);
			g.setVertices(Range.of(100));
			g.setEdges(5, new AtomicInteger()::getAndIncrement);

			/* check default */
			assertFalse(g.generate().isDirected());

			/* check directed */
			g.setDirected(true);
			assertTrue(g.generate().isDirected());

			/* check undirected */
			g.setDirected(false);
			assertFalse(g.generate().isDirected());
		}
	}

	@Test
	public void testEdgeProbabilities() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? RecursiveMatrixGraphGenerator.newIntInstance()
							: RecursiveMatrixGraphGenerator.newInstance();
			g.setSeed(0x7d5f093080604661L);
			g.setVertices(Range.of(10));
			g.setEdges(30, new AtomicInteger()::getAndIncrement);
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbabilities(0.2, 0.2, 0.2, 0.2));
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbabilities(-0.2, 0.2, 0.5, 0.5));

			g.setEdgeProbabilities(0.3, 0.3, 0.3, 0.1);
			assertNotNull(g.generate());

			/* b and c must be equal in undirected graphs */
			g.setDirected(false);
			g.setEdgeProbabilities(0.3, 0.2, 0.4, 0.1);
			assertThrows(IllegalArgumentException.class, () -> g.generate());

			g.setDirected(true);
			g.setEdgeProbabilities(0.3, 0.2, 0.4, 0.1);
			assertNotNull(g.generate());
		}
	}

	@Test
	public void testTooManyEdges() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? RecursiveMatrixGraphGenerator.newIntInstance()
							: RecursiveMatrixGraphGenerator.newInstance();
			g.setSeed(0x28522dc13436389fL);
			g.setVertices(Range.of(10));
			g.setEdges(100, new AtomicInteger()::getAndIncrement);

			assertThrows(IllegalArgumentException.class, () -> g.generate());
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			RecursiveMatrixGraphGenerator<Integer, Integer> g =
					intGraph ? RecursiveMatrixGraphGenerator.newIntInstance()
							: RecursiveMatrixGraphGenerator.newInstance();
			g.setSeed(0x29362bc2fa3dddc3L);
			g.setVertices(Range.of(10));
			g.setEdges(30, new AtomicInteger()::getAndIncrement);

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		}
	}

}
