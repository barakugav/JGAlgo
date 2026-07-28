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
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphFactoryTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void newCopyOf() {
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);

			Graph<Integer, Integer> g1 = factory.newGraph();
			g1.addVertices(range(10, 20));
			g1.addEdge(10, 11, 5);
			g1.addEdge(11, 12, 6);

			Graph<Integer, Integer> g2 = factory.newCopyOf(g1);
			assertEquals(g1, g2);

			/* check mutable */
			g2.addVertex(100);
			assertTrue(g2.vertices().contains(100));
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);

			Graph<Integer, Integer> g1 = factory.newGraph();
			g1.addVertices(range(10, 20));
			g1.addEdge(10, 11, 5);
			g1.addEdge(11, 12, 6);
			WeightsDouble<Integer> weights1 = g1.addEdgesWeights("weights", double.class);
			weights1.set(5, 1.0);
			weights1.set(6, 2.0);

			Graph<Integer, Integer> g2 = factory.newCopyOf(g1, true, true);
			assertEquals(g1, g2);

			Graph<Integer, Integer> g3 = factory.newCopyOf(g1, false, false);
			assertNotEquals(g1, g3);
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			Graph<Integer, Integer> g1 = factory.newGraph();

			factory.setVertexBuilder(existingIds -> existingIds.size() + 11);
			factory.setEdgeBuilder(existingIds -> existingIds.size() + 80);
			Graph<Integer, Integer> g2 = factory.newCopyOf(g1);
			g2.addVertex();
			g2.addVertex();
			assertEquals(IntSet.of(11, 12), g2.vertices());
			g2.addEdge(11, 12);
			assertEquals(IntSet.of(80), g2.edges());
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void newImmutableCopyOf() {
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);

			Graph<Integer, Integer> g1 = factory.newGraph();
			g1.addVertices(range(10, 20));
			g1.addEdge(10, 11, 5);
			g1.addEdge(11, 12, 6);

			Graph<Integer, Integer> g2 = factory.newImmutableCopyOf(g1);
			assertEquals(g1, g2);

			/* check immutable */
			assertThrows(UnsupportedOperationException.class, () -> g2.addVertex(100));
		});
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);

			Graph<Integer, Integer> g1 = factory.newGraph();
			g1.addVertices(range(10, 20));
			g1.addEdge(10, 11, 5);
			g1.addEdge(11, 12, 6);
			WeightsDouble<Integer> weights1 = g1.addEdgesWeights("weights", double.class);
			weights1.set(5, 1.0);
			weights1.set(6, 2.0);

			Graph<Integer, Integer> g2 = factory.newImmutableCopyOf(g1, true, true);
			assertEquals(g1, g2);

			Graph<Integer, Integer> g3 = factory.newImmutableCopyOf(g1, false, false);
			assertNotEquals(g1, g3);
		});
	}

	@Test
	public void setDirected() {
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			assertEqualsBool(directed, factory.newGraph().isDirected());
			factory.setDirected(!directed);
			assertEqualsBool(!directed, factory.newGraph().isDirected());
		});
	}

	@Test
	public void expectedVerticesNum() {
		foreachBoolConfig(directed -> {
			/* nothing to check really, just call and make sure no exception is thrown */
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			factory.expectedVerticesNum(100);
			assertNotNull(factory.newGraph());

			assertThrows(IllegalArgumentException.class, () -> factory.expectedVerticesNum(-1));
		});
	}

	@Test
	public void expectedEdgesNum() {
		foreachBoolConfig(directed -> {
			/* nothing to check really, just call and make sure no exception is thrown */
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			factory.expectedEdgesNum(100);
			assertNotNull(factory.newGraph());

			assertThrows(IllegalArgumentException.class, () -> factory.expectedEdgesNum(-1));
		});
	}

	@Test
	public void hints() {
		foreachBoolConfig(directed -> {
			GraphFactory<Integer, Integer> factory = GraphFactory.newInstance(directed);
			Class<?> defaultImpl = factory.newGraph().indexGraph().getClass();
			factory.addHint(GraphFactory.Hint.FastEdgeLookup);
			assertNotEquals(defaultImpl, factory.newGraph().indexGraph().getClass());
			factory.removeHint(GraphFactory.Hint.FastEdgeLookup);
			assertEquals(defaultImpl, factory.newGraph().indexGraph().getClass());
		});
	}

}
