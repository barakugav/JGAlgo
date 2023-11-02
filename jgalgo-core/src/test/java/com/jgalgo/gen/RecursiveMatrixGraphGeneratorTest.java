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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;

public class RecursiveMatrixGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		RecursiveMatrixGraphGenerator<String, Integer> g = RecursiveMatrixGraphGenerator.newInstance();
		g.setSeed(0x69bd6fdb73f97870L);
		g.setVertices(Set.of("a", "b", "c"));
		g.setEdges(1, new AtomicInteger()::getAndIncrement);
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());
		assertEquals(Set.of("a", "b", "c"), g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		RecursiveMatrixGraphGenerator<Integer, Integer> g = RecursiveMatrixGraphGenerator.newInstance();
		g.setSeed(0x28522dc13436389fL);
		g.setVertices(Range.of(100));
		g.setEdges(5, new AtomicInteger()::getAndIncrement);
		assertEquals(Range.of(0, 5), g.generate().edges());
		assertEquals(Range.of(5, 10), g.generate().edges());

		assertThrows(IllegalArgumentException.class, () -> g.setEdges(-5, new AtomicInteger()::getAndIncrement));
	}

	@Test
	public void testDirected() {
		RecursiveMatrixGraphGenerator<Integer, Integer> g = RecursiveMatrixGraphGenerator.newInstance();
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

	@Test
	public void testEdgeProbabilities() {
		RecursiveMatrixGraphGenerator<Integer, Integer> g = RecursiveMatrixGraphGenerator.newInstance();
		assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbabilities(0.2, 0.2, 0.2, 0.2));
		assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbabilities(-0.2, 0.2, 0.5, 0.5));
		g.setEdgeProbabilities(0.3, 0.3, 0.3, 0.1);
	}

}
