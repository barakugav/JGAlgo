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

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class MaximumFlowPushRelabelPartialAugmentTest extends TestBase {

	private static MaximumFlowPushRelabel algo() {
		return MaximumFlowPushRelabel.newInstancePartialAugment();
	}

	@Test
	public void testRandDiGraphsDoubleFlow() {
		final long seed = 0x6ea2d7847f258834L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphs() {
		final long seed = 0x75d59ec969d654e1L;
		MaximumFlowTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsIntFlow() {
		final long seed = 0xf9e121dc1491244cL;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsIntFlow() {
		final long seed = 0x27e1644931912b43L;
		MaximumFlowTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0xc7ccd41e0d7a1033L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSink() {
		final long seed = 0x70d9aeccf0d34d2fL;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xfaada22b9f4688e2L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0x56e705d379a480d9L;
		MaximumFlowTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testRandDiGraphsWithALotOfParallelEdges() {
		final long seed = 0xf828e81afa55e976L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testRandUGraphsWithALotOfParallelEdges() {
		final long seed = 0x9a67fc16673e1f40L;
		MaximumFlowTestUtils.testRandGraphsWithALotOfParallelEdges(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphs() {
		final long seed = 0x20ee12f8d9096e11L;
		MinimumEdgeCutSTTestUtils.testRandGraphs(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphs() {
		final long seed = 0x76eb3cafb597670eL;
		MinimumEdgeCutSTTestUtils.testRandGraphs(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsInt() {
		final long seed = 0xded4725e9ce57a70L;
		MinimumEdgeCutSTTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsInt() {
		final long seed = 0x9d44bc518b4b7c1eL;
		MinimumEdgeCutSTTestUtils.testRandGraphsInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSink() {
		final long seed = 0x12b8649a1d8f5960L;
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSink() {
		final long seed = 0xc4535d40af9e491cL;
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xcb433123c78407bbL;
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkInt() {
		final long seed = 0xce5a7a62dc907d07L;
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo(), seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x74de1fe282ebe4a9L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xdce4eb821846c33fL;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphs(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xa5f6d72eae290087L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x9021777584e8f923L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsInt(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xb0145f43577952c7L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkUsingGenericMinCutFromMaxFlow() {
		final long seed = 0xcb71315be5e0b695L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSink(algo, seed, /* directed= */ false);
	}

	@Test
	public void testMinimumCutRandDiGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x33a555d59c999438L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ true);
	}

	@Test
	public void testMinimumCutRandUGraphsMultiSourceMultiSinkIntUsingGenericMinCutFromMaxFlow() {
		final long seed = 0x90435e505edc8db1L;
		MinimumEdgeCutST algo = MinimumEdgeCutST.newFromMaximumFlow(algo());
		MinimumEdgeCutSTTestUtils.testRandGraphsMultiSourceMultiSinkInt(algo, seed, /* directed= */ false);
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
