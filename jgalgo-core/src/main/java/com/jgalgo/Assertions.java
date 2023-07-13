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

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.IntComparator;

class Assertions {

	static class Graphs {

		private static final boolean AssertBipartitePartition = getBoolConfig("AssertionsGraphsBipartitePartition");
		private static final boolean AssertPositiveWeights = getBoolConfig("AssertionsGraphsPositiveWeights");
		private static final boolean AssertIdChecks = getBoolConfig("AssertionsGraphIdCheck");

		static void onlyDirected(Graph g) {
			if (!g.getCapabilities().directed())
				throw new IllegalArgumentException("only directed graphs are supported");
		}

		static void onlyUndirected(Graph g) {
			if (g.getCapabilities().directed())
				throw new IllegalArgumentException("only undirected graphs are supported");
		}

		static void onlyBipartite(Graph g, Weights.Bool partition) {
			if (!AssertBipartitePartition)
				return;
			if (Bipartite.isValidBipartitePartition(g, partition))
				throw new IllegalArgumentException("the graph is not bipartite");
		}

		static void noSelfEdges(Graph g, String msg) {
			if (GraphsUtils.containsSelfEdges(g))
				throw new IllegalArgumentException(msg);
		}

		static void noParallelEdges(Graph g, String msg) {
			if (GraphsUtils.containsParallelEdges(g))
				throw new IllegalArgumentException(msg);
		}

		static void onlyPositiveEdgesWeights(Graph g, WeightFunction w) {
			if (!AssertPositiveWeights)
				return;
			if (w instanceof WeightFunction.Int) {
				WeightFunction.Int wInt = (WeightFunction.Int) w;
				for (int e : g.edges())
					onlyPositiveWeight(wInt.weightInt(e));
			} else {
				for (int e : g.edges())
					onlyPositiveWeight(w.weight(e));
			}
		}

		static void onlyPositiveWeight(double w) {
			if (!AssertPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		static void onlyPositiveWeight(int w) {
			if (!AssertPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		static void sourceSinkNotTheSame(int source, int sink) {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex (" + source + ")");
		}

		static void checkId(int id, int length) {
			if (!AssertIdChecks)
				return;
			if (id < 0 || id >= length)
				throw new IndexOutOfBoundsException(
						"No such vertex/edge: " + id + " valid range [" + 0 + ", " + length + ")");
		}

		static void checkVertex(int vertex, int n) {
			if (!AssertIdChecks)
				return;
			if (vertex < 0 || vertex >= n)
				throw new IndexOutOfBoundsException("No such vertex: " + vertex);
		}

		static void checkEdge(int edge, int m) {
			if (!AssertIdChecks)
				return;
			if (edge < 0 || edge >= m)
				throw new IndexOutOfBoundsException("No such edge: " + edge);
		}

	}

	static class Arrays {
		static void checkFromTo(int from, int to, int length) {
			Objects.checkFromToIndex(from, to, length);
		}

		static void checkIndex(int index, int from, int to) {
			if (index < from || index >= to)
				throw new IndexOutOfBoundsException(
						"Index " + index + " out of bounds for range Range [" + from + ", " + to + ")");
		}
	}

	static class Iters {

		private static final boolean AssertNotEmpty = getBoolConfig("AssertionsIterNotEmpty");

		static final String ERR_NO_NEXT = "Iterator has no next element";
		static final String ERR_NO_PREVIOUS = "Iterator has no previous element";

		static void hasNext(Iterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasNext())
				throw new NoSuchElementException(ERR_NO_NEXT);
		}

		static void hasPrevious(ListIterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasPrevious())
				throw new NoSuchElementException(ERR_NO_PREVIOUS);
		}

	}

	static class Heaps {

		private static final boolean AssertDecreaseKeyLegal = getBoolConfig("AssertionsHeapsDecreaseKeyLegal");
		private static final boolean AssertNotEmpty = getBoolConfig("AssertionsHeapsNotEmpty");
		private static final boolean AssertMeldLegal = getBoolConfig("AssertionsHeapsMeldLegal");

		static <E> void decreaseKeyIsSmaller(E oldKey, E newKey, Comparator<? super E> cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? Utils.cmpDefault(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		static void decreaseKeyIsSmaller(int oldKey, int newKey, IntComparator cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? Integer.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		static void decreaseKeyIsSmaller(double oldKey, double newKey, DoubleComparator cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? Double.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		static <E> void noMeldWithSelf(Heap<E> heap, Heap<? extends E> other) {
			if (!AssertMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		static <K, V> void noMeldWithSelf(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		@SuppressWarnings("rawtypes")
		static <K, V> void meldWithSameImpl(Class<? extends HeapReferenceable> impl,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (!impl.isAssignableFrom(other.getClass()))
				throw new IllegalArgumentException("Can't meld heaps with different implementations");
		}

		static <E> void equalComparatorBeforeMeld(Heap<E> heap, Heap<? extends E> other) {
			if (!AssertMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		static <K, V> void equalComparatorBeforeMeld(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		static void notEmpty(Heap<?> heap) {
			if (!AssertNotEmpty)
				return;
			if (heap.isEmpty())
				throw new IllegalStateException("Heap is empty");
		}

		static void notEmpty(HeapReferenceable<?, ?> heap) {
			if (!AssertNotEmpty)
				return;
			if (heap.isEmpty())
				throw new IllegalStateException("Heap is empty");
		}

	}

	private static boolean getBoolConfig(String name) {
		return ((Boolean) JGAlgoConfig.getOption(name).get()).booleanValue();
	}

}
