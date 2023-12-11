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

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;

public class GraphLinkedPtrTest extends TestBase {

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> IntGraphFactory.newInstance(directed)
				.setOption("impl", selfEdges ? "linked-list-ptr-selfedges" : "linked-list-ptr").newGraph();
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testAddVertex(graphImpl(selfEdges));
		});
	}

	@Test
	public void vertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.verticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void addVertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.addVerticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void addEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testAddEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void edges() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.edgesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void endpoints() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testEndpoints(graphImpl(selfEdges));
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
		final long seed = 0xf17f2392d708a6bbL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testGetEdgesSourceTarget(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testEdgeIter() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testEdgeIter(graphImpl(selfEdges));
		});
	}

	@Test
	public void edgeIterRemoveSingleEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testEdgeIterRemoveSingle(graphImpl(selfEdges));
		});
	}

	@Test
	public void edgeIterRemoveAll() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testEdgeIterRemoveAll(graphImpl(selfEdges));
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
		final long seed = 0x100115652062b424L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClear(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x914bb2f87efda719L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClearEdges(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopy() {
		final long seed = 0x6f2eabc8e7cd3a70L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x5c51c3fa807b25bcL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x9f77f9dfded3f6fL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0x6966e624022a1540L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void copyConstructor() {
		final long seed = 0xb924cde1f2774bbbL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyConstructor(g -> {
				if (g.isDirected()) {
					return new GraphLinkedPtrDirected(selfEdges, g, true, true);
				} else {
					return new GraphLinkedPtrUndirected(selfEdges, g, true, true);
				}
			}, seed);
		});
	}

	@Test
	public void builderConstructor() {
		final long seed = 0xcc8121b9ca48ddd1L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testBuilderConstructor(builder -> {
				if (builder.isDirected()) {
					return new GraphLinkedPtrDirected(selfEdges, (IndexGraphBuilderImpl) builder);
				} else {
					return new GraphLinkedPtrUndirected(selfEdges, (IndexGraphBuilderImpl) builder);
				}
			}, seed);
		});
	}

	@Test
	public void removeEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRemoveEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void reverseEdge() {
		final long seed = 0xd8e7ea9aff32441fL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testReverseEdge(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void moveEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testMoveEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void testUndirectedMST() {
		final long seed = 0x757d2f9883276f90L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedMST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x96f07cf342fcb057L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMDST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xb3775d0c2d4aa98aL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMaxFlow(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xbda54e345679e161L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), false, seed);
		});
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0x136a0df5ecaae5a2L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), true, seed);
		});
	}

	@Test
	public void capabilities() {
		foreachBoolConfig((directed, selfEdges) -> {
			IndexGraph g;
			if (directed) {
				g = new GraphLinkedPtrDirected(selfEdges, 0, 0);
			} else {
				g = new GraphLinkedPtrUndirected(selfEdges, 0, 0);
			}
			assertEqualsBool(directed, g.isDirected());
			assertEqualsBool(selfEdges, g.isAllowSelfEdges());
			assertTrue(g.isAllowParallelEdges());
		});
	}

}
