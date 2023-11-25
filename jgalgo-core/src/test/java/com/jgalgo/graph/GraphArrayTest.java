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

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> IntGraphFactory.newUndirected().setOption("impl", selfEdges ? "array-selfedges" : "array")
				.setDirected(directed).newGraph();
	}

	@Test
	public void testVertexAdd() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testVertexAdd(graphImpl(selfEdges));
		});
	}

	@Test
	public void testAddEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testAddEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testGetEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdgesOutIn() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testGetEdgesOutIn(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdgesSourceTarget() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testGetEdgesSourceTarget(graphImpl(selfEdges));
		});
	}

	@Test
	public void testEdgeIter() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testEdgeIter(graphImpl(selfEdges));
		});
	}

	@Test
	public void testDegree() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDegree(graphImpl(selfEdges));
		});
	}

	@Test
	public void testClear() {
		final long seed = 0x23321e37dfd99637L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClear(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testClearEdges() {
		final long seed = 0xad0005187cebcd83L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClearEdges(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopy() {
		final long seed = 0xf26cb0540a4874e8L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x5adf5b4d0c0a0f16L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x14e08fffb6f5c0f4L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0x9004ede4c2aa7f1bL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x6519a3d6cfdcaa15L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedMST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x93159a7004fabaabL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMDST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xb3bd816cf5395102L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMaxFlow(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		final long seed = 0x40f90cfba5f21f7cL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedBipartiteMatching(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		final long seed = 0x6a1920e2c7e46291L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xb49ae602bf9f9896L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), false, seed);
		});
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xf1d6fb75a6d8d711L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), true, seed);
		});
	}

}
