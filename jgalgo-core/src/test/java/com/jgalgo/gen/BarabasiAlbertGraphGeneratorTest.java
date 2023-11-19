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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;

public class BarabasiAlbertGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		BarabasiAlbertGraphGenerator<String, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
		g.setSeed(0x31fea9e869929b26L);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = Range.of(50).intStream().mapToObj(String::valueOf).collect(Collectors.toSet());
		g.setVertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		g.setEdgesToAddPerStep(1);
		g.setInitialCliqueSize(2);
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g = BarabasiAlbertGraphGenerator.newIntInstance();
		g.setSeed(0x7bc0644a3988b9e0L);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = Range.of(50);
		g.setVertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> vertexId.getAndIncrement());
		g.setEdgesToAddPerStep(1);
		g.setInitialCliqueSize(2);
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			g.setSeed(0x58ca12b719e97e61L);
			g.setVertices(Range.of(50));

			/* edges were not set yet */
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setEdges(new AtomicInteger()::getAndIncrement);
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(Range.of(g1.edges().size()), g1.edges());
		}
	}

	@Test
	public void testDirected() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			g.setSeed(0x8df4db32de040f1dL);
			g.setVertices(40, new AtomicInteger()::getAndIncrement);
			g.setEdges(new AtomicInteger()::getAndIncrement);

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
	public void edgesPerStep() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			assertThrows(IllegalArgumentException.class, () -> g.setEdgesToAddPerStep(-3));
			g.setEdgesToAddPerStep(4);
		}
	}

	@Test
	public void tooBigEdgesPerStep() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			g.setSeed(0x58ca12b719e97e61L);
			g.setVertices(Range.of(10));
			g.setEdges(new AtomicInteger()::getAndIncrement);
			g.setInitialCliqueSize(3);

			g.setEdgesToAddPerStep(4);
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setEdgesToAddPerStep(3);
			assertNotNull(g.generate());
		}
	}

	@Test
	public void testInitialCliqueSize() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			assertThrows(IllegalArgumentException.class, () -> g.setInitialCliqueSize(-3));
			g.setInitialCliqueSize(4);
		}
	}

	@Test
	public void tooBigInitialClique() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			g.setSeed(0x58ca12b719e97e61L);
			g.setVertices(Range.of(10));
			g.setEdges(new AtomicInteger()::getAndIncrement);

			g.setInitialCliqueSize(11);
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.setInitialCliqueSize(10);
			assertNotNull(g.generate());
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			BarabasiAlbertGraphGenerator<Integer, Integer> g = intGraph ? BarabasiAlbertGraphGenerator.newIntInstance()
					: BarabasiAlbertGraphGenerator.newInstance();
			g.setSeed(0x34960acd3d3b944cL);
			g.setVertices(Range.of(40));
			g.setEdges(new AtomicInteger()::getAndIncrement);

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		}
	}

}
