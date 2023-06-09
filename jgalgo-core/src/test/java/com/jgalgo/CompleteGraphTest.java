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
package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class CompleteGraphTest extends TestBase {

	@Test
	public void testVertices() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			IntSet expectedVertices = new IntOpenHashSet(n);
			for (int v = 0; v < n; v++)
				expectedVertices.add(v);
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				assertEquals(expectedVertices, g.vertices());
			}
		}
	}

	@Test
	public void testEdges() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);

				int m = n * (n - 1) / (directed ? 1 : 2);
				IntSet expectedEdges = new IntOpenHashSet(m);
				for (int e = 0; e < m; e++)
					expectedEdges.add(e);

				assertEquals(expectedEdges, g.edges());
			}
		}
	}

	@Test
	public void testAddRemoveVertex() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				assertThrows(UnsupportedOperationException.class, () -> g.addVertex());
				if (n > 0)
					assertThrows(UnsupportedOperationException.class,
							() -> g.removeVertex(g.vertices().iterator().nextInt()));
			}
		}
	}

	@Test
	public void testAddRemoveEdge() {
		for (int n : IntList.of(2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				assertThrows(UnsupportedOperationException.class, () -> g.addEdge(0, 1));
				assertThrows(UnsupportedOperationException.class, () -> g.removeEdge(g.edges().iterator().nextInt()));
			}
		}
	}

	@Test
	public void testEdgesOutIn() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);

				for (int u : g.vertices()) {
					EdgeSet edgesOut = g.edgesOut(u);
					assertEquals(n - 1, edgesOut.size());

					IntSet expectedEdges = new IntOpenHashSet(n - 1);
					for (int v = 0; v < n; v++)
						if (u != v)
							expectedEdges.add(g.getEdge(u, v));
					assertEquals(n - 1, expectedEdges.size());
					assertEquals(expectedEdges, edgesOut);

					IntSet iteratedEdges = new IntOpenHashSet(n - 1);
					for (EdgeIter eit = edgesOut.iterator(); eit.hasNext();) {
						int peekNext = ((EdgeIterImpl) eit).peekNext();
						int e = eit.nextInt();
						assertEquals(peekNext, e);

						assertEquals(u, eit.source());
						assertEquals(g.edgeEndpoint(e, u), eit.target());

						iteratedEdges.add(e);
					}
					assertEquals(expectedEdges, iteratedEdges);
				}
				for (int v : g.vertices()) {
					EdgeSet edgesIn = g.edgesIn(v);
					assertEquals(n - 1, edgesIn.size());

					IntSet expectedEdges = new IntOpenHashSet(n - 1);
					for (int u = 0; u < n; u++)
						if (v != u)
							expectedEdges.add(g.getEdge(u, v));
					assertEquals(n - 1, expectedEdges.size());
					assertEquals(expectedEdges, edgesIn);

					IntSet iteratedEdges = new IntOpenHashSet(n - 1);
					for (EdgeIter eit = edgesIn.iterator(); eit.hasNext();) {
						int peekNext = ((EdgeIterImpl) eit).peekNext();
						int e = eit.nextInt();
						assertEquals(peekNext, e);

						assertEquals(g.edgeEndpoint(e, v), eit.source());
						assertEquals(v, eit.target());

						iteratedEdges.add(e);
					}
					assertEquals(expectedEdges, iteratedEdges);
				}
			}
		}
	}

	@Test
	public void testGetEdge() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				for (int u : g.vertices()) {
					for (int v : g.vertices()) {
						int e = g.getEdge(u, v);
						if (u == v) {
							assertEquals(-1, e);
						} else {
							assertNotEquals(-1, e);
						}
					}
				}
			}
		}
	}

	@Test
	public void testGetEdges() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				for (int u : g.vertices()) {
					for (int v : g.vertices()) {
						EdgeSet edges = g.getEdges(u, v);
						IntSet expectedEdges = u == v ? IntSets.emptySet() : IntSet.of(g.getEdge(u, v));
						assertEquals(expectedEdges, edges);

						IntSet iteratedEdges = new IntOpenHashSet(1);
						for (EdgeIter eit = edges.iterator(); eit.hasNext();) {
							int peekNext = ((EdgeIterImpl) eit).peekNext();
							int e = eit.nextInt();
							assertEquals(peekNext, e);

							assertEquals(u, eit.source());
							assertEquals(v, eit.target());

							iteratedEdges.add(e);
						}
						assertEquals(expectedEdges, iteratedEdges);
					}
				}
			}
		}
	}

	@Test
	public void testRemoveEdges() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				for (int v : g.vertices()) {
					assertThrows(UnsupportedOperationException.class, () -> g.removeEdgesOf(v));
					assertThrows(UnsupportedOperationException.class, () -> g.removeEdgesOutOf(v));
					assertThrows(UnsupportedOperationException.class, () -> g.removeEdgesInOf(v));
				}
			}
		}
	}

	@Test
	public void testClear() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				assertThrows(UnsupportedOperationException.class, () -> g.clear());
				assertThrows(UnsupportedOperationException.class, () -> g.clearEdges());
			}
		}
	}

	@Test
	public void testReverseEdge() {
		for (int n : IntList.of(2, 5, 13, 19, 50, 1237)) {
			Graph g = Graphs.newCompleteGraphDirected(n);
			int m = g.edges().size();
			int eIdx = 0x98154656 % m;
			int e = g.edges().toIntArray()[Math.abs(eIdx)];
			assertThrows(UnsupportedOperationException.class, () -> g.reverseEdge(e));
		}
	}

	@Test
	public void testAllEdgesExistsUniquely() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				ObjectSet<IntCollection> seenEdges = new ObjectOpenHashSet<>(g.edges().size());
				for (int e : g.edges()) {
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					IntCollection edge = directed ? IntList.of(u, v) : IntSet.of(u, v);
					assertFalse(seenEdges.contains(edge));
					seenEdges.add(edge);
				}

				ObjectSet<IntCollection> expectedEdges = new ObjectOpenHashSet<>(g.edges().size());
				for (int u : g.vertices())
					for (int v : g.vertices())
						if (u != v)
							expectedEdges.add(directed ? IntList.of(u, v) : IntSet.of(u, v));
				assertEquals(expectedEdges, seenEdges);
			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testCapabilities() {
		for (int n : IntList.of(0, 1, 2, 5, 13, 19, 50, 1237)) {
			for (boolean directed : BooleanList.of(false, true)) {
				Graph g = directed ? Graphs.newCompleteGraphDirected(n) : Graphs.newCompleteGraphUndirected(n);
				GraphCapabilities capabilities = g.getCapabilities();
				assertFalse(capabilities.selfEdges());
				assertFalse(capabilities.parallelEdges());
				assertEquals(directed, capabilities.directed());
			}
		}
	}

}
