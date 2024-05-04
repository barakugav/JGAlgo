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

public class MaximumFlowPushRelabelToFrontTest extends TestBase {

	private static MaximumFlowPushRelabel algo() {
		return MaximumFlowPushRelabel.newInstanceMoveToFront();
	}

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0x8fb191d57a090f45L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphs() {
		final long seed = 0xc2dc26d60fe02059L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0x3d296bd5e39fbefbL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x76f63550330a6958L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x7b39aabc186d9821L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xd63fd20c29ff6e3eL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x33437ee07c8e5182L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x9eaa37817b47861aL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0x891d8d87cdc4f0abL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0xbf8c896deea4008eL;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0xb4adedfb5d675969L;
		MinimumEdgeCutStTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x709c5bb121a78e30L;
		MinimumEdgeCutStTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0x5817e5c904a5dad1L;
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0x4e667179de4612b4L;
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x34b546da15a87199L;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xf76920525136ad47L;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xd60598090dd98da9L;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xe6cc06bfd27b968eL;
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x2d8b2f167cdc5e50L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc07645520bcef1f2L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc833101540b8e5f1L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x8628e6c6741acd1bL;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x116caa98e86e6c67L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xc07f7f68bdd3136cL;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xf73caac34b9b6997L;
		MinimumEdgeCutSt algo = MinimumEdgeCutSt.newFromMaximumFlow(algo());
		MinimumEdgeCutStTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x6f017f4a5e0204e3L;
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
