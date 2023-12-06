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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.Trees;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
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

			UniformTreeGenerator<Integer, Integer> gen =
					rand.nextBoolean() ? UniformTreeGenerator.newInstance() : UniformTreeGenerator.newIntInstance();
			gen.setSeed(seedGen.nextSeed());
			gen.setVertices(vertices);
			gen.setEdges(new AtomicInteger()::getAndIncrement);

			Graph<Integer, Integer> g = gen.generate();
			assertTrue(Trees.isTree(g));
		});
	}

	@Test
	public void testVertices() {
		final long seed = 0xacd3a42fcece4646L;
		UniformTreeGenerator<String, Integer> g = UniformTreeGenerator.newInstance();
		g.setSeed(seed);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<String> vertices = range(7).mapToObj(String::valueOf).collect(Collectors.toSet());
		g.setVertices(vertices);
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> String.valueOf(vertexId.getAndIncrement()));
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
		assertEquals(Set.of("0", "1", "2", "3"), g.generate().vertices());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testVerticesIntGraph() {
		final long seed = 0xdb4a9a7de48830d5L;
		UniformTreeGenerator<Integer, Integer> g = UniformTreeGenerator.newIntInstance();
		g.setSeed(seed);
		g.setEdges(new AtomicInteger()::getAndIncrement);

		/* vertices were not set yet */
		assertThrows(IllegalStateException.class, () -> g.generate());

		Set<Integer> vertices = range(7);
		g.setVertices(range(7));
		assertEquals(vertices, g.generate().vertices());
		assertEquals(vertices, g.generate().vertices());

		AtomicInteger vertexId = new AtomicInteger();
		g.setVertices(4, () -> vertexId.getAndIncrement());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
		assertEquals(Set.of(0, 1, 2, 3), g.generate().vertices());
	}

	@Test
	public void testEdges() {
		foreachBoolConfig(intGraph -> {
			for (int n : IntList.of(0, 1, 7, 80)) {
				final long seed = 0x8153d76d685a56d2L;
				UniformTreeGenerator<Integer, Integer> g0 =
						intGraph ? UniformTreeGenerator.newIntInstance() : UniformTreeGenerator.newInstance();
				g0.setSeed(seed);
				g0.setVertices(range(n));

				/* edges were not set yet */
				assertThrows(IllegalStateException.class, () -> g0.generate());

				g0.setEdges(new AtomicInteger()::getAndIncrement);
				Graph<Integer, Integer> g = g0.generate();

				assertEqualsBool(intGraph, g instanceof IntGraph);

				int expectedNumEdges = n == 0 ? 0 : n - 1;
				assertEquals(range(expectedNumEdges), g.edges());
			}
		});
	}

}
