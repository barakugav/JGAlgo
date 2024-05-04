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

package com.jgalgo.alg.flow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.connect.MinimumEdgeCutSt2TestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class MaximumFlowDinicDynamicTreesTest extends TestBase {

	private static MaximumFlowDinicDynamicTrees algo() {
		return new MaximumFlowDinicDynamicTrees();
	}

	@Test
	public void testRandDiGraphs() {
		final long seed = 0x67b60b1ffd6fee78L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphs() {
		final long seed = 0xc145e91e4b75cdd2L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0xee0121506177386L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x38831cada35583c6L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x7e782b2ad78ef0e9L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xd47e3e561aa9ea7aL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xf584fec83bdb004cL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x5be23a963d0b15a8L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0xbc0b85c1dba82f15L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0xec2534341da6fbeeL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0x57895831bc5f0b59L;
		MinimumEdgeCutSt2TestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0x7452b9f0434abacbL;
		MinimumEdgeCutSt2TestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x3bed83f0d259a3d6L;
		MinimumEdgeCutSt2TestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x1615a1ce98d0f5c9L;
		MinimumEdgeCutSt2TestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x9ecd5d10b7d6641eL;
		MinimumEdgeCutSt2TestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSink() {
		final long seed = 0x3f158fb2fefc508aL;
		MinimumEdgeCutSt2TestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xe614027b65b85dc9L;
		MinimumEdgeCutSt2TestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x59960e1c624e979aL;
		MinimumEdgeCutSt2TestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@SuppressWarnings("boxing")
	@Test
	public void sameSourceSink() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		MaximumFlow algo = algo();
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, List.of(), List.of(0, 1)));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, List.of(0, 1), List.of(0)));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, List.of(0, 0), List.of(1)));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, List.of(0), List.of(1, 1)));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, null, List.of(0), List.of(1, 0)));
	}

	@SuppressWarnings("boxing")
	@Test
	public void negativeCapacity() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		MaximumFlow algo = algo();
		IWeightFunction w1 = e -> -1;
		IWeightFunctionInt w2 = e -> -1;
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, w1, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumFlow(g, w2, 0, 1));
	}

}
