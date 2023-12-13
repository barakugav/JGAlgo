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

public class GraphHashmapMultiTest extends TestBase {

	private static Boolean2ObjectFunction<Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> IntGraphFactory.newInstance(directed)
				.setOption("impl", selfEdges ? "hashtable-multi-selfedges" : "hashtable-multi").newGraph();
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
		final long seed = 0xbe1150acd4dbdb52L;
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
		final long seed = 0x8b91671de95def6bL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClear(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testClearEdges() {
		final long seed = 0x78de7f68b2175767L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testClearEdges(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopy() {
		final long seed = 0xbbb58f1e21d76e5L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testCopyWithWeights() {
		final long seed = 0x5a7ee235c32a5ef0L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopy() {
		final long seed = 0x26a79b129533cc81L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopy(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testImmutableCopyWithWeights() {
		final long seed = 0xabbc453653c0b368L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testImmutableCopyWithWeights(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void copyConstructor() {
		final long seed = 0x2f0feb2e2abcfc01L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testCopyConstructor(g -> {
				if (g.isDirected()) {
					return new GraphHashmapMultiDirected(selfEdges, g, true, true);
				} else {
					return new GraphHashmapMultiUndirected(selfEdges, g, true, true);
				}
			}, seed);
		});
	}

	@Test
	public void builderConstructor() {
		final long seed = 0xe88e6580761faf67L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testBuilderConstructor(builder -> {
				if (builder.isDirected) {
					return new GraphHashmapMultiDirected(selfEdges, builder);
				} else {
					return new GraphHashmapMultiUndirected(selfEdges, builder);
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
		final long seed = 0x5678d5379291f193L;
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
		final long seed = 0x149c6019ec1655c2L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedMST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMDST() {
		final long seed = 0x27445adceab0986dL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMDST(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testDirectedMaxFlow() {
		final long seed = 0xd8aefabd35edd53L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testDirectedMaxFlow(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testUndirectedBipartiteMatching() {
		final long seed = 0xf5915fab473f3de8L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedBipartiteMatching(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testUndirectedBipartiteMatchingWeighted() {
		final long seed = 0xac6670aa884c0c87L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testUndirectedBipartiteMatchingWeighted(graphImpl(selfEdges), seed);
		});
	}

	@Test
	public void testRandOpsUndirected() {
		final long seed = 0xad00337d69996b7fL;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), false, seed);
		});
	}

	@Test
	public void testRandOpsDirected() {
		final long seed = 0xfa7bd3f2a2d735a7L;
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.testRandOps(graphImpl(selfEdges), true, seed);
		});
	}

	@Test
	public void capabilities() {
		foreachBoolConfig((directed, selfEdges) -> {
			IndexGraph g;
			if (directed) {
				g = new GraphHashmapMultiDirected(selfEdges, 0, 0);
			} else {
				g = new GraphHashmapMultiUndirected(selfEdges, 0, 0);
			}
			assertEqualsBool(directed, g.isDirected());
			assertEqualsBool(selfEdges, g.isAllowSelfEdges());
			assertTrue(g.isAllowParallelEdges());
		});
	}

}
