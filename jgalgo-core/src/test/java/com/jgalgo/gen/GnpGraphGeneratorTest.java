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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.TestBase;

public class GnpGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		GnpGraphGenerator<String, Integer> g = GnpGraphGenerator.newInstance();
		g.setSeed(0xf36cd36da8801a6cL);
		g.setEdges(new AtomicInteger()::getAndIncrement);

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
		GnpGraphGenerator<Integer, Integer> g = GnpGraphGenerator.newIntInstance();
		g.setSeed(0x430f4b24893b9f43L);
		g.setEdges(new AtomicInteger()::getAndIncrement);

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
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? GnpGraphGenerator.newIntInstance() : GnpGraphGenerator.newInstance();
			g.setSeed(0x2157279ef75b0caaL);
			g.setVertices(range(10));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setEdges(new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(g1.edges().size()), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? GnpGraphGenerator.newIntInstance() : GnpGraphGenerator.newInstance();
			g.setSeed(0x2d3d96ffc9c5d464L);
			g.setVertices(10, new AtomicInteger()::getAndIncrement);
			g.setEdges(new AtomicInteger()::getAndIncrement);

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
	public void testEdgeProbabilities() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? GnpGraphGenerator.newIntInstance() : GnpGraphGenerator.newInstance();
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbability(1.1));
			assertThrows(IllegalArgumentException.class, () -> g.setEdgeProbability(-0.1));
			g.setEdgeProbability(0.5);
		});
	}

	@Test
	public void testSelfEdges() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? GnpGraphGenerator.newIntInstance() : GnpGraphGenerator.newInstance();
			g.setVertices(12, new AtomicInteger()::getAndIncrement);
			g.setEdges(new AtomicInteger()::getAndIncrement);
			g.setEdgeProbability(0.98);

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

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? GnpGraphGenerator.newIntInstance() : GnpGraphGenerator.newInstance();
			g.setVertices(12, new AtomicInteger()::getAndIncrement);
			g.setEdges(new AtomicInteger()::getAndIncrement);
			g.setEdgeProbability(0.98);

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

}
