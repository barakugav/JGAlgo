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

import static com.jgalgo.internal.util.Range.range;
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
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class VertexPartitionTest extends TestBase {

	@Test
	public void testBlockVertices() {
		final long seed = 0xa8f42d4a6c48995dL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(64);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 50).repeat(8);
		tester.addPhase().withArgs(512, 1024, 200).repeat(8);
		tester.run((n, m, k) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());

				for (int b : range(k)) {
					Set<Integer> expected = g
							.vertices()
							.stream()
							.filter(v -> partition.vertexBlock(v) == b)
							.collect(Collectors.toSet());
					Set<Integer> actual = partition.blockVertices(b);
					assertEquals(expected, actual);

					/* test .contains() */
					for (Integer v : g.vertices())
						assertEqualsBool(expected.contains(v), actual.contains(v));
					for (int i = 0; i < 5; i++) {
						Integer nonVertex;
						do {
							nonVertex = Integer.valueOf(rand.nextInt());
						} while (g.vertices().contains(nonVertex));
						assertFalse(actual.contains(nonVertex));
					}
				}
			});
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
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());
				final Random rand = new Random(seedGen.nextSeed());

				Graph<Integer, Integer> blocksGraph = partition.blocksGraph(true, true);
				Graph<Integer, Integer> blocksGraphNonSelf = partition.blocksGraph(true, false);

				for (int b : range(k)) {
					Set<Integer> expected = g
							.edges()
							.stream()
							.filter(e -> partition.vertexBlock(g.edgeSource(e)) == b
									&& partition.vertexBlock(g.edgeTarget(e)) == b)
							.collect(Collectors.toSet());
					Set<Integer> actual = partition.blockEdges(b);
					assertEquals(expected, actual);

					/* test .contains() */
					for (Integer e : g.edges())
						assertEqualsBool(expected.contains(e), actual.contains(e));
					for (int i = 0; i < 5; i++) {
						Integer nonEdge;
						do {
							nonEdge = Integer.valueOf(rand.nextInt());
						} while (g.edges().contains(nonEdge));
						assertFalse(actual.contains(nonEdge));
					}

					/* test blocksGraph */
					assertEquals(expected, blocksGraph.getEdges(Integer.valueOf(b), Integer.valueOf(b)));
					assertFalse(blocksGraphNonSelf.containsEdge(Integer.valueOf(b), Integer.valueOf(b)));
					assertEquals(0, blocksGraphNonSelf.getEdges(Integer.valueOf(b), Integer.valueOf(b)).size());
				}
			});
		});
	}

	@Test
	public void testCrossEdges() {
		final long seed = 0xd8458292eb56ed9L;
		final SeedGenerator seedGen = new SeedGenerator(seed);

		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32, 3).repeat(128);
		tester.addPhase().withArgs(16, 32, 6).repeat(128);
		tester.addPhase().withArgs(31, 76, 27).repeat(7);
		tester.addPhase().withArgs(64, 256, 7).repeat(64);
		tester.addPhase().withArgs(64, 256, 28).repeat(15);
		tester.addPhase().withArgs(512, 1024, 5).repeat(8);
		tester.addPhase().withArgs(512, 1024, 30).repeat(2);
		tester.run((n, m, k) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = randGraph(n, m, directed, index, seedGen.nextSeed());
				VertexPartition<Integer, Integer> partition = randPartition(g, k, seedGen.nextSeed());

				Graph<Integer, Integer> blocksGraph = partition.blocksGraph(true, true);
				Graph<Integer, Integer> blocksGraphNonParallel = partition.blocksGraph(false, true);

				for (int b1 : range(k)) {
					for (int b2 : range(k)) {
						Set<Integer> expected;
						if (directed) {
							expected = g
									.edges()
									.stream()
									.filter(e -> partition.vertexBlock(g.edgeSource(e)) == b1
											&& partition.vertexBlock(g.edgeTarget(e)) == b2)
									.collect(Collectors.toSet());
						} else {
							expected = g
									.edges()
									.stream()
									.filter(e -> (partition.vertexBlock(g.edgeSource(e)) == b1
											&& partition.vertexBlock(g.edgeTarget(e)) == b2)
											|| (partition.vertexBlock(g.edgeSource(e)) == b2
													&& partition.vertexBlock(g.edgeTarget(e)) == b1))
									.collect(Collectors.toSet());
						}
						Set<Integer> actual = partition.crossEdges(b1, b2);
						assertEquals(expected, actual);

						assertEquals(expected, blocksGraph.getEdges(Integer.valueOf(b1), Integer.valueOf(b2)));
						if (expected.isEmpty()) {
							assertFalse(blocksGraphNonParallel.containsEdge(Integer.valueOf(b1), Integer.valueOf(b2)));
						} else {
							assertEquals(1,
									blocksGraphNonParallel.getEdges(Integer.valueOf(b1), Integer.valueOf(b2)).size());
						}
					}
				}
			});
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
			foreachBoolConfig((directed, index) -> {
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
			});
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
		Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seed);
		return index ? g.indexGraph() : g;
	}

}
