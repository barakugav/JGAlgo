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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.tree.Trees;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class UniformTreeGeneratorTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0xa56992bd4a5dd3e6L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3).repeat(32);
		tester.addPhase().withArgs(6).repeat(32);
		tester.addPhase().withArgs(16).repeat(8);
		tester.addPhase().withArgs(1257).repeat(4);
		tester.run(n -> {
			IntSet vertices = new IntOpenHashSet();
			while (vertices.size() < n)
				vertices.add(rand.nextInt(2 * n));

			Graph<Integer, Integer> g = (rand.nextBoolean() ? new UniformTreeGenerator<Integer, Integer>()
					: new UniformTreeGenerator<>(IntGraphFactory.undirected()))
							.seed(seedGen.nextSeed())
							.vertices(vertices)
							.edges(IdBuilderInt.defaultBuilder())
							.generate();
			assertTrue(Trees.isTree(g));
		});
	}

	@Test
	public void vertices() {
		final long seed = 0xacd3a42fcece4646L;
		UniformTreeGenerator<String, Integer> g = new UniformTreeGenerator<>();
		g.seed(seed);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = range(7).mapToObj(String::valueOf).collect(Collectors.toSet());
		g.vertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

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
		final long seed = 0xdb4a9a7de48830d5L;
		UniformTreeGenerator<Integer, Integer> g = new UniformTreeGenerator<>(IntGraphFactory.undirected());
		g.seed(seed);
		g.edges(IdBuilderInt.defaultBuilder());

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = range(7);
		g.vertices(range(7));
		assertEquals(vertices, g.generate().vertices());
		/* assert the vertices are reused */
		assertEquals(vertices, g.generate().vertices());

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
			for (int n : IntList.of(0, 1, 7, 80)) {
				final long seed = 0x8153d76d685a56d2L;
				UniformTreeGenerator<Integer, Integer> g0 =
						intGraph ? new UniformTreeGenerator<>(IntGraphFactory.undirected())
								: new UniformTreeGenerator<>();
				g0.seed(seed);
				g0.vertices(range(n));

				/* edges were not set yet */
				if (!intGraph) {
					assertThrows(IllegalStateException.class, () -> g0.generate());
				} else {
					/* int graph factory should have a default id builder */
					assertNotNull(g0.generate());
				}

				g0.edges(IdBuilderInt.defaultBuilder());
				Graph<Integer, Integer> g = g0.generate();

				assertEqualsBool(intGraph, g instanceof IntGraph);

				int expectedNumEdges = n == 0 ? 0 : n - 1;
				assertEquals(range(1, 1 + expectedNumEdges), g.edges());
			}
		});
	}

	@Test
	public void graphFactory() {
		assertNotNull(new UniformTreeGenerator<>().graphFactory());
		GraphFactory<Integer, Integer> gf = GraphFactory.undirected();
		UniformTreeGenerator<Integer, Integer> g = new UniformTreeGenerator<>(gf);
		assertTrue(gf == g.graphFactory());
	}

}
