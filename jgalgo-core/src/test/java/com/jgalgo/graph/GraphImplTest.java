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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class GraphImplTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void addVertex() {
		final Random rand = new Random(0x6771c2cd7f36ac88L);
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = directed ? Graph.newDirected() : Graph.newDirected();
				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int v = rand.nextInt();
					if (vertices.contains(v))
						continue;
					g.addVertex(v);
					vertices.add(v);
				}
				assertEquals(vertices, g.vertices());
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void addVertices() {
		final Random rand = new Random(0xb7f57a04c7c50e73L);
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = directed ? Graph.newDirected() : Graph.newDirected();

			Set<Integer> vertices = new HashSet<>();
			List<Integer> verticesList = new ArrayList<>();
			for (int r = 0; r < 50; r++) {
				int num = rand.nextInt(5);
				List<Integer> vs = new ArrayList<>();
				while (vs.size() < num) {
					int v = rand.nextInt();
					if (v < 0 || vertices.contains(v) || vs.contains(v))
						continue;
					vs.add(v);
				}
				if (r % 4 == 0) {
					g.addVertices(vs);
					vertices.addAll(vs);
					verticesList.addAll(vs);
				} else if (r % 4 == 1) {
					vs.add(null);
					Collections.shuffle(vs, rand);
					assertThrows(NullPointerException.class, () -> g.addVertices(vs));
				} else if (r % 4 == 2 && vs.size() > 0) {
					vs.add(randElement(vs, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
				} else if (r % 4 == 3 && vertices.size() > 0) {
					vs.add(randElement(verticesList, rand));
					Collections.shuffle(vs, rand);
					assertThrows(IllegalArgumentException.class, () -> g.addVertices(vs));
				}
				assertEquals(vertices, g.vertices());
			}
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void verticesToArray() {
		final Random rand = new Random(0x6771c2cd7f36ac88L);
		foreachBoolConfig(directed -> {
			for (int repeat = 0; repeat < 25; repeat++) {
				final int n = rand.nextInt(100);
				Graph<Integer, Integer> g = directed ? Graph.newDirected() : Graph.newDirected();
				Set<Integer> vertices = new IntOpenHashSet();
				while (vertices.size() < n) {
					int v = rand.nextInt();
					if (vertices.contains(v))
						continue;
					g.addVertex(v);
					vertices.add(v);
				}

				Object[] arr1 = g.vertices().toArray();
				Integer[] arr2 = g.vertices().toArray(new Integer[0]);
				Integer[] arr3Input = new Integer[vertices.size()];
				Integer[] arr3 = g.vertices().toArray(arr3Input);
				Integer[] arr4Input = new Integer[vertices.size() + 7];
				Arrays.fill(arr4Input, -788);
				Integer[] arr4 = g.vertices().toArray(arr4Input);
				assertEquals(vertices, Set.of(arr1)); /* Set.of() checks that there are no duplications */
				assertEquals(vertices, Set.of(arr2));
				assertEquals(vertices, Set.of(arr3));
				assertEquals(vertices, Set.of(Arrays.copyOf(arr4, n)));
				assertTrue(arr3Input == arr3);
				assertTrue(arr4Input == arr4);
				assertNull(arr4[n]);
				for (int i : range(n + 1, arr4.length))
					assertEquals(-788, arr4[i]);
			}
		});
	}

	@Test
	public void addVertexNull() {
		Graph<String, String> g = Graph.newUndirected();
		assertThrows(NullPointerException.class, () -> g.addVertex(null));
	}

	@Test
	public void addVertexDuplication() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("A");
		assertThrows(IllegalArgumentException.class, () -> g.addVertex("A"));
	}

	@Test
	public void addVertexWithVertexBuilder() {
		assertNull(Graph.newUndirected().vertexBuilder());
		assertThrows(UnsupportedOperationException.class, () -> Graph.newUndirected().addVertex());

		GraphFactory<String, String> factory = GraphFactory.undirected();
		factory.setVertexBuilder(vertices -> String.valueOf(101 + vertices.size()));
		Graph<String, String> g = factory.newGraph();
		assertNotNull(g.vertexBuilder());
		String v1 = g.addVertex();
		String v2 = g.addVertex();
		String v3 = "333";
		g.addVertex(v3);
		String v4 = g.addVertex();
		assertEquals("101", v1);
		assertEquals("102", v2);
		assertEquals("104", v4);
		assertEquals(Set.of("101", "102", "104", "333"), g.vertices());
	}

	@Test
	public void addVertexWithVertexBuilderNull() {
		GraphFactory<Integer, String> factory = GraphFactory.undirected();
		IdBuilderInt vBuilder = vertices -> 1 + vertices.size();
		factory.setVertexBuilder(vBuilder);
		Graph<Integer, String> g = factory.newGraph();
		assertEquals(1, g.addVertex());
		assertEquals(2, g.addVertex());
		assertEquals(3, g.addVertex());
		assertEquals(4, g.addVertex());
	}

	@Test
	public void removeVertices() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.removeVerticesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void addEdgeNull() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("A");
		g.addVertex("B");
		assertThrows(NullPointerException.class, () -> g.addEdge("A", "B", null));
	}

	@Test
	public void addEdgeDuplication() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(IllegalArgumentException.class, () -> g.addEdge("B", "A", "AB"));
	}

	@Test
	public void addEdgeWithEdgeBuilder() {
		assertNull(Graph.newUndirected().edgeBuilder());
		assertThrows(UnsupportedOperationException.class, () -> {
			Graph<String, String> g = Graph.newUndirected();
			g.addVertex("A");
			g.addVertex("B");
			g.addEdge("A", "B");
		});

		GraphFactory<String, String> factory = GraphFactory.undirected();
		factory.setEdgeBuilder(edges -> String.valueOf(101 + edges.size()));
		Graph<String, String> g = factory.newGraph();
		assertNotNull(g.edgeBuilder());
		g.addVertices(List.of("A", "B", "C", "D", "E"));
		String e1 = g.addEdge("A", "B");
		String e2 = g.addEdge("B", "C");
		String e3 = "333";
		g.addEdge("C", "D", e3);
		String e4 = g.addEdge("D", "E");
		assertEquals("101", e1);
		assertEquals("102", e2);
		assertEquals("104", e4);
		assertEquals(Set.of("101", "102", "104", "333"), g.edges());
	}

	@Test
	public void addEdges() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.addEdgesTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void getEdge() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.getEdgeTest(graphImpl(selfEdges));
		});
	}

	@Test
	public void removeEdges() {
		foreachBoolConfig(selfEdges -> {
			GraphImplTestUtils.removeEdgesTest(graphImpl(selfEdges));
		});
	}

	private static Function<Boolean, Graph<Integer, Integer>> graphImpl(boolean selfEdges) {
		return directed -> GraphFactory
				.<Integer, Integer>newInstance(directed.booleanValue())
				.allowSelfEdges(selfEdges)
				.newGraph();
	}

	@Test
	public void renameVertex() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		g.renameVertex("A", "C");

		Graph<String, String> expected = Graph.newDirected();
		expected.addVertex("C");
		expected.addVertex("B");
		expected.addEdge("C", "B", "AB");
		assertEquals(expected, g);
	}

	@Test
	public void renameVertexSameId() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		g.renameVertex("A", "A");

		Graph<String, String> expected = Graph.newDirected();
		expected.addVertex("A");
		expected.addVertex("B");
		expected.addEdge("A", "B", "AB");
		assertEquals(expected, g);
	}

	@Test
	public void renameVertexDuplication() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex("A", "B"));
	}

	@Test
	public void renameVertexNonExisting() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(NoSuchVertexException.class, () -> g.renameVertex("C", "D"));
	}

	@Test
	public void renameVertexNull() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(NullPointerException.class, () -> g.renameVertex("A", null));
	}

	@Test
	public void renameEdge() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		g.renameEdge("AB", "CCCCCCCC");

		Graph<String, String> expected = Graph.newDirected();
		expected.addVertex("A");
		expected.addVertex("B");
		expected.addEdge("A", "B", "CCCCCCCC");
		assertEquals(expected, g);
	}

	@Test
	public void renameEdgeSameId() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		g.renameEdge("AB", "AB");

		Graph<String, String> expected = Graph.newDirected();
		expected.addVertex("A");
		expected.addVertex("B");
		expected.addEdge("A", "B", "AB");
		assertEquals(expected, g);
	}

	@Test
	public void renameEdgeDuplication() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		g.addEdge("A", "B", "CCCCCCCC");
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge("AB", "CCCCCCCC"));
	}

	@Test
	public void renameEdgeNonExisting() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(NoSuchEdgeException.class, () -> g.renameEdge("CCCCCCCC", "DDDDDDDD"));
	}

	@Test
	public void renameEdgeNull() {
		Graph<String, String> g = Graph.newDirected();
		g.addVertex("A");
		g.addVertex("B");
		g.addEdge("A", "B", "AB");
		assertThrows(NullPointerException.class, () -> g.renameEdge("AB", null));
	}

}
