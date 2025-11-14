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

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class GraphMatrixTest extends TestBase {

	private static Function<Boolean, Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> IntGraphFactory
				.newInstance(directed.booleanValue())
				.setOption("impl", selfEdges ? "matrix-selfedges" : "matrix")
				.newGraph();
	}

	@Test
	public void addVertex() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testAddVertex(graphImpl(selfEdges));
		});
	}

	@Test
	public void addVertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.addVerticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void removeVertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.removeVerticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void vertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.verticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void addEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testAddEdge(graphImpl(selfEdges));
		});
	}

	@Test
	public void addEdges() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.addEdgesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void addEdgesReassignIds() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.addEdgesReassignIdsTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void removeEdges() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.removeEdgesTest(graphImpl(selfEdges));
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
			GraphImplTestUtils.getEdgeTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdgesOutIn() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.outInEdgesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdgesSourceTarget() {
		final long seed = 0xe625cd95d66b6839L;
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
		final long seed = 0x25824e374104beceL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClear(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x79550c17b3a2bb6eL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClearEdges(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopy() {
		final long seed = 0xd400f44f753a56b6L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x1e7e9287a4e51db6L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x325c47e089f6edebL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0x662367fc40987614L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void copyConstructor() {
		final long seed = 0x8e5b78fe69a1c611L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyConstructor(g -> {
				if (g.isDirected()) {
					return new GraphMatrixDirected(selfEdges, g, true, true);
				} else {
					return new GraphMatrixUndirected(selfEdges, g, true, true);
				}
			}, seed);
		});
	}

	@Test
	public void builderConstructor() {
		final long seed = 0xd95f38700240f660L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testBuilderConstructor(builder -> {
				if (builder.isDirected()) {
					return new GraphMatrixDirected(selfEdges, builder);
				} else {
					return new GraphMatrixUndirected(selfEdges, builder);
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
		final long seed = 0x51e3bdb3331d8cb3L;
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
	public void testUndirectedMdst() {
		final long seed = 0x63a396934a49021cL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedMst(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMdst() {
		final long seed = 0xcebe72e8015778c1L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMdst(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xe2e6e3d11dfaa9dfL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMaxFlow(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0x2aee685276834043L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), false, seed);
		});
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0x4cd9a3bcb63cf8f8L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), true, seed);
		});
	}

	@Test
	public void capabilities() {
		foreachBoolConfig((directed, selfEdges) -> {
			IndexGraph g;
			if (directed) {
				g = new GraphMatrixDirected(selfEdges, 0, 0);
			} else {
				g = new GraphMatrixUndirected(selfEdges, 0, 0);
			}
			assertEqualsBool(directed, g.isDirected());
			assertEqualsBool(selfEdges, g.isAllowSelfEdges());
			assertFalse(g.isAllowParallelEdges());
		});
	}

}
