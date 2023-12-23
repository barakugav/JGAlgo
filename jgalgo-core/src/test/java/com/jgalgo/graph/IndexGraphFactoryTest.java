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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class IndexGraphFactoryTest extends TestBase {

	@Test
	public void newCopyOf() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);

			IndexGraph g1 = factory.newGraph();
			g1.addVertices(range(10));
			g1.addEdge(1, 2);
			g1.addEdge(3, 4);

			IndexGraph g2 = factory.newCopyOf(g1);
			assertEquals(g1, g2);

			/* check mutable */
			int v = g2.addVertexInt();
			assertTrue(g2.vertices().contains(v));
		});
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);

			IndexGraph g1 = factory.newGraph();
			g1.addVertices(range(10));
			g1.addEdge(1, 2);
			g1.addEdge(3, 4);
			IWeightsDouble weights1 = g1.addEdgesWeights("weights", double.class);
			weights1.set(0, 1.0);
			weights1.set(1, 2.0);

			IndexGraph g2 = factory.newCopyOf(g1, true, true);
			assertEquals(g1, g2);

			IndexGraph g3 = factory.newCopyOf(g1, false, false);
			assertNotEquals(g1, g3);
		});
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			IndexGraph g1 = IndexGraphFactory.newInstance(!directed).newGraph();
			assertThrows(IllegalArgumentException.class, () -> factory.newCopyOf(g1));
		});
	}

	@Test
	public void newImmutableCopyOf() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);

			IndexGraph g1 = factory.newGraph();
			g1.addVertices(range(10));
			g1.addEdge(1, 2);
			g1.addEdge(3, 4);

			IndexGraph g2 = factory.newImmutableCopyOf(g1);
			assertEquals(g1, g2);

			/* check immutable */
			assertThrows(UnsupportedOperationException.class, () -> g2.addVertexInt());
		});
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);

			IndexGraph g1 = factory.newGraph();
			g1.addVertices(range(10));
			g1.addEdge(1, 2);
			g1.addEdge(3, 4);
			IWeightsDouble weights1 = g1.addEdgesWeights("weights", double.class);
			weights1.set(0, 1.0);
			weights1.set(1, 2.0);

			IndexGraph g2 = factory.newImmutableCopyOf(g1, true, true);
			assertEquals(g1, g2);

			IndexGraph g3 = factory.newImmutableCopyOf(g1, false, false);
			assertNotEquals(g1, g3);
		});
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			IndexGraph g1 = IndexGraphFactory.newInstance(!directed).newGraph();
			assertThrows(IllegalArgumentException.class, () -> factory.newImmutableCopyOf(g1));
		});
	}

	@Test
	public void setDirected() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertEqualsBool(directed, factory.newGraph().isDirected());
			factory.setDirected(!directed);
			assertEqualsBool(!directed, factory.newGraph().isDirected());
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void setVertexFactory() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setVertexFactory(null));
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void setEdgeFactory() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setEdgeFactory(null));
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void setVertexBuilder() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setVertexBuilder(null));
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void setEdgeBuilder() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(UnsupportedOperationException.class, () -> factory.setEdgeBuilder(null));
		});
	}

	@Test
	public void setOptionUnknownImpl() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(IllegalArgumentException.class, () -> factory.setOption("impl", "unknown-impl").newGraph());
		});
	}

	@Test
	public void setOptionUnknownOption() {
		foreachBoolConfig(directed -> {
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			assertThrows(IllegalArgumentException.class, () -> factory.setOption("unknown-option", "value"));
		});
	}

	@Test
	public void expectedVerticesNum() {
		foreachBoolConfig(directed -> {
			/* nothing to check really, just call and make sure no exception is thrown */
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			factory.expectedVerticesNum(100);
			assertNotNull(factory.newGraph());

			assertThrows(IllegalArgumentException.class, () -> factory.expectedVerticesNum(-1));
		});
	}

	@Test
	public void expectedEdgesNum() {
		foreachBoolConfig(directed -> {
			/* nothing to check really, just call and make sure no exception is thrown */
			IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
			factory.expectedEdgesNum(100);
			assertNotNull(factory.newGraph());

			assertThrows(IllegalArgumentException.class, () -> factory.expectedEdgesNum(-1));
		});
	}

	@Test
	public void hints() {
		foreachBoolConfig((directed, selfEdges, parallelEdges) -> {
			foreachBoolConfig((fastRemove, fastLookup, denseGraph) -> {
				IndexGraphFactory factory = IndexGraphFactory.newInstance(directed);
				factory.allowSelfEdges(selfEdges);
				factory.allowParallelEdges(parallelEdges);
				if (fastRemove)
					factory.addHint(GraphFactory.Hint.FastEdgeRemoval);
				if (fastLookup)
					factory.addHint(GraphFactory.Hint.FastEdgeLookup);
				if (denseGraph)
					factory.addHint(GraphFactory.Hint.DenseGraph);

				IndexGraph g = factory.newGraph();
				assertEqualsBool(directed, g.isDirected());
				assertEqualsBool(selfEdges, g.isAllowSelfEdges());
				assertEqualsBool(parallelEdges, g.isAllowParallelEdges());

				Class<?> expectedImp;
				if (fastLookup || !parallelEdges) {
					if (parallelEdges) {
						expectedImp = directed ? GraphHashmapMultiDirected.class : GraphHashmapMultiUndirected.class;
					} else if (denseGraph) {
						expectedImp = directed ? GraphMatrixDirected.class : GraphMatrixUndirected.class;
					} else {
						expectedImp = directed ? GraphHashmapDirected.class : GraphHashmapUndirected.class;
					}
				} else if (fastRemove) {
					expectedImp = directed ? GraphLinkedDirected.class : GraphLinkedUndirected.class;
				} else {
					expectedImp = directed ? GraphArrayDirected.class : GraphArrayUndirected.class;
				}
				assertEquals(expectedImp, g.getClass());
			});
		});
	}

}
