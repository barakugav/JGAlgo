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

public class GraphLinkedTest extends TestBase {

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> IntGraphFactory.newInstance(directed)
				.setOption("impl", selfEdges ? "linked-list-selfedges" : "linked-list").newGraph();
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
			GraphImplTestUtils.testGetEdgesOutIn(graphImpl(selfEdges));
		});
	}

	@Test
	public void testGetEdgesSourceTarget() {
		final long seed = 0xdbadd874840f349cL;
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
		final long seed = 0xa87763a4d802f408L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClear(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x43435f8582dc816eL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClearEdges(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopy() {
		final long seed = 0xe2efa42f139a4254L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x814eb6bdae72fed1L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x3766037dcbed2ec3L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0xd66a281795c52020L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void copyConstructor() {
		final long seed = 0xe66a1af5afad78f9L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyConstructor(g -> {
				if (g.isDirected()) {
					return new GraphLinkedDirected(selfEdges, g, true, true);
				} else {
					return new GraphLinkedUndirected(selfEdges, g, true, true);
				}
			}, seed);
		});
	}

	@Test
	public void builderConstructor() {
		final long seed = 0x6b6cb8916d75ad80L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testBuilderConstructor(builder -> {
				if (builder.isDirected) {
					return new GraphLinkedDirected(selfEdges, builder);
				} else {
					return new GraphLinkedUndirected(selfEdges, builder);
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
		final long seed = 0xb17c92de3f262267L;
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
		final long seed = 0xfa13f4e010916dcaL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedMST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x62f68fa5a31c6010L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMDST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0x643cd690ad09efb4L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMaxFlow(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xa05a3427656375aaL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), false, seed);
		});
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xdd2bc9ad386bf866L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), true, seed);
		});
	}

	@Test
	public void capabilities() {
		foreachBoolConfig((directed, selfEdges) -> {
			IndexGraph g;
			if (directed) {
				g = new GraphLinkedDirected(selfEdges, 0, 0);
			} else {
				g = new GraphLinkedUndirected(selfEdges, 0, 0);
			}
			assertEqualsBool(directed, g.isDirected());
			assertEqualsBool(selfEdges, g.isAllowSelfEdges());
			assertTrue(g.isAllowParallelEdges());
		});
	}

}
