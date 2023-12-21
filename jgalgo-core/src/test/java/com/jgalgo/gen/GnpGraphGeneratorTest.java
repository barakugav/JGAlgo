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

public class GnpGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		GnpGraphGenerator<String, Integer> g = new GnpGraphGenerator<>();
		g.seed(0xf36cd36da8801a6cL);
		g.edges(IdBuilderInt.defaultBuilder());

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
		GnpGraphGenerator<Integer, Integer> g = new GnpGraphGenerator<>(IntGraphFactory.undirected());
		g.seed(0x430f4b24893b9f43L);
		g.edges(IdBuilderInt.defaultBuilder());

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
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			g.seed(0x2157279ef75b0caaL);
			g.vertices(range(10));

			/* edges were not set yet */
			if (intGraph) {
				assertNotNull(g.generate());
			} else {
				assertThrows(IllegalStateException.class, () -> g.generate());
			}

			g.edges(IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(1, 1 + g1.edges().size()), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			g.seed(0x2d3d96ffc9c5d464L);
			g.vertices(10, IdBuilderInt.defaultBuilder());
			g.edges(IdBuilderInt.defaultBuilder());

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
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			assertThrows(IllegalArgumentException.class, () -> g.edgeProbability(1.1));
			assertThrows(IllegalArgumentException.class, () -> g.edgeProbability(-0.1));
			g.edgeProbability(0.5);
		});
	}

	@Test
	public void testSelfEdges() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			g.vertices(12, IdBuilderInt.defaultBuilder());
			g.edges(IdBuilderInt.defaultBuilder());
			g.edgeProbability(0.98);

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

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			g.vertices(12, IdBuilderInt.defaultBuilder());
			g.edges(IdBuilderInt.defaultBuilder());
			g.edgeProbability(0.98);

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

	@Test
	public void probabilityZero() {
		foreachBoolConfig(intGraph -> {
			GnpGraphGenerator<Integer, Integer> g =
					intGraph ? new GnpGraphGenerator<>(IntGraphFactory.undirected()) : new GnpGraphGenerator<>();
			g.edgeProbability(0);
			g.vertices(range(10));
			g.edges(IdBuilderInt.defaultBuilder());

			for (int repeat = 0; repeat < 20; repeat++)
				assertTrue(g.generate().edges().isEmpty());
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new GnpGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		GnpGraphGenerator<Integer, Integer> g = new GnpGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
