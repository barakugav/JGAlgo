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
package com.jgalgo.internal.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import com.jgalgo.FlowNetwork;
import com.jgalgo.GraphsUtils;
import com.jgalgo.JGAlgoConfig;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.data.Heap;
import com.jgalgo.internal.data.HeapReferenceable;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class Assertions {

	public static class Graphs {

		private static final boolean AssertBipartitePartition = getBoolConfig("AssertionsGraphsBipartitePartition");
		private static final boolean AssertPositiveWeights = getBoolConfig("AssertionsGraphsPositiveWeights");
		private static final boolean AssertIdChecks = getBoolConfig("AssertionsGraphIdCheck");

		public static void onlyDirected(Graph g) {
			if (!g.getCapabilities().directed())
				throw new IllegalArgumentException("only directed graphs are supported");
		}

		public static void onlyUndirected(Graph g) {
			if (g.getCapabilities().directed())
				throw new IllegalArgumentException("only undirected graphs are supported");
		}

		public static void onlyBipartite(IndexGraph g, Weights.Bool partition) {
			if (!AssertBipartitePartition)
				return;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				if (partition.getBool(g.edgeSource(e)) == partition.getBool(g.edgeTarget(e)))
					throw new IllegalArgumentException("the graph is not bipartite");
		}

		public static void noSelfEdges(Graph g, String msg) {
			if (GraphsUtils.containsSelfEdges(g))
				throw new IllegalArgumentException(msg);
		}

		public static void noParallelEdges(Graph g, String msg) {
			if (GraphsUtils.containsParallelEdges(g))
				throw new IllegalArgumentException(msg);
		}

		public static void onlyPositiveEdgesWeights(IndexGraph g, WeightFunction w) {
			if (!AssertPositiveWeights)
				return;
			if (w instanceof WeightFunction.Int) {
				WeightFunction.Int wInt = (WeightFunction.Int) w;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					onlyPositiveWeight(wInt.weightInt(e));
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					onlyPositiveWeight(w.weight(e));
			}
		}

		public static void onlyPositiveWeight(double w) {
			if (!AssertPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		public static void onlyPositiveWeight(int w) {
			if (!AssertPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		public static void checkId(int id, int length) {
			if (!AssertIdChecks)
				return;
			if (id < 0 || id >= length)
				throw new IndexOutOfBoundsException(
						"No such vertex/edge: " + id + " valid range [" + 0 + ", " + length + ")");
		}

		public static void checkVertex(int vertex, int n) {
			if (!AssertIdChecks)
				return;
			if (vertex < 0 || vertex >= n)
				throw new IndexOutOfBoundsException("No such vertex: " + vertex);
		}

		public static void checkEdge(int edge, int m) {
			if (!AssertIdChecks)
				return;
			if (edge < 0 || edge >= m)
				throw new IndexOutOfBoundsException("No such edge: " + edge);
		}

	}

	public static class Flows {

		public static void sourceSinkNotTheSame(int source, int sink) {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex (" + source + ")");
		}

		public static void sourcesSinksNotTheSame(IntCollection sources, IntCollection sinks) {
			if (sources.isEmpty())
				throw new IllegalArgumentException("no sources vertices provided");
			if (sinks.isEmpty())
				throw new IllegalArgumentException("no sinks vertices provided");
			final byte UNSEEN = 0;
			final byte SOURCE = 1;
			final byte TARGET = 2;
			Int2ByteMap types = new Int2ByteOpenHashMap(sources.size() + sinks.size());
			types.defaultReturnValue(UNSEEN);
			for (int v : sources) {
				int vType = types.put(v, SOURCE);
				if (vType != UNSEEN)
					throw new IllegalArgumentException("Source vertex appear twice (" + v + ")");
			}
			for (int v : sinks) {
				int vType = types.put(v, TARGET);
				if (vType != UNSEEN) {
					if (vType == SOURCE)
						throw new IllegalArgumentException("A vertex can't be both a source and target (" + v + ")");
					if (vType == TARGET)
						throw new IllegalArgumentException("Target vertex appear twice (" + v + ")");
				}
			}
		}

		public static void checkLowerBound(IndexGraph g, FlowNetwork net, WeightFunction lowerBound) {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				double l = lowerBound.weight(e);
				if (!(0 <= l && l <= net.getCapacity(e)))
					throw new IllegalArgumentException("Lower bound must be in [0, capacity] for edge " + e);
			}
		}

		public static void checkDemand(IndexGraph g, WeightFunction demand) {
			double sum = 0;
			for (int n = g.vertices().size(), v = 0; v < n; v++) {
				double d = demand.weight(v);
				if (!Double.isFinite(d))
					throw new IllegalArgumentException("Demand must be non-negative for vertex " + v);
				sum += d;
			}
			if (sum != 0)
				throw new IllegalArgumentException("Sum of demand must be zero");
		}
	}

	public static class Arrays {
		public static void checkFromTo(int from, int to, int length) {
			Objects.checkFromToIndex(from, to, length);
		}

		public static void checkIndex(int index, int from, int to) {
			if (index < from || index >= to)
				throw new IndexOutOfBoundsException(
						"Index " + index + " out of bounds for range Range [" + from + ", " + to + ")");
		}
	}

	public static class Iters {

		private static final boolean AssertNotEmpty = getBoolConfig("AssertionsIterNotEmpty");

		public static final String ERR_NO_NEXT = "Iterator has no next element";
		public static final String ERR_NO_PREVIOUS = "Iterator has no previous element";

		public static void hasNext(Iterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasNext())
				throw new NoSuchElementException(ERR_NO_NEXT);
		}

		public static void hasPrevious(ListIterator<?> it) {
			if (!AssertNotEmpty)
				return;
			if (!it.hasPrevious())
				throw new NoSuchElementException(ERR_NO_PREVIOUS);
		}

	}

	public static class Heaps {

		private static final boolean AssertDecreaseKeyLegal = getBoolConfig("AssertionsHeapsDecreaseKeyLegal");
		private static final boolean AssertNotEmpty = getBoolConfig("AssertionsHeapsNotEmpty");
		private static final boolean AssertMeldLegal = getBoolConfig("AssertionsHeapsMeldLegal");

		public static <E> void decreaseKeyIsSmaller(E oldKey, E newKey, Comparator<? super E> cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? JGAlgoUtils.cmpDefault(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static void decreaseKeyIsSmaller(int oldKey, int newKey, IntComparator cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? Integer.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static void decreaseKeyIsSmaller(double oldKey, double newKey, DoubleComparator cmp) {
			if (!AssertDecreaseKeyLegal)
				return;
			int c = cmp == null ? Double.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static <E> void noMeldWithSelf(Heap<E> heap, Heap<? extends E> other) {
			if (!AssertMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		public static <K, V> void noMeldWithSelf(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		@SuppressWarnings("rawtypes")
		public static <K, V> void meldWithSameImpl(Class<? extends HeapReferenceable> impl,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (!impl.isAssignableFrom(other.getClass()))
				throw new IllegalArgumentException("Can't meld heaps with different implementations");
		}

		public static <E> void equalComparatorBeforeMeld(Heap<E> heap, Heap<? extends E> other) {
			if (!AssertMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		public static <K, V> void equalComparatorBeforeMeld(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!AssertMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		public static void notEmpty(Heap<?> heap) {
			if (!AssertNotEmpty)
				return;
			if (heap.isEmpty())
				throw new IllegalStateException("Heap is empty");
		}

		public static void notEmpty(HeapReferenceable<?, ?> heap) {
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
