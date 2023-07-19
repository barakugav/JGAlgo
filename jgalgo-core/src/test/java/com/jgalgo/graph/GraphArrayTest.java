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

public class GraphArrayTest extends TestBase {

	private static Boolean2ObjectFunction<Graph> graphImpl() {
		return directed -> IndexGraphFactory.newUndirected().setOption("impl", "GraphArray").setDirected(directed)
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
		final long seed = 0x23321e37dfd99637L;
		GraphImplTestUtils.testClear(graphImpl(), seed);
	}

	@Test
	public void testClearEdges() {
		final long seed = 0xad0005187cebcd83L;
		GraphImplTestUtils.testClearEdges(graphImpl(), seed);
	}

	@Test
	public void testCopy() {
		final long seed = 0xf26cb0540a4874e8L;
		GraphImplTestUtils.testCopy(graphImpl(), seed);
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x14e08fffb6f5c0f4L;
		GraphImplTestUtils.testImmutableCopy(graphImpl(), seed);
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x6519a3d6cfdcaa15L;
		GraphImplTestUtils.testUndirectedMST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x93159a7004fabaabL;
		GraphImplTestUtils.testDirectedMDST(graphImpl(), seed);
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xb3bd816cf5395102L;
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		final long seed = 0x40f90cfba5f21f7cL;
		GraphImplTestUtils.testUndirectedBipartiteMatching(graphImpl(), seed);
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		final long seed = 0x6a1920e2c7e46291L;
		GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(graphImpl(), seed);
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xb49ae602bf9f9896L;
		GraphImplTestUtils.testRandOps(graphImpl(), false, seed);
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xf1d6fb75a6d8d711L;
		GraphImplTestUtils.testRandOps(graphImpl(), true, seed);
	}

}
