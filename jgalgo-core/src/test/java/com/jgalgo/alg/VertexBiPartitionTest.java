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

package com.jgalgo.alg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class VertexBiPartitionTest extends TestBase {

	@Test
	public void testBlockVertices() {
		final long seed = 0x6b49d9148040928bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexBiPartition<Integer, Integer> partition = randPartition(g, seedGen.nextSeed());

				Set<Integer> leftExpected = g.vertices().stream().filter(partition::isLeft).collect(Collectors.toSet());
				Set<Integer> rightExpected =
						g.vertices().stream().filter(partition::isRight).collect(Collectors.toSet());
				assertEquals(leftExpected, partition.blockVertices(0));
				assertEquals(leftExpected, partition.leftVertices());
				assertEquals(rightExpected, partition.blockVertices(1));
				assertEquals(rightExpected, partition.rightVertices());
			});
		});
	}

	@Test
	public void testBlockEdges() {
		final long seed = 0x3fb7d2388b5c744bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexBiPartition<Integer, Integer> partition = randPartition(g, seedGen.nextSeed());

				Set<Integer> leftExpected = g
						.edges()
						.stream()
						.filter(e -> partition.isLeft(g.edgeSource(e)) && partition.isLeft(g.edgeTarget(e)))
						.collect(Collectors.toSet());
				Set<Integer> rightExpected = g
						.edges()
						.stream()
						.filter(e -> partition.isRight(g.edgeSource(e)) && partition.isRight(g.edgeTarget(e)))
						.collect(Collectors.toSet());
				assertEquals(leftExpected, partition.blockEdges(0));
				assertEquals(leftExpected, partition.leftEdges());
				assertEquals(rightExpected, partition.blockEdges(1));
				assertEquals(rightExpected, partition.rightEdges());
			});
		});
	}

	@Test
	public void testCrossEdges() {
		final long seed = 0x135c97f5e3b12700L;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexBiPartition<Integer, Integer> partition = randPartition(g, seedGen.nextSeed());

				Graph<Integer, Integer> blocksGraph = partition.blocksGraph(true, true);

				Set<Integer> crossEdgesLeftRightExpected;
				Set<Integer> crossEdgesRightLeftExpected;
				if (directed) {
					crossEdgesLeftRightExpected = g
							.edges()
							.stream()
							.filter(e -> partition.isLeft(g.edgeSource(e)) && partition.isRight(g.edgeTarget(e)))
							.collect(Collectors.toSet());
					crossEdgesRightLeftExpected = g
							.edges()
							.stream()
							.filter(e -> partition.isRight(g.edgeSource(e)) && partition.isLeft(g.edgeTarget(e)))
							.collect(Collectors.toSet());
				} else {
					crossEdgesLeftRightExpected = crossEdgesRightLeftExpected = g
							.edges()
							.stream()
							.filter(e -> (partition.isLeft(g.edgeSource(e)) && partition.isRight(g.edgeTarget(e)))
									|| (partition.isRight(g.edgeSource(e)) && partition.isLeft(g.edgeTarget(e))))
							.collect(Collectors.toSet());
				}

				assertEquals(crossEdgesLeftRightExpected, partition.crossEdges(0, 1));
				assertEquals(crossEdgesLeftRightExpected, partition.crossEdges());
				assertEquals(crossEdgesRightLeftExpected, partition.crossEdges(1, 0));

				assertEquals(crossEdgesLeftRightExpected, blocksGraph.getEdges(Integer.valueOf(0), Integer.valueOf(1)));
				assertEquals(crossEdgesRightLeftExpected, blocksGraph.getEdges(Integer.valueOf(1), Integer.valueOf(0)));
			});
		});
	}

	@Test
	public void testIsPartition() {
		final long seed = 0x6a861644d20a2822L;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());

				Map<Integer, Boolean> partition = randPartitionMap(g, seedGen.nextSeed());
				assertTrue(VertexBiPartition.isPartition(g, partition::get));

				assertFalse(VertexBiPartition.isPartition(g, v -> true));
				assertFalse(VertexBiPartition.isPartition(g, v -> false));
			});
		});
	}

	private static <V, E> VertexBiPartition<V, E> randPartition(Graph<V, E> g, long seed) {
		return VertexBiPartition.fromMap(g, randPartitionMap(g, seed));
	}

	@SuppressWarnings("boxing")
	private static <V, E> Map<V, Boolean> randPartitionMap(Graph<V, E> g, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		final int n = g.vertices().size();
		if (2 > n)
			throw new IllegalArgumentException();
		Map<V, Boolean> partition = new Object2ObjectOpenHashMap<>();

		List<V> vs = new ArrayList<>(g.vertices());
		Collections.shuffle(vs, rand);
		int idx = 0;
		partition.put(vs.get(idx++), true);
		partition.put(vs.get(idx++), false);
		for (; idx < n; idx++)
			partition.put(vs.get(idx), rand.nextBoolean());
		return partition;
	}

	private static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, boolean index, long seed) {
		Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seed);
		return index ? g.indexGraph() : g;
	}

}
