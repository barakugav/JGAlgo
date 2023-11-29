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

public class IntGraphImplTest extends TestBase {

	@Test
	public void addVertexNegative() {
		IntGraph g = IntGraph.newUndirected();
		assertThrows(IllegalArgumentException.class, () -> g.addVertex(-1));
	}

	@Test
	public void addVertexDuplication() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(7);
		assertThrows(IllegalArgumentException.class, () -> g.addVertex(7));
	}

	@Test
	public void addEdgeNegative() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(7);
		g.addVertex(8);
		assertThrows(IllegalArgumentException.class, () -> g.addEdge(7, 8, -1));
	}

	@Test
	public void addEdgeDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.addEdge(8, 7, 3));
	}

	@Test
	public void renameVertex() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.renameVertex(7, 22);

		IntGraph expected = IntGraph.newDirected();
		expected.addVertex(22);
		expected.addVertex(8);
		expected.addEdge(22, 8, 3);
		assertEquals(expected, g);
	}

	@Test
	public void renameVertexDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex(7, 8));
	}

	@Test
	public void renameVertexNonExisting() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(NoSuchVertexException.class, () -> g.renameVertex(22, 23));
	}

	@Test
	public void renameVertexNegative() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameVertex(7, -1));
	}

	@Test
	public void renameEdge() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.renameEdge(3, 10);

		IntGraph expected = IntGraph.newDirected();
		expected.addVertex(7);
		expected.addVertex(8);
		expected.addEdge(7, 8, 10);
		assertEquals(expected, g);
	}

	@Test
	public void renameEdgeDuplication() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		g.addEdge(7, 8, 10);
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge(3, 10));
	}

	@Test
	public void renameEdgeNonExisting() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(NoSuchEdgeException.class, () -> g.renameEdge(10, 15));
	}

	@Test
	public void renameEdgeNegative() {
		IntGraph g = IntGraph.newDirected();
		g.addVertex(7);
		g.addVertex(8);
		g.addEdge(7, 8, 3);
		assertThrows(IllegalArgumentException.class, () -> g.renameEdge(3, -1));
	}

}
