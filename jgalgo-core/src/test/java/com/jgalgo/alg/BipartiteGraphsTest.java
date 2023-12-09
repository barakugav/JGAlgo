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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;

public class BipartiteGraphsTest extends TestBase {

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
	public void isBipartitePositive() {
		final long seed = 0xacba69f86de907d4L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(7, 9, 32).repeat(128);
		tester.addPhase().withArgs(37, 11, 256).repeat(64);
		tester.addPhase().withArgs(200, 315, 1024).repeat(8);
		tester.run((sn, tn, m) -> {
			foreachBoolConfig((directed, index) -> {
				Graph<Integer, Integer> g =
						GraphsTestUtils.randBipartiteGraph(sn, tn, m, directed, index, seedGen.nextSeed());
				g.removeVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey);

				assertTrue(BipartiteGraphs.isBipartite(g));
				assertEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));

				assertNotEquals(Optional.empty(), BipartiteGraphs.findPartition(g, true));
				assertNotEquals(Optional.empty(), BipartiteGraphs.getExistingPartition(g));
			});
		});
	}

}
