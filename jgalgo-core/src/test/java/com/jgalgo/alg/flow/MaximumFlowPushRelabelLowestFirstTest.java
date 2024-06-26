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
import com.jgalgo.alg.connect.MinimumEdgeCutSt;
import com.jgalgo.alg.connect.MinimumEdgeCutStTestUtils;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class MaximumFlowPushRelabelLowestFirstTest extends TestBase {

	private static MaximumFlowPushRelabel algo() {
		return MaximumFlowPushRelabel.newInstanceLowestFirst();
	}

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0xa3401ed1fd71bd97L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphs() {
		final long seed = 0x7427fa0583f971b3L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0x0204011e1b393aaaL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x9bdc74a39bba086dL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x3132bbecc9e1f710L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xe1284305042c216eL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x967e06859b8261d5L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xd6f874b4a618cc1cL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0x1fc5bc8f0584706eL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0xd431426d4c5a8eb7L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x13996322a27d8bdL;
		MinimumEdgeCutStTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x3a34a17f7cc8c2edL;
		MinimumEdgeCutStTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0x9a378c8dd98b3bceL;
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0x7ba018b10dbab024L;
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x224e679e9669bc0bL;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSink() {
		final long seed = 0x42a9f16c24c9262aL;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x9eb228362cc4bdfaL;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x8c94a87d3c23f210L;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xb3355e4e0b00de3dL;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x319b9f9dd6264b29L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5fc7b22045f53253L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x33efe39c59370996L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x9d516b28b5f8d886L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x7a4761442e692788L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x3b706dedb8e9bbbeL;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x5004bf8743477e30L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ false);
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
