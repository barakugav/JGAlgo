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
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class VertexPartitionTest extends TestBase {

	@Test
	public void testBlockVertices() {
		final long seed = 0xa8f42d4a6c48995dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(64);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 50).repeat(8);
		tester.addPhase().withArgs(512, 1024, 200).repeat(8);
		tester.run((n, m, k) -> {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());

					for (int b = 0; b < k; b++) {
						final int b0 = b;
						Set<Integer> expected = g.vertices().stream().filter(v -> partition.vertexBlock(v) == b0)
								.collect(Collectors.toSet());
						Set<Integer> actual = partition.blockVertices(b);
						assertEquals(expected, actual);
					}
				}
			}
		});
	}

	@Test
	public void testBlockEdges() {
		final long seed = 0xf5281151efcb468eL;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(64);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 50).repeat(8);
		tester.addPhase().withArgs(512, 1024, 200).repeat(8);
		tester.run((n, m, k) -> {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());

					for (int b = 0; b < k; b++) {
						final int b0 = b;
						Set<Integer> expected =
								g.edges().stream()
										.filter(e -> partition.vertexBlock(g.edgeSource(e)) == b0
												&& partition.vertexBlock(g.edgeTarget(e)) == b0)
										.collect(Collectors.toSet());
						Set<Integer> actual = partition.blockEdges(b);
						assertEquals(expected, actual);
					}
				}
			}
		});
	}

	@Test
	public void testCrossEdges() {
		final long seed = 0xd8458292eb56ed9L;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(64);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 30).repeat(8);
		tester.run((n, m, k) -> {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());

					Graph<Integer, Integer> blocksGraph = partition.blocksGraph(true, true);

					for (int b1 = 0; b1 < k; b1++) {
						for (int b2 = 0; b2 < k; b2++) {
							final int b10 = b1;
							final int b20 = b2;
							Set<Integer> expected;
							if (directed) {
								expected = g.edges().stream()
										.filter(e -> partition.vertexBlock(g.edgeSource(e)) == b10
												&& partition.vertexBlock(g.edgeTarget(e)) == b20)
										.collect(Collectors.toSet());
							} else {
								expected = g.edges().stream()
										.filter(e -> (partition.vertexBlock(g.edgeSource(e)) == b10
												&& partition.vertexBlock(g.edgeTarget(e)) == b20)
												|| (partition.vertexBlock(g.edgeSource(e)) == b20
														&& partition.vertexBlock(g.edgeTarget(e)) == b10))
										.collect(Collectors.toSet());
							}
							Set<Integer> actual = partition.crossEdges(b1, b2);
							assertEquals(expected, actual);

							assertEquals(expected, blocksGraph.getEdges(Integer.valueOf(b1), Integer.valueOf(b2)));
						}
					}
				}
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testIsPartition() {
		final long seed = 0x30a53df6d88b87d5L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(64);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 30).repeat(8);
		tester.run((n, m, k) -> {
			for (boolean directed : BooleanList.of(false, true)) {
				for (boolean index : BooleanList.of(false, true)) {
					Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());

					Object2IntMap<Integer> partition1 = randPartitionMap(g, k, seedGen.nextSeed());
					assertTrue(VertexPartition.isPartition(g, partition1::getInt));

					Object2IntMap<Integer> partition2 = new Object2IntOpenHashMap<>(partition1);
					partition2.put(Graphs.randVertex(g, rand), -1 - rand.nextInt(5));
					assertFalse(VertexPartition.isPartition(g, partition2::getInt));

					Object2IntMap<Integer> partition3 = new Object2IntOpenHashMap<>(partition1);
					int block = rand.nextInt(k);
					partition3.replaceAll((v, b) -> b != block ? b : k);
					assertFalse(VertexPartition.isPartition(g, partition3::getInt));
				}
			}
		});
	}

	private static <V, E> VertexPartition<V, E> randPartition(Graph<V, E> g, int k, long seed) {
		return VertexPartition.fromMap(g, randPartitionMap(g, k, seed));
	}

	private static <V, E> Object2IntMap<V> randPartitionMap(Graph<V, E> g, int k, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		final int n = g.vertices().size();
		if (k > n)
			throw new IllegalArgumentException();
		Object2IntMap<V> partition = new Object2IntOpenHashMap<>();

		List<V> vs = new ArrayList<>(g.vertices());
		Collections.shuffle(vs, rand);
		int idx = 0;
		for (; idx < k; idx++)
			partition.put(vs.get(idx), idx);
		for (; idx < n; idx++)
			partition.put(vs.get(idx), rand.nextInt(k));
		return partition;
	}

	private static Graph<Integer, Integer> randGraph(int n, int m, boolean directed, boolean index, long seed) {
		Graph<Integer, Integer> g = new RandomGraphBuilder(seed).n(n).m(m).directed(directed).parallelEdges(true)
				.selfEdges(true).cycles(true).connected(false).build();
		return index ? g.indexGraph() : g;
	}

}
