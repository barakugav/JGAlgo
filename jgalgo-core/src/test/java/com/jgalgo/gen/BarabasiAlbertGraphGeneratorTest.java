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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;

public class BarabasiAlbertGraphGeneratorTest extends TestBase {

	@Test
	public void testVertices() {
		BarabasiAlbertGraphGenerator<String, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
		g.setSeed(0x31fea9e869929b26L);
		Set<String> vertices = Range.of(50).intStream().mapToObj(String::valueOf).collect(Collectors.toSet());
		g.setVertices(vertices);
		g.setEdges(new AtomicInteger()::getAndIncrement);
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		g.setEdgesToAddPerStep(1);
		g.setInitialCliqueSize(2);
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
		g.setSeed(0x58ca12b719e97e61L);
		g.setVertices(Range.of(50));
		g.setEdges(new AtomicInteger()::getAndIncrement);
		Graph<Integer, Integer> g1 = g.generate();
		assertEquals(Range.of(g1.edges().size()), g1.edges());
	}

	@Test
	public void testDirected() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
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

	@Test
	public void testAverageDegrees() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
		assertThrows(IllegalArgumentException.class, () -> g.setEdgesToAddPerStep(-3));
		g.setEdgesToAddPerStep(4);
	}

	@Test
	public void testInitialCliqueSize() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g = BarabasiAlbertGraphGenerator.newInstance();
		assertThrows(IllegalArgumentException.class, () -> g.setInitialCliqueSize(-3));
		g.setInitialCliqueSize(4);
	}

}
