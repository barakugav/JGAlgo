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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntSets;

public class EmptyGraphTest extends TestBase {

	@Test
	public void testVertices() {
		assertEquals(IntSets.emptySet(), Graphs.EmptyGraphUndirected.vertices());
		assertEquals(IntSets.emptySet(), Graphs.EmptyGraphDirected.vertices());
	}

	@Test
	public void testEdges() {
		assertEquals(IntSets.emptySet(), Graphs.EmptyGraphUndirected.edges());
		assertEquals(IntSets.emptySet(), Graphs.EmptyGraphDirected.edges());
	}

	@Test
	public void testAddVertex() {
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphUndirected.addVertex());
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphDirected.addVertex());
	}

	@Test
	public void testRemoveVertex() {
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphUndirected.removeVertex(0));
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphDirected.removeVertex(0));
	}

	@Test
	public void testEdgesOut() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.outEdges(0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.outEdges(0));
	}

	@Test
	public void testEdgesIn() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.inEdges(0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.inEdges(0));
	}

	@Test
	public void testGetEdge() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.getEdge(0, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.getEdge(0, 0));
	}

	@Test
	public void testGetEdges() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.getEdges(0, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.getEdges(0, 0));
	}

	@Test
	public void testAddEdge() {
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphUndirected.addEdge(0, 0));
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphDirected.addEdge(0, 0));
	}

	@Test
	public void testRemoveEdge() {
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphUndirected.removeEdge(0));
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphDirected.removeEdge(0));
	}

	@Test
	public void testReverseEdge() {
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphUndirected.reverseEdge(0));
		assertThrows(UnsupportedOperationException.class, () -> Graphs.EmptyGraphDirected.reverseEdge(0));
	}

	@Test
	public void testEdgeSource() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.edgeSource(0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.edgeSource(0));
	}

	@Test
	public void testEdgeTarget() {
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphUndirected.edgeTarget(0));
		assertThrows(IndexOutOfBoundsException.class, () -> Graphs.EmptyGraphDirected.edgeTarget(0));
	}

	@Test
	public void testWeights() {
		assertEquals(Collections.emptySet(), Graphs.EmptyGraphUndirected.getVerticesWeightsKeys());
		assertEquals(Collections.emptySet(), Graphs.EmptyGraphUndirected.getEdgesWeightsKeys());
		assertEquals(Collections.emptySet(), Graphs.EmptyGraphDirected.getVerticesWeightsKeys());
		assertEquals(Collections.emptySet(), Graphs.EmptyGraphDirected.getEdgesWeightsKeys());
		final String weightsKey = "keyName";
		assertThrows(UnsupportedOperationException.class,
				() -> Graphs.EmptyGraphUndirected.addVerticesWeights(weightsKey, null));
		assertThrows(UnsupportedOperationException.class,
				() -> Graphs.EmptyGraphDirected.addVerticesWeights(weightsKey, null));
		assertThrows(UnsupportedOperationException.class,
				() -> Graphs.EmptyGraphUndirected.addEdgesWeights(weightsKey, null));
		assertThrows(UnsupportedOperationException.class,
				() -> Graphs.EmptyGraphDirected.addEdgesWeights(weightsKey, null));
		assertNull(Graphs.EmptyGraphUndirected.getVerticesWeights(weightsKey));
		assertNull(Graphs.EmptyGraphDirected.getVerticesWeights(weightsKey));
		assertNull(Graphs.EmptyGraphUndirected.getEdgesWeights(weightsKey));
		assertNull(Graphs.EmptyGraphDirected.getEdgesWeights(weightsKey));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testIndexGraph() {
		assertEquals(Graphs.EmptyGraphUndirected, Graphs.EmptyGraphUndirected.indexGraph());
		assertEquals(Graphs.EmptyGraphDirected, Graphs.EmptyGraphDirected.indexGraph());
	}

	@Test
	public void testCopy() {
		assertEquals(Graphs.EmptyGraphUndirected, Graphs.EmptyGraphUndirected.copy());
		assertEquals(Graphs.EmptyGraphDirected, Graphs.EmptyGraphDirected.copy());
	}

	@Test
	public void testCapabilities() {
		assertFalse(Graphs.EmptyGraphUndirected.isDirected());
		assertTrue(Graphs.EmptyGraphDirected.isDirected());
	}

}
