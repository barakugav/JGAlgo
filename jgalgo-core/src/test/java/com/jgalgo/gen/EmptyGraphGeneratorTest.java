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
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

public class EmptyGraphGeneratorTest extends TestBase {

	@Test
	public void testIntGraph() {
		assertTrue(new EmptyGraphGenerator<>(IntGraphFactory.undirected()).generate() instanceof IntGraph);
		assertFalse(new EmptyGraphGenerator<Integer, Integer>().generate() instanceof IntGraph);
	}

	@SuppressWarnings("boxing")
	@Test
	public void vertices() {
		foreachBoolConfig(intGraph -> {
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? new EmptyGraphGenerator<>(IntGraphFactory.undirected()) : new EmptyGraphGenerator<>();
			assertEquals(Set.of(), g.generate().vertices());

			g.vertices(Set.of(0, 1, 2));
			assertEquals(Set.of(0, 1, 2), g.generate().vertices());

			AtomicInteger vertexId = new AtomicInteger();
			g.vertices(4, existingIds -> vertexId.getAndIncrement());
			assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
			assertEquals(Set.of(4, 5, 6, 7), g.generate().vertices());

			g.vertices(5); /* default vertex builder */
			if (intGraph) {
				assertEquals(5, g.generate().vertices().size());
			} else {
				assertThrows(IllegalStateException.class, () -> g.generate());
			}
			assertThrows(IllegalArgumentException.class, () -> g.vertices(-3));
		});
	}

	@Test
	public void edges() {
		EmptyGraphGenerator<String, Integer> g = new EmptyGraphGenerator<>();
		assertEquals(Set.of(), g.generate().edges());
		g.vertices(Set.of("a", "b", "c"));
		assertEquals(Set.of(), g.generate().edges());
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? new EmptyGraphGenerator<>(IntGraphFactory.undirected()) : new EmptyGraphGenerator<>();

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
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? new EmptyGraphGenerator<>(IntGraphFactory.undirected()) : new EmptyGraphGenerator<>();
			g.vertices(IntList.of(1, 9, 3, 4, 5));

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testEmptyGraphOneLine() {
		assertEquals(Set.of(), EmptyGraphGenerator.emptyGraph(Set.of()).vertices());
		assertEquals(Set.of(), EmptyGraphGenerator.emptyGraph(Set.of()).edges());
		assertEquals(Set.of("a", "b", "c"), EmptyGraphGenerator.emptyGraph(Set.of("a", "b", "c")).vertices());
		assertEquals(Set.of(0, 1, 2), EmptyGraphGenerator.emptyGraph(Set.of(0, 1, 2)).vertices());
		assertEquals(Set.of(), EmptyGraphGenerator.emptyGraph(Set.of("a", "b", "c")).edges());
		assertEquals(Set.of(), EmptyGraphGenerator.emptyGraph(Set.of(0, 1, 2)).edges());
	}

	@Test
	public void graphFactory() {
		assertNotNull(new EmptyGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		EmptyGraphGenerator<Integer, Integer> g = new EmptyGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
