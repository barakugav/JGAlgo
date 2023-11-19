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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntList;

public class EmptyGraphGeneratorTest extends TestBase {

	@Test
	public void testIntGraph() {
		assertTrue(EmptyGraphGenerator.newIntInstance().generate() instanceof IntGraph);
		assertFalse(EmptyGraphGenerator.<Integer, Integer>newInstance().generate() instanceof IntGraph);
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVertices() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? EmptyGraphGenerator.newIntInstance() : EmptyGraphGenerator.newInstance();
			assertEquals(Set.of(), g.generate().vertices());

			g.setVertices(Set.of(0, 1, 2));
			assertEquals(Set.of(0, 1, 2), g.generate().vertices());
		}
	}

	@Test
	public void testEdges() {
		EmptyGraphGenerator<String, Integer> g = EmptyGraphGenerator.newInstance();
		assertEquals(Set.of(), g.generate().edges());
		g.setVertices(Set.of("a", "b", "c"));
		assertEquals(Set.of(), g.generate().edges());
	}

	@Test
	public void testDirected() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? EmptyGraphGenerator.newIntInstance() : EmptyGraphGenerator.newInstance();

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

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			EmptyGraphGenerator<Integer, Integer> g =
					intGraph ? EmptyGraphGenerator.newIntInstance() : EmptyGraphGenerator.newInstance();
			g.setVertices(IntList.of(1, 9, 3, 4, 5));

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		}
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

}
