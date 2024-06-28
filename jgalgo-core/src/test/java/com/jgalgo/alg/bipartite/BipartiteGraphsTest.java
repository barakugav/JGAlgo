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
package com.jgalgo.alg.bipartite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.internal.util.TestBase;

public class BipartiteGraphsTest extends TestBase {

	@Test
	public void isBipartitePositive() {
		final long seed = 0xacba69f86de907d4L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(7, 9, 32).repeat(128);
		tester.addPhase().withArgs(37, 11, 256).repeat(64);
		tester.addPhase().withArgs(200, 315, 1024).repeat(8);
		tester.run((sn, tn, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = GraphsTestUtils.randBipartiteGraph(sn, tn, m, directed, seedGen.nextSeed());
				g.removeVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
				g = index ? g.indexGraph() : g;

				assertTrue(BipartiteGraphs.isBipartite(g));
				assertEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));

				assertNotEquals(Optional.empty(), BipartiteGraphs.findPartition(g, true));
				assertNotEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));
			});
		});
	}

	@Test
	public void isBipartiteNegative() {
		final long seed = 0x5dbd08ce273530bdL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
				g = index ? g.indexGraph() : g;
				/* its possible we will generate a bipartite graph, but its not going to happen */
				assertFalse(BipartiteGraphs.isBipartite(g));
			});
		});
	}

	@Test
	public void findPartitionPositive() {
		final long seed = 0xff8f2dd6005efde4L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(7, 9, 32).repeat(128);
		tester.addPhase().withArgs(37, 11, 256).repeat(64);
		tester.addPhase().withArgs(200, 315, 1024).repeat(8);
		tester.run((sn, tn, m) -> {
			foreachBoolConfig((directed, index, addPartitionWeight) -> {
				Graph<Integer, Integer> g0 =
						GraphsTestUtils.randBipartiteGraph(sn, tn, m, directed, seedGen.nextSeed());
				g0.removeVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
				Graph<Integer, Integer> g = index ? g0.indexGraph() : g0;

				Optional<VertexBiPartition<Integer, Integer>> partitionOptional;
				if (addPartitionWeight) {
					partitionOptional = BipartiteGraphs.findPartition(g, true);
				} else {
					partitionOptional = BipartiteGraphs.findPartition(g);
				}
				assertTrue(partitionOptional.isPresent());
				VertexBiPartition<Integer, Integer> partition = partitionOptional.get();
				assertNotNull(partition);

				assertTrue(VertexBiPartition.isPartition(g, partition::isLeft));
				g.edges().stream().forEach(e -> {
					Integer u = g.edgeSource(e), v = g.edgeTarget(e);
					assertNotEqualsBool(partition.isLeft(u), partition.isLeft(v));
				});

				if (addPartitionWeight) {
					WeightsBool<Integer> partitionWeights =
							g.verticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
					assertNotNull(partitionWeights);
					g.edges().stream().forEach(e -> {
						Integer u = g.edgeSource(e), v = g.edgeTarget(e);
						assertNotEqualsBool(partitionWeights.get(u), partitionWeights.get(v));
					});
					assertNotEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));

				} else {
					assertNull(g.verticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey));
					assertEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));
				}
			});
		});
	}

	@Test
	public void findPartitionNegative() {
		final long seed = 0x2627a41c27c058a0L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(512, 1024).repeat(8);
		tester.run((n, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, seedGen.nextSeed());
				g = index ? g.indexGraph() : g;
				/* its possible we will generate a bipartite graph, but its not going to happen */
				assertEquals(Optional.empty(), BipartiteGraphs.findPartition(g));
			});
		});
	}

	@Test
	public void findPartitionOverrideExistingOne() {
		final long seed = 0xfe653b273874e8e9L;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randBipartiteGraph(20, 31, 200, false, rand.nextLong());

		/* random (probably invalid) existing partition */
		WeightsBool<Integer> existingPartition = g.verticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		for (Integer v : g.vertices())
			existingPartition.set(v, rand.nextBoolean());

		Optional<VertexBiPartition<Integer, Integer>> partitionOptional = BipartiteGraphs.findPartition(g, true);
		assertTrue(partitionOptional.isPresent());
		VertexBiPartition<Integer, Integer> partition = partitionOptional.get();
		assertNotNull(partition);

		/* assert returned partition uses the existing vertices weights */
		assertEquals(Set.of(BipartiteGraphs.VertexBiPartitionWeightKey), g.verticesWeightsKeys());
		for (Integer v : g.vertices())
			assertEqualsBool(partition.isLeft(v), existingPartition.get(v));
	}

	@Test
	public void findPartitionOverrideExistingOneWrongType() {
		final long seed = 0xa8e10cc34aa5a990L;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randBipartiteGraph(20, 31, 200, false, rand.nextLong());
		g.removeVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, int.class);

		assertThrows(IllegalArgumentException.class, () -> BipartiteGraphs.findPartition(g, true));
	}

	@Test
	public void getExistingPartition() {
		final long seed = 0xb0a049416edf2b2fL;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randBipartiteGraph(20, 31, 200, false, rand.nextLong());

		Optional<VertexBiPartition<Integer, Integer>> partitionOptional = BipartiteGraphs.getExistingPartition(g);
		assertTrue(partitionOptional.isPresent());
		VertexBiPartition<Integer, Integer> partition = partitionOptional.get();
		assertNotNull(partition);

		assertTrue(VertexBiPartition.isPartition(g, partition::isLeft));
		g.edges().stream().forEach(e -> {
			Integer u = g.edgeSource(e), v = g.edgeTarget(e);
			assertNotEqualsBool(partition.isLeft(u), partition.isLeft(v));
		});
	}

	@Test
	public void getExistingPartitionIntGraph() {
		final long seed = 0xb0a049416edf2b2fL;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g0 = GraphsTestUtils.randBipartiteGraph(20, 31, 200, false, rand.nextLong());
		IntGraph g = IntGraphFactory.undirected().newCopyOf(g0, true, true);

		Optional<IVertexBiPartition> partitionOptional = BipartiteGraphs.getExistingPartition(g);
		assertTrue(partitionOptional.isPresent());
		IVertexBiPartition partition = partitionOptional.get();
		assertNotNull(partition);

		assertTrue(IVertexBiPartition.isPartition(g, partition::isLeft));
		g.edges().intStream().forEach(e -> {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			assertNotEqualsBool(partition.isLeft(u), partition.isLeft(v));
		});
	}

	@Test
	public void getExistingPartitionWrongType() {
		final long seed = 0x2f69c1700d49e080L;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randBipartiteGraph(20, 31, 200, false, rand.nextLong());
		g.removeVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);
		g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, int.class);

		assertThrows(IllegalArgumentException.class, () -> BipartiteGraphs.getExistingPartition(g));
	}

}
