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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;

public class BarabasiAlbertGraphGeneratorTest extends TestBase {

	@Test
	public void vertices() {
		BarabasiAlbertGraphGenerator<String, Integer> g = new BarabasiAlbertGraphGenerator<>();
		g.seed(0x31fea9e869929b26L);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = range(50).mapToObj(String::valueOf).collect(Collectors.toSet());
		g.vertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(4, existingIds -> String.valueOf(vertexId.getAndIncrement()));
		g.edgesPerStep(1);
		g.initialCliqueSize(2);
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("4", "5", "6", "7"), g.generate().vertices());

		g.vertices(5); /* default vertex builder */
		assertThrows(IllegalStateException.class, () -> g.generate());
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesIntGraph() {
		BarabasiAlbertGraphGenerator<Integer, Integer> g =
				new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected());
		g.seed(0x7bc0644a3988b9e0L);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = range(50);
		g.vertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.vertices(4, existingIds -> vertexId.getAndIncrement());
		g.edgesPerStep(1);
		g.initialCliqueSize(2);
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(4, 5, 6, 7), g.generate().vertices());

		g.vertices(5); /* default vertex builder */
		assertEquals(5, g.generate().vertices().size());

		assertThrows(IllegalArgumentException.class, () -> g.vertices(-3));
	}

	@Test
	public void edges() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			g.seed(0x58ca12b719e97e61L);
			g.vertices(range(50));

			/* edges were not set yet */
			if (!intGraph) {
				assertThrows(IllegalStateException.class, () -> g.generate());
			} else {
				/* int graph factory should have a default id builder */
				assertNotNull(g.generate());
			}

			g.edges(IdBuilderInt.defaultBuilder());
			Graph<Integer, Integer> g1 = g.generate();
			assertEquals(range(1, 1 + g1.edges().size()), g1.edges());
		});
	}

	@Test
	public void testDirected() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			g.seed(0x8df4db32de040f1dL);
			g.vertices(40, IdBuilderInt.defaultBuilder());
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
	public void edgesPerStep() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			assertThrows(IllegalArgumentException.class, () -> g.edgesPerStep(-3));
			g.edgesPerStep(4);
		});
	}

	@Test
	public void tooBigEdgesPerStep() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			g.seed(0x58ca12b719e97e61L);
			g.vertices(range(10));
			g.edges(IdBuilderInt.defaultBuilder());
			g.initialCliqueSize(3);

			g.edgesPerStep(4);
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.edgesPerStep(3);
			assertNotNull(g.generate());
		});
	}

	@Test
	public void initialCliqueSize() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			assertThrows(IllegalArgumentException.class, () -> g.initialCliqueSize(-3));
			g.initialCliqueSize(4);
		});
	}

	@Test
	public void tooBigInitialClique() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			g.seed(0x58ca12b719e97e61L);
			g.vertices(range(10));
			g.edges(IdBuilderInt.defaultBuilder());

			g.initialCliqueSize(11);
			assertThrows(IllegalStateException.class, () -> g.generate());

			g.initialCliqueSize(10);
			assertNotNull(g.generate());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMutability() {
		foreachBoolConfig(intGraph -> {
			BarabasiAlbertGraphGenerator<Integer, Integer> g =
					intGraph ? new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
							: new BarabasiAlbertGraphGenerator<>();
			g.seed(0x34960acd3d3b944cL);
			g.vertices(range(40));
			g.edges(IdBuilderInt.defaultBuilder());

			Graph<Integer, Integer> gImmutable = g.generate();
			assertThrows(UnsupportedOperationException.class, () -> gImmutable.addVertex(50));

			Graph<Integer, Integer> gMutable = g.generateMutable();
			gMutable.addVertex(50);
			assertTrue(gMutable.vertices().contains(50));
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new BarabasiAlbertGraphGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		BarabasiAlbertGraphGenerator<Integer, Integer> g = new BarabasiAlbertGraphGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
