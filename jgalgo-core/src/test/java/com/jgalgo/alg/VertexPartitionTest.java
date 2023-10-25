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
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class VertexPartitionTest extends TestBase {

	@Test
	public void testBlockVertices() {
		final long seed = 0;
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
					Graph g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition partition = randPartition(g, k, seedGen.nextSeed());

					for (int b = 0; b < k; b++) {
						final int b0 = b;
						IntSet expected = new IntOpenHashSet(
								g.vertices().intStream().filter(v -> partition.vertexBlock(v) == b0).toArray());
						IntSet actual = partition.blockVertices(b);
						assertEquals(expected, actual);
					}
				}
			}
		});
	}

	@Test
	public void testBlockEdges() {
		final long seed = 0;
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
					Graph g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition partition = randPartition(g, k, seedGen.nextSeed());

					for (int b = 0; b < k; b++) {
						final int b0 = b;
						IntSet expected = new IntOpenHashSet(
								g.edges().intStream().filter(e -> partition.vertexBlock(g.edgeSource(e)) == b0
										&& partition.vertexBlock(g.edgeTarget(e)) == b0).toArray());
						IntSet actual = partition.blockEdges(b);
						assertEquals(expected, actual);
					}
				}
			}
		});
	}

	@Test
	public void testCrossEdges() {
		final long seed = 0;
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
					Graph g = randGraph(n, m, directed, index, seedGen.nextSeed());
					VertexPartition partition = randPartition(g, k, seedGen.nextSeed());

					for (int b1 = 0; b1 < k; b1++) {
						for (int b2 = 0; b2 < k; b2++) {
							final int b10 = b1;
							final int b20 = b2;
							IntSet expected;
							if (directed) {
								expected =
										new IntOpenHashSet(
												g.edges().intStream()
														.filter(e -> partition.vertexBlock(g.edgeSource(e)) == b10
																&& partition.vertexBlock(g.edgeTarget(e)) == b20)
														.toArray());
							} else {
								expected = new IntOpenHashSet(g.edges().intStream()
										.filter(e -> (partition.vertexBlock(g.edgeSource(e)) == b10
												&& partition.vertexBlock(g.edgeTarget(e)) == b20)
												|| (partition.vertexBlock(g.edgeSource(e)) == b20
														&& partition.vertexBlock(g.edgeTarget(e)) == b10))
										.toArray());
							}
							IntSet actual = partition.crossEdges(b1, b2);
							assertEquals(expected, actual);
						}
					}
				}
			}
		});
	}

	private static VertexPartition randPartition(Graph g, int k, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		final int n = g.vertices().size();
		if (k > n)
			throw new IllegalArgumentException();
		Int2IntMap partition = new Int2IntOpenHashMap();

		int[] vs = g.vertices().toIntArray();
		IntArrays.shuffle(vs, rand);
		int idx = 0;
		for (; idx < k; idx++)
			partition.put(vs[idx], idx);
		for (; idx < n; idx++)
			partition.put(vs[idx], rand.nextInt(k));
		return VertexPartition.fromMap(g, partition);
	}

	private static Graph randGraph(int n, int m, boolean directed, boolean index, long seed) {
		Graph g = new RandomGraphBuilder(seed).n(n).m(m).directed(directed).parallelEdges(true).selfEdges(true)
				.cycles(true).connected(false).build();
		return index ? g.indexGraph() : g;
	}

}
