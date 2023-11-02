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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class EmptyGraphGeneratorTest extends TestBase {

	@Test
	public void testIntGraph() {
		assertTrue(EmptyGraphGenerator.newIntInstance().generate() instanceof IntGraph);
		assertFalse(EmptyGraphGenerator.<Integer, Integer>newInstance().generate() instanceof IntGraph);
	}

	@Test
	public void testVertices() {
		assertEquals(Set.of(), EmptyGraphGenerator.newInstance().generate().vertices());
		EmptyGraphGenerator<String, Integer> g = EmptyGraphGenerator.newInstance();
		g.setVertices(Set.of("a", "b", "c"));
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		assertEquals(Set.of(), EmptyGraphGenerator.newInstance().generate().edges());
		EmptyGraphGenerator<String, Integer> g = EmptyGraphGenerator.newInstance();
		g.setVertices(Set.of("a", "b", "c"));
		assertEquals(Set.of(), g.generate().edges());
	}

	@Test
	public void testDirected() {
		EmptyGraphGenerator<String, Integer> g = EmptyGraphGenerator.newInstance();

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
