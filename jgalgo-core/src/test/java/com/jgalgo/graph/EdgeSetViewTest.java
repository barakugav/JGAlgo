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
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

@SuppressWarnings("boxing")
public class EdgeSetViewTest extends TestBase {

	@Test
	public void idsSet() {
		foreachBoolConfig(directed -> {
			IntGraph g = createIntGraph(directed);
			IntSet edges = new IntOpenHashSet(g.edges().intStream().filter(e -> e % 3 == 2).toArray());
			IEdgeSetView view = new IEdgeSetView(edges, g);
			assertEquals(edges, view.idsSet());
		});
		foreachBoolConfig(directed -> {
			Graph<Integer, Integer> g = createGraph(directed, false);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSetView<Integer, Integer> view = new EdgeSetView<>(edges, g);
			assertEquals(edges, view.idsSet());
		});
	}

	@Test
	public void size() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertEquals(edges.size(), view.size());
			if (!edges.isEmpty()) {
				edges.remove(edges.iterator().nextInt());
				assertEquals(edges.size(), view.size());
			}
			if (!edges.isEmpty()) {
				view.remove(view.iterator().next());
				assertEquals(edges.size(), view.size());
			}
		});
	}

	@Test
	public void isEmpty() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			if (!edges.isEmpty())
				assertFalse(view.isEmpty());
			edges.clear();
			assertTrue(edges.isEmpty());
			assertTrue(view.isEmpty());
		});
	}

	@Test
	public void toArray() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertArrayEquals(edges.toArray(), view.toArray());
			assertArrayEquals(edges.toArray(new Integer[0]), view.toArray(new Integer[0]));
			if (view instanceof IEdgeSet) {
				assertArrayEquals(edges.toIntArray(), ((IEdgeSet) view).toIntArray());
				assertArrayEquals(edges.toArray(new int[0]), ((IEdgeSet) view).toArray(new int[0]));
			}
		});
	}

	@Test
	public void contains() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			for (int e : edges)
				assertTrue(view.contains(e));
			for (int e : g.edges())
				assertEqualsBool(edges.contains(e), view.contains(e));
		});
	}

	@Test
	public void containsAll() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			if (view instanceof IEdgeSet)
				assertTrue(((IEdgeSet) view).containsAll(edges));
			assertTrue(view.containsAll(new HashSet<>(edges)));

			IntSet edges2 = new IntOpenHashSet(edges);
			for (int x = 0; !edges2.add(x); x++);
			if (view instanceof IEdgeSet)
				assertFalse(((IEdgeSet) view).containsAll(edges2));
			assertFalse(view.containsAll(new HashSet<>(edges2)));
		});
	}

	@Test
	public void addAll() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertFalse(view.addAll(edges));
			assertFalse(view.addAll(new HashSet<>(edges)));

			IntSet edges2 = new IntOpenHashSet(edges);
			if (view instanceof IEdgeSet) {
				for (int x = 0; !edges2.add(x); x++);
				assertTrue(((IEdgeSet) view).addAll(edges2));
				assertEquals(edges2, edges);
			}
			for (int x = 0; !edges2.add(x); x++);
			assertTrue(view.addAll(new HashSet<>(edges2)));
			assertEquals(edges2, edges);
		});
	}

	@Test
	public void retainAll() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertFalse(view.retainAll(edges));
			assertFalse(view.retainAll(new HashSet<>(edges)));

			IntSet edges2 = new IntOpenHashSet(edges);
			if (view instanceof IEdgeSet) {
				for (int x = 0; !edges2.remove(x); x++);
				assertEquals(edges2.size() + 1, edges.size());
				assertTrue(((IEdgeSet) view).retainAll(edges2));
				assertEquals(edges2, edges);
			}

			for (int x = 0; !edges2.remove(x); x++);
			assertEquals(edges2.size() + 1, edges.size());
			assertTrue(view.retainAll(new HashSet<>(edges2)));
			assertEquals(edges2, edges);
		});
	}

	@Test
	public void removeAll() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			int nonExistingEdge;
			for (nonExistingEdge = 0; edges.contains(nonExistingEdge); nonExistingEdge++);
			assertFalse(view.removeAll(IntList.of(nonExistingEdge)));
			assertFalse(view.removeAll(List.of(Integer.valueOf(nonExistingEdge))));

			IntSet toRemove = new IntOpenHashSet();
			int sizeBeforeRemove = view.size();
			if (view instanceof IEdgeSet) {
				for (int e : edges) {
					if (toRemove.size() == 2)
						break;
					toRemove.add(e);
				}
				assertTrue(((IEdgeSet) view).removeAll(toRemove));
				assertEquals(sizeBeforeRemove - 2, view.size());
			}

			toRemove.clear();
			sizeBeforeRemove = view.size();
			for (int e : edges) {
				if (toRemove.size() == 2)
					break;
				toRemove.add(e);
			}
			assertTrue(view.removeAll(new HashSet<>(toRemove)));
			assertEquals(sizeBeforeRemove - 2, view.size());
		});
	}

	@Test
	public void clear() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			view.clear();
			assertTrue(view.isEmpty());
			assertTrue(edges.isEmpty());
		});
	}

	@Test
	public void remove() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			int nonExistingEdge;
			for (nonExistingEdge = 0; edges.contains(nonExistingEdge); nonExistingEdge++);
			assertFalse(view.remove(nonExistingEdge));

			int edge = edges.iterator().nextInt();
			assertTrue(view.remove(edge));
			assertFalse(view.remove(edge));
			assertEquals(edges, view);
		});
	}

	@Test
	public void add() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			int nonExistingEdge;
			for (nonExistingEdge = 0; edges.contains(nonExistingEdge); nonExistingEdge++);
			assertTrue(view.add(nonExistingEdge));
			assertFalse(view.add(nonExistingEdge));
			assertEquals(edges, view);
		});
	}

	@Test
	public void equals() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertEquals(edges, view);
			assertEquals(view, edges);

			edges = new IntOpenHashSet(edges);
			for (int x = 0; !edges.add(x); x++);
			assertNotEquals(edges, view);
			assertNotEquals(view, edges);
		});
	}

	@Test
	public void hashCodeTest() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertEquals(edges.hashCode(), view.hashCode());
		});
	}

	@Test
	public void toStringTest() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);
			assertEquals(edges.toString(), view.toString());

			edges = new IntOpenHashSet(edges);
			for (int x = 0; !edges.add(x); x++);
			assertNotEquals(edges.toString(), view.toString());
		});
	}

	@Test
	public void iterator() {
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);

			IntIterator expectedIter = edges.iterator();
			EdgeIter<Integer, Integer> iter = view.iterator();

			assertThrows(IllegalStateException.class, () -> iter.source());
			assertThrows(IllegalStateException.class, () -> iter.target());

			while (expectedIter.hasNext()) {
				assertTrue(iter.hasNext());
				int expectedEdge = expectedIter.nextInt();
				int expectedSource = g.edgeSource(expectedEdge);
				int expectedTarget = g.edgeTarget(expectedEdge);
				int peek;
				boolean peekSuccess;
				try {
					peek = iter.peekNext();
					peekSuccess = true;
				} catch (UnsupportedOperationException e) {
					peek = -1;
					peekSuccess = false;
				}
				int edge = iter.next();
				int source = iter.source();
				int target = iter.target();
				if (peekSuccess)
					assertEquals(expectedEdge, peek);
				assertEquals(expectedEdge, edge);
				assertEquals(expectedSource, source);
				assertEquals(expectedTarget, target);
			}
			assertFalse(iter.hasNext());
		});
		foreachBoolConfig((directed, intGraph) -> {
			Graph<Integer, Integer> g = createGraph(directed, intGraph);
			IntSet edges = new IntOpenHashSet(g.edges().stream().filter(e -> e % 3 == 2).collect(toSet()));
			EdgeSet<Integer, Integer> view = EdgeSet.of(edges, g);

			int edgeToRemove = edges.toIntArray()[edges.size() / 2];
			EdgeIter<Integer, Integer> iter = view.iterator();
			boolean removed = false;
			while (iter.hasNext()) {
				int edge = iter.next();
				if (edge == edgeToRemove) {
					assertFalse(removed);
					iter.remove();
					removed = true;
				}
			}
			assertTrue(removed);
			assertFalse(edges.contains(edgeToRemove));
			assertEquals(edges, view);
		});
	}

	private static IntGraph createIntGraph(boolean directed) {
		return (IntGraph) createGraph(directed, true);
	}

	private static Graph<Integer, Integer> createGraph(boolean directed, boolean intGraph) {
		final Random rand = new Random(0x3de603ab33e9d63fL);
		Graph<Integer, Integer> g = intGraph ? IntGraph.newDirected() : Graph.newDirected();
		g.addVertices(range(20));
		while (g.edges().size() < 40) {
			int u = Graphs.randVertex(g, rand);
			int v = Graphs.randVertex(g, rand);
			if (!g.isAllowSelfEdges() && u == v)
				continue;
			if (!g.isAllowParallelEdges() && g.containsEdge(u, v))
				continue;
			int e = rand.nextInt(1 + g.edges().size() * 2);
			if (g.edges().contains(e))
				continue;
			g.addEdge(u, v, e);
		}
		return g;
	}

}
