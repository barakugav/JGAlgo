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

package com.jgalgo.graph;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;

public class GraphHashmapTest extends TestBase {

	private static Boolean2ObjectFunction<IntGraph> graphImpl() {
		return directed -> IndexGraphFactory.newUndirected().setOption("impl", "hashtable").setDirected(directed)
				.newGraph();
	}

	@Test
	public void testVertexAdd() {
		GraphImplTestUtils.testVertexAdd(graphImpl());
	}

	@Test
	public void testAddEdge() {
		GraphImplTestUtils.testAddEdge(graphImpl());
	}

	@Test
	public void testGetEdge() {
		GraphImplTestUtils.testGetEdge(graphImpl());
	}

	@Test
	public void testGetEdgesOutIn() {
		GraphImplTestUtils.testGetEdgesOutIn(graphImpl());
	}

	@Test
	public void testGetEdgesSourceTarget() {
		GraphImplTestUtils.testGetEdgesSourceTarget(graphImpl());
	}

	@Test
	public void testEdgeIter() {
		GraphImplTestUtils.testEdgeIter(graphImpl());
	}

	@Test
	public void testDegree() {
		GraphImplTestUtils.testDegree(graphImpl());
	}

	@Test
	public void testClear() {
		final long seed = 0xfeee3061e66c04c7L;
		GraphImplTestUtils.testClear(graphImpl(), seed);
	}

	@Test
	public void testClearEdges() {
		final long seed = 0xbfc4ee3bc6145db5L;
		GraphImplTestUtils.testClearEdges(graphImpl(), seed);
	}

	@Test
	public void testCopy() {
		final long seed = 0xa3d1a609b6ad68e7L;
		GraphImplTestUtils.testCopy(graphImpl(), seed);
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x8d8eb7bf35defaecL;
		GraphImplTestUtils.testCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0xe11e9cb0706b5ddcL;
		GraphImplTestUtils.testImmutableCopy(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0xaa3e9481acc6d705L;
		GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(), seed);
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0xc0778e0a295e533fL;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x7b7cfd4d9b348c87L;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xdc3e747dc97e0566L;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		final long seed = 0x3079d44f4dd5ee62L;
		GraphImplTestUtils.testUndirectedBipartiteMatching(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		final long seed = 0xbfe47c2603ab25a9L;
		GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xcc71d4106a85283eL;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xe1f4bdbf72d9b0b7L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
