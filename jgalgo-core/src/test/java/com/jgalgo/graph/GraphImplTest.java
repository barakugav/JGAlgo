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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class GraphImplTest extends TestBase {

	@Test
	public void addVertexNull() {
		Graph<String, String> g = Graph.newUndirected();
		assertThrows(IllegalArgumentException.class, () -> g.addVertex(null));
	}

	@Test
	public void addVertexDuplication() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("A");
		assertThrows(IllegalArgumentException.class, () -> g.addVertex("A"));
	}

	@Test
	public void addEdgeNull() {
		Graph<String, String> g = Graph.newUndirected();
		g.addVertex("A");
		g.addVertex("B");
		assertThrows(IllegalArgumentException.class, () -> g.addEdge("A", "B", null));
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
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex("A", null));
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
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge("AB", null));
	}

}
