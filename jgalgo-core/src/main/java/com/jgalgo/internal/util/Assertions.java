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
import com.jgalgo.alg.GraphsUtils;
import com.jgalgo.alg.Trees;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.ds.Heap;
import com.jgalgo.internal.ds.HeapReferenceable;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class Assertions {

	public static class Graphs {

		public static void onlyDirected(Graph<?, ?> g) {
			if (!g.isDirected())
				throw new IllegalArgumentException("only directed graphs are supported");
		}

		public static void onlyUndirected(Graph<?, ?> g) {
			if (g.isDirected())
				throw new IllegalArgumentException("only undirected graphs are supported");
		}

		public static void onlyBipartite(IndexGraph g, IWeightsBool partition) {
			if (!JGAlgoConfigImpl.AssertionsGraphsBipartitePartition)
				return;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				if (partition.get(g.edgeSource(e)) == partition.get(g.edgeTarget(e)))
					throw new IllegalArgumentException("the graph is not bipartite");
		}

		public static void noSelfEdges(IntGraph g, String msg) {
			if (GraphsUtils.containsSelfEdges(g))
				throw new IllegalArgumentException(msg);
		}

		public static void noParallelEdges(IntGraph g, String msg) {
			if (GraphsUtils.containsParallelEdges(g))
				throw new IllegalArgumentException(msg);
		}

		public static void onlyPositiveEdgesWeights(IndexGraph g, IWeightFunction w) {
			if (!JGAlgoConfigImpl.AssertionsGraphsPositiveWeights)
				return;
			if (WeightFunction.isInteger(w)) {
				IWeightFunctionInt wInt = (IWeightFunctionInt) w;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					onlyPositiveWeight(wInt.weightInt(e));
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					onlyPositiveWeight(w.weight(e));
			}
		}

		public static void onlyPositiveWeight(double w) {
			if (!JGAlgoConfigImpl.AssertionsGraphsPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		public static void onlyPositiveWeight(int w) {
			if (!JGAlgoConfigImpl.AssertionsGraphsPositiveWeights)
				return;
			if (w < 0)
				throw new IllegalArgumentException("only positive weights are supported: " + w);
		}

		public static void onlyCardinality(IWeightFunction w) {
			if (!WeightFunction.isCardinality(w))
				throw new IllegalArgumentException("only cardinality shortest path is supported by this algorithm");
		}

		public static void onlyTree(IndexGraph g) {
			if (!JGAlgoConfigImpl.AssertionsGraphsIsTree)
				return;
			if (!Trees.isTree(g))
				throw new IllegalArgumentException("only trees are supported");
		}

		public static void onlyTree(IndexGraph g, int root) {
			if (!JGAlgoConfigImpl.AssertionsGraphsIsTree)
				return;
			if (!Trees.isTree(g, Integer.valueOf(root)))
				throw new IllegalArgumentException("The given graph is not a tree rooted at the given root");
		}

		public static void checkId(int elementIdx, int length, boolean isEdge) {
			if (!JGAlgoConfigImpl.AssertionsGraphIdCheck)
				return;
			if (elementIdx < 0 || elementIdx >= length) {
				if (isEdge) {
					throw NoSuchEdgeException.ofIndex(elementIdx);
				} else {
					throw NoSuchVertexException.ofIndex(elementIdx);
				}
			}
		}

		public static void checkVertex(int vertexIdx, int n) {
			if (!JGAlgoConfigImpl.AssertionsGraphIdCheck)
				return;
			if (vertexIdx < 0 || vertexIdx >= n)
				throw NoSuchVertexException.ofIndex(vertexIdx);
		}

		public static void checkEdge(int edgeIdx, int m) {
			if (!JGAlgoConfigImpl.AssertionsGraphIdCheck)
				return;
			if (edgeIdx < 0 || edgeIdx >= m)
				throw NoSuchEdgeException.ofIndex(edgeIdx);
		}

	}

	public static class Flows {

		public static void sourceSinkNotTheSame(int source, int sink) {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex (idx=" + source + ")");
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
					throw new IllegalArgumentException("Source vertex appear twice (idx=" + v + ")");
			}
			for (int v : sinks) {
				int vType = types.put(v, TARGET);
				if (vType != UNSEEN) {
					if (vType == SOURCE)
						throw new IllegalArgumentException(
								"A vertex can't be both a source and target (idx=" + v + ")");
					if (vType == TARGET)
						throw new IllegalArgumentException("Target vertex appear twice (idx=" + v + ")");
				}
			}
		}

		public static void positiveCapacities(IndexGraph g, IWeightFunction capacity) {
			if (WeightFunction.isCardinality(capacity))
				return;
			if (WeightFunction.isInteger(capacity)) {
				IWeightFunctionInt capacityInt = (IWeightFunctionInt) capacity;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int cap = capacityInt.weightInt(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (idx=" + e + "): " + cap);
				}
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double cap = capacity.weight(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (idx=" + e + "): " + cap);
				}
			}
		}

		public static void positiveCapacity(double capacity) {
			if (capacity < 0)
				throw new IllegalArgumentException("capacity can't be negative: " + capacity);
		}

		public static void flowLessThanCapacity(double flow, double capacity, double EPS) {
			if (flow > capacity + EPS)
				throw new IllegalArgumentException("Illegal flow: " + flow + " > " + capacity);
		}

		public static void flowLessThanCapacity(int flow, int capacity) {
			if (flow > capacity)
				throw new IllegalArgumentException("Illegal flow: " + flow + " > " + capacity);
		}

		public static void checkLowerBound(IndexGraph g, IWeightFunction capacity, IWeightFunction lowerBound) {
			if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(lowerBound)) {
				IWeightFunctionInt capacityInt = (IWeightFunctionInt) capacity;
				IWeightFunctionInt lowerBoundInt = (IWeightFunctionInt) lowerBound;
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int l = lowerBoundInt.weightInt(e);
					int cap = capacityInt.weightInt(e);
					if (!(0 <= l && l <= cap))
						throw new IllegalArgumentException(
								"Lower bound " + l + " of edge with index " + e + " must be in [0, " + cap + "]");
				}
			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double l = lowerBound.weight(e);
					double cap = capacity.weight(e);
					if (!(0 <= l && l <= cap))
						throw new IllegalArgumentException(
								"Lower bound " + l + " of edge with index " + e + " must be in [0, " + cap + "]");
				}
			}
		}

		public static void checkSupply(IndexGraph g, IWeightFunction supply) {
			double sum = 0;
			for (int n = g.vertices().size(), v = 0; v < n; v++) {
				double d = supply.weight(v);
				if (!Double.isFinite(d))
					throw new IllegalArgumentException("Supply must be finite for vertex with index " + v);
				sum += d;
			}
			if (Math.abs(sum) > 1e-6)
				throw new IllegalArgumentException("Sum of supply must be zero");
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

		public static final String ERR_NO_NEXT = "Iterator has no next element";
		public static final String ERR_NO_PREVIOUS = "Iterator has no previous element";

		public static void hasNext(Iterator<?> it) {
			if (!JGAlgoConfigImpl.AssertionsIterNotEmpty)
				return;
			if (!it.hasNext())
				throw new NoSuchElementException(ERR_NO_NEXT);
		}

		public static void hasPrevious(ListIterator<?> it) {
			if (!JGAlgoConfigImpl.AssertionsIterNotEmpty)
				return;
			if (!it.hasPrevious())
				throw new NoSuchElementException(ERR_NO_PREVIOUS);
		}

	}

	public static class Heaps {

		public static <E> void decreaseKeyIsSmaller(E oldKey, E newKey, Comparator<? super E> cmp) {
			if (!JGAlgoConfigImpl.AssertionsHeapsDecreaseKeyLegal)
				return;
			int c = cmp == null ? JGAlgoUtils.cmpDefault(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static void decreaseKeyIsSmaller(int oldKey, int newKey, IntComparator cmp) {
			if (!JGAlgoConfigImpl.AssertionsHeapsDecreaseKeyLegal)
				return;
			int c = cmp == null ? Integer.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static void decreaseKeyIsSmaller(double oldKey, double newKey, DoubleComparator cmp) {
			if (!JGAlgoConfigImpl.AssertionsHeapsDecreaseKeyLegal)
				return;
			int c = cmp == null ? Double.compare(oldKey, newKey) : cmp.compare(oldKey, newKey);
			if (c < 0)
				throw new IllegalArgumentException("New key is greater than existing one");
		}

		public static <E> void noMeldWithSelf(Heap<E> heap, Heap<? extends E> other) {
			if (!JGAlgoConfigImpl.AssertionsHeapsMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		public static <K, V> void noMeldWithSelf(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!JGAlgoConfigImpl.AssertionsHeapsMeldLegal)
				return;
			if (heap == other)
				throw new IllegalArgumentException("A heap can't meld with itself");
		}

		@SuppressWarnings("rawtypes")
		public static <K, V> void meldWithSameImpl(Class<? extends HeapReferenceable> impl,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!JGAlgoConfigImpl.AssertionsHeapsMeldLegal)
				return;
			if (!impl.isAssignableFrom(other.getClass()))
				throw new IllegalArgumentException("Can't meld heaps with different implementations");
		}

		public static <E> void equalComparatorBeforeMeld(Heap<E> heap, Heap<? extends E> other) {
			if (!JGAlgoConfigImpl.AssertionsHeapsMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		public static <K, V> void equalComparatorBeforeMeld(HeapReferenceable<K, V> heap,
				HeapReferenceable<? extends K, ? extends V> other) {
			if (!JGAlgoConfigImpl.AssertionsHeapsMeldLegal)
				return;
			if (!Objects.equals(heap.comparator(), other.comparator()))
				throw new IllegalArgumentException("Can't meld, heaps have different comparators");
		}

		public static void notEmpty(Heap<?> heap) {
			if (!JGAlgoConfigImpl.AssertionsHeapsNotEmpty)
				return;
			if (heap.isEmpty())
				throw new IllegalStateException("Heap is empty");
		}

		public static void notEmpty(HeapReferenceable<?, ?> heap) {
			if (!JGAlgoConfigImpl.AssertionsHeapsNotEmpty)
				return;
			if (heap.isEmpty())
				throw new IllegalStateException("Heap is empty");
		}

	}

}
