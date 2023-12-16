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
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBaseMutable extends IndexGraphBase {

	private final boolean isAllowSelfEdges;
	private final boolean isAllowParallelEdges;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	private final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	private final DataContainer.Long edgeEndpointsContainer;

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities.isDirected, 0, 0, true);
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		verticesInternalContainers = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalContainers = new DataContainer.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedVerticesNum, false);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedEdgesNum, true);

		edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);
	}

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities.isDirected, g.vertices().size(), g.edges().size(), true);
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		if (isDirected()) {
			Assertions.Graphs.onlyDirected(g);
		} else {
			Assertions.Graphs.onlyUndirected(g);
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size(), false);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size(), true);
		if (copyVerticesWeights) {
			for (String key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesWeights(key), vertices, false));
		}
		if (copyEdgesWeights) {
			for (String key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesWeights(key), edges, true));
		}

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(vertices);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());

		if (g instanceof GraphBaseMutable) {
			GraphBaseMutable g0 = (GraphBaseMutable) g;
			edgeEndpointsContainer = copyEdgesContainer(g0.edgeEndpointsContainer, newArr -> edgeEndpoints = newArr);
		} else {

			final int m = edges.size();
			edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);
			for (int e = 0; e < m; e++)
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}

		if (!capabilities.isAllowSelfEdges) {
			if (g.isAllowSelfEdges()) {
				checkNoSelfEdges();
			} else {
				assert checkNoSelfEdges();
			}
		}
	}

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl.Artifacts builder) {
		super(capabilities.isDirected, builder.vertices.copy(), builder.edges.copy());
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.verticesUserWeights, vertices);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.edgesUserWeights, edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());

		final int m = edges.size();
		edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);

		for (int e = 0; e < m; e++)
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));

		if (!capabilities.isAllowSelfEdges)
			checkNoSelfEdges();
	}

	static class Capabilities {
		private Capabilities(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			this.isDirected = isDirected;
			this.isAllowSelfEdges = isAllowSelfEdges;
			this.isAllowParallelEdges = isAllowParallelEdges;
		}

		static Capabilities of(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			return new Capabilities(isDirected, isAllowSelfEdges, isAllowParallelEdges);
		}

		private final boolean isDirected;
		private final boolean isAllowSelfEdges;
		private final boolean isAllowParallelEdges;

	}

	@Override
	public final boolean isAllowSelfEdges() {
		return isAllowSelfEdges;
	}

	@Override
	public final boolean isAllowParallelEdges() {
		return isAllowParallelEdges;
	}

	private final GraphElementSet.Mutable vertices0() {
		return (GraphElementSet.Mutable) vertices;
	}

	private final GraphElementSet.Mutable edges0() {
		return (GraphElementSet.Mutable) edges;
	}

	@Override
	public int addVertexInt() {
		int u = vertices0().add();
		assert u >= 0;
		ensureVertexCapacity(u + 1);
		return u;
	}

	@Override
	public final void addVertices(Collection<? extends Integer> vertices) {
		int currentNum = this.vertices.size();
		if (!isRange(currentNum, vertices))
			throw new IllegalArgumentException("added vertices must be a consecutive range of integers starting from "
					+ currentNum + " but was " + vertices);
		addVerticesImpl(vertices.size());
	}

	static boolean isSortedRange(int from, Collection<? extends Integer> elements) {
		if (elements.isEmpty())
			return true;

		/* GraphElementSet are 0,1,2,3,... */
		if (elements instanceof GraphElementSet)
			return from == 0;

		/* If the given collection is a range, Range.equals() will simply compare 'from' and 'to' */
		int num = elements.size();
		if (elements instanceof Range)
			return range(from, from + num).equals(elements);

		/* Check naively if the elements are sorted range */
		int expectedNextElm = from;
		for (int elm : elements) {
			if (elm != expectedNextElm)
				return false;
			expectedNextElm = elm + 1;
		}
		return true;
	}

	static boolean isRange(int from, Collection<? extends Integer> elements) {
		if (isSortedRange(from, elements))
			return true;
		int num = elements.size();

		/* If the given collection is a set, we know there are no duplications, compare to Range obj */
		if (elements instanceof Set)
			return range(from, from + num).equals(elements);

		/* Lastly, use the robust method which uses non constant memory */
		Bitmap bitmap = new Bitmap(num);
		for (int elm : elements) {
			if (elm < from || elm >= from + num || bitmap.get(elm - from))
				return false;
			bitmap.set(elm - from);
		}
		return true;
	}

	void addVerticesImpl(int count) {
		vertices0().addAll(count);
		ensureVertexCapacity(vertices.size());
	}

	@Override
	public final void removeVertex(int vertex) {
		checkVertex(vertex);
		removeEdgesOf(vertex);

		if (vertex == vertices.size - 1) {
			removeVertexLast(vertex);
		} else {
			vertexSwapAndRemove(vertex, vertices.size - 1);
		}
	}

	void removeVertexLast(int vertex) {
		verticesUserWeights.clearElement(vertex);
		vertices0().removeIdx(vertex);
	}

	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// verticesInternalContainers.swapElements(removedIdx, swappedIdx);
		verticesUserWeights.swapAndClear(removedIdx, swappedIdx);
		vertices0().swapAndRemove(removedIdx, swappedIdx);
	}

	abstract void markVertex(int vertex);

	abstract void unmarkVertex(int vertex);

	abstract boolean isMarkedVertex(int vertex);

	@Override
	public void removeVertices(Collection<? extends Integer> vertices) {
		/*
		 * We want to remove multiple elements at once, but due to the index graph invariants, we can't simply remove
		 * them in any order, as swaps may be required. If we want to remove k elements, we first mark the the ones
		 * which are in the last k positions, and then iterate over the last k elements (out of all the elements) one
		 * after another, and either remove it if it is marked, or swap it with an element which is not in the last k
		 * positions. Unfortunately, this requires an allocation of size k.
		 */

		@SuppressWarnings("unchecked")
		IntCollection vertices0 = IntAdapters.asIntCollection((Collection<Integer>) vertices);
		final int removeSize = vertices.size();
		final int removeUpTo = this.vertices.size - removeSize;
		Bitmap lastVerticesMarks = new Bitmap(removeSize);
		int markedVerticesNum = 0;
		try {
			for (int v : vertices0) {
				checkVertex(v);
				if (isMarkedVertex(v))
					throw new IllegalArgumentException("duplicate vertex in removed collection: " + v);
				if (v >= removeUpTo)
					lastVerticesMarks.set(v - removeUpTo);
				markVertex(v);
				markedVerticesNum++;
			}
		} finally {
			for (IntIterator vit = vertices0.iterator(); markedVerticesNum-- > 0;)
				unmarkVertex(vit.nextInt());
		}

		if (lastVerticesMarks.cardinality() == removeSize) {
			/* simply remove last k vertices */
			for (int lastVertex = this.vertices.size - 1; lastVertex >= removeUpTo; lastVertex--) {
				removeEdgesOf(lastVertex);
				removeVertexLast(lastVertex);
			}

		} else {
			/* we must copy the indices, as the given collection of vertices may relay on the current indexing */
			IntArrayList nonLastVertices = new IntArrayList(removeSize);
			for (int v : vertices0)
				if (v < removeUpTo)
					nonLastVertices.add(v);
			for (int lastVertex = this.vertices.size - 1; lastVertex >= removeUpTo; lastVertex--) {
				if (lastVerticesMarks.get(lastVertex - removeUpTo)) {
					/* remove last vertex */
					removeEdgesOf(lastVertex);
					removeVertexLast(lastVertex);
				} else {
					/* swap and remove */
					int removedVertex = nonLastVertices.popInt();
					removeEdgesOf(removedVertex);
					vertexSwapAndRemove(removedVertex, lastVertex);
				}
			}
			assert nonLastVertices.isEmpty();
		}
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		checkNewEdgeEndpoints(source, target);

		int e = edges0().add();

		assert e >= 0;
		ensureEdgeCapacity(e + 1);

		setEndpoints(e, source, target);
		return e;
	}

	@Override
	public final void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		@SuppressWarnings("unchecked")
		EdgeSet<Integer, Integer> edges0 = (EdgeSet<Integer, Integer>) edges;
		final Set<Integer> edgesIds;
		if (edges instanceof IEdgeSetView) {
			edgesIds = ((IEdgeSetView) edges).idsSet();
		} else if (edges instanceof EdgeSetView) {
			edgesIds = ((EdgeSetView<Integer, Integer>) edges0).idsSet();
		} else {
			edgesIds = edges0;
		}
		if (isSortedRange(this.edges.size, edgesIds)) {
			if (edges instanceof IEdgeSet) {
				/* the variant with 're-assign ids' assigns indices by the iteration order */
				addEdgesReassignIds((IEdgeSet) edges);
				return;
			}

			int addedEdges = 0;
			try {
				for (EdgeIter<Integer, Integer> iter = edges0.iterator(); iter.hasNext();) {
					Integer e = iter.next();
					assert e.intValue() == this.edges.size; /* isSortedRange() */
					int source = iter.source().intValue();
					int target = iter.target().intValue();
					addEdge(source, target);
					addedEdges++;
				}
			} catch (RuntimeException e) {
				while (addedEdges-- > 0)
					rollBackLastEdge();
				throw e;
			}

		} else {
			/* The edges are not a sorted range. They may still be the valid range, simply unsorted. */
			/* Unfortunately, the following implementation requires an allocation. */

			int currentNum = this.edges.size();
			long[] newEdgesEndpoints = new long[edges.size()];
			Arrays.fill(newEdgesEndpoints, EndpointNone);
			var op = new Object() {
				void accept(int edge, int source, int target) {
					if (source < 0)
						throw NoSuchVertexException.ofIndex(source);
					if (target < 0)
						throw NoSuchVertexException.ofIndex(target);
					int eIdx = edge - currentNum;
					if (eIdx < 0 || eIdx >= newEdgesEndpoints.length || newEdgesEndpoints[eIdx] != EndpointNone)
						throw new IllegalArgumentException(
								"added edges must be a consecutive range of integers starting from " + currentNum
										+ " but was " + edges);
					newEdgesEndpoints[eIdx] = sourceTarget2Endpoints(source, target);
				}
			};
			if (edges instanceof IEdgeSet) {
				for (IEdgeIter iter = ((IEdgeSet) edges).iterator(); iter.hasNext();) {
					int e = iter.nextInt();
					int source = iter.sourceInt();
					int target = iter.targetInt();
					op.accept(e, source, target);
				}
			} else {
				for (EdgeIter<Integer, Integer> iter = edges0.iterator(); iter.hasNext();) {
					int e = iter.next().intValue();
					int source = iter.source().intValue();
					int target = iter.target().intValue();
					op.accept(e, source, target);
				}
			}

			int addedEdges = 0;
			try {
				for (long endpoints : newEdgesEndpoints) {
					int source = endpoints2Source(endpoints);
					int target = endpoints2Target(endpoints);
					addEdge(source, target);
					addedEdges++;
				}
			} catch (RuntimeException e) {
				while (addedEdges-- > 0)
					rollBackLastEdge();
				throw e;
			}
		}
	}

	@Override
	public final IntSet addEdgesReassignIds(IEdgeSet edges) {
		int addedEdges = 0;
		try {
			for (IEdgeIter iter = edges.iterator(); iter.hasNext();) {
				iter.nextInt(); /* ignore edge ID */
				int source = iter.sourceInt();
				int target = iter.targetInt();
				addEdge(source, target);
				addedEdges++;
			}
		} catch (RuntimeException e) {
			while (addedEdges-- > 0)
				rollBackLastEdge();
			throw e;
		}
		return range(this.edges.size - addedEdges, this.edges.size);
	}

	void checkNewEdgeEndpoints(int source, int target) {
		if (!isAllowSelfEdges() && source == target)
			throw new IllegalArgumentException("Self edges are not allowed");
		if (!isAllowParallelEdges() && getEdge(source, target) >= 0)
			throw new IllegalArgumentException(
					"Edge (idx=" + source + ",idx=" + target + ") already exists. Parallel edges are not allowed.");
	}

	@Override
	public final void removeEdge(int edge) {
		checkEdge(edge);
		if (edge == edges.size - 1) {
			removeEdgeLast(edge);
		} else {
			int swappedIdx = edges.size - 1;
			edgeSwapAndRemove(edge, swappedIdx);
		}
	}

	/* identical to removeEdge, without notifying listeners, and always last edge */
	private void rollBackLastEdge() {
		int edge = edges.size - 1;
		rollBackEdge = true;
		removeEdgeLast(edge);
		rollBackEdge = false;
	}

	private boolean rollBackEdge;

	void removeEdgeLast(int edge) {
		edgesUserWeights.clearElement(edge);
		if (rollBackEdge) {
			edges0().rollBackAdd(edge);
		} else {
			edges0().removeIdx(edge);
		}
		clear(edgeEndpoints, edge, DefaultEndpoints);
	}

	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// edgesInternalContainers.swapElements(removedIdx, swappedIdx);
		edgesUserWeights.swapAndClear(removedIdx, swappedIdx);
		edges0().swapAndRemove(removedIdx, swappedIdx);
		swapAndClear(edgeEndpoints, removedIdx, swappedIdx, DefaultEndpoints);
	}

	@Override
	public final void removeEdges(Collection<? extends Integer> edges) {
		/*
		 * We want to remove multiple elements at once, but due to the index graph invariants, we can't simply remove
		 * them in any order, as swaps may be required. If we want to remove k elements, we first mark the the ones
		 * which are in the last k positions, and then iterate over the last k elements (out of all the elements) one
		 * after another, and either remove it if it is marked, or swap it with an element which is not in the last k
		 * positions. Unfortunately, this requires an allocation of size k.
		 */

		@SuppressWarnings("unchecked")
		IntCollection edges0 = IntAdapters.asIntCollection((Collection<Integer>) edges);
		final int removeSize = edges.size();
		final int removeUpTo = this.edges.size - removeSize;
		Bitmap lastEdgesMarks = new Bitmap(removeSize);
		IntConsumer mark = e -> replaceEdgeSource(e, -source(e) - 1);
		IntConsumer unmark = e -> replaceEdgeSource(e, -source(e) - 1);
		IntPredicate isMarked = e -> source(e) < 0;
		int markedEdgesNum = 0;
		try {
			for (int e : edges0) {
				checkEdge(e);
				if (isMarked.test(e))
					throw new IllegalArgumentException("duplicate edge in removed collection: " + e);
				if (e >= removeUpTo)
					lastEdgesMarks.set(e - removeUpTo);
				mark.accept(e);
				markedEdgesNum++;
			}
		} finally {
			for (IntIterator eit = edges0.iterator(); markedEdgesNum-- > 0;)
				unmark.accept(eit.nextInt());
		}

		if (lastEdgesMarks.cardinality() == removeSize) {
			/* simply remove last k edges */
			for (int lastEdge = this.edges.size - 1; lastEdge >= removeUpTo; lastEdge--)
				removeEdgeLast(lastEdge);

		} else {
			/* we must copy the indices, as the given collection of edges may relay on the current indexing */
			IntArrayList nonLastEdges = new IntArrayList(removeSize);
			for (int e : edges0)
				if (e < removeUpTo)
					nonLastEdges.add(e);
			for (int lastEdge = this.edges.size - 1; lastEdge >= removeUpTo; lastEdge--) {
				if (lastEdgesMarks.get(lastEdge - removeUpTo)) {
					/* remove last edge */
					removeEdgeLast(lastEdge);
				} else {
					/* swap and remove */
					int removedEdge = nonLastEdges.popInt();
					edgeSwapAndRemove(removedEdge, lastEdge);
				}
			}
			assert nonLastEdges.isEmpty();
		}
	}

	void replaceEdgeSource(int edge, int newSource) {
		long endpoints = edgeEndpoints[edge];
		int target = endpoints2Target(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(newSource, target);
	}

	void replaceEdgeTarget(int edge, int newTarget) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, newTarget);
	}

	void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		int target = endpoints2Target(endpoints);
		if (source == oldEndpoint)
			source = newEndpoint;
		if (target == oldEndpoint)
			target = newEndpoint;
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, target);
	}

	@Override
	public void clear() {
		clearEdges();
		vertices.clear();
		// internal weights are handled manually
		// verticesInternalContainers.clearContainers();
		verticesUserWeights.clearContainers();
	}

	@Override
	public void clearEdges() {
		edges.clear();
		// internal weights are handled manually
		// edgesInternalContainers.clearContainers();
		edgesUserWeights.clearContainers();
		edgeEndpointsContainer.clear();
	}

	@Override
	public void ensureVertexCapacity(int vertexCapacity) {
		verticesInternalContainers.ensureCapacity(vertexCapacity);
		verticesUserWeights.ensureCapacity(vertexCapacity);
	}

	@Override
	public void ensureEdgeCapacity(int edgeCapacity) {
		edgesInternalContainers.ensureCapacity(edgeCapacity);
		edgesUserWeights.ensureCapacity(edgeCapacity);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		verticesUserWeights.removeWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		return edgesUserWeights.getWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		edgesUserWeights.removeWeights(key);
	}

	<T> DataContainer.Obj<T> newVerticesContainer(T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
		return addVerticesContainer(new DataContainer.Obj<>(vertices, defVal, emptyArr, onArrayAlloc));
	}

	DataContainer.Int newVerticesIntContainer(int defVal, Consumer<int[]> onArrayAlloc) {
		return addVerticesContainer(new DataContainer.Int(vertices, defVal, onArrayAlloc));
	}

	<T> DataContainer.Obj<T> newEdgesContainer(T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Obj<>(edges, defVal, emptyArr, onArrayAlloc));
	}

	DataContainer.Int newEdgesIntContainer(int defVal, Consumer<int[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Int(edges, defVal, onArrayAlloc));
	}

	DataContainer.Long newEdgesLongContainer(long defVal, Consumer<long[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Long(edges, defVal, onArrayAlloc));
	}

	DataContainer.Int copyVerticesContainer(DataContainer.Int container, Consumer<int[]> onArrayAlloc) {
		return addVerticesContainer(container.copy(vertices, onArrayAlloc));
	}

	<T> DataContainer.Obj<T> copyVerticesContainer(DataContainer.Obj<T> container, T[] emptyArr,
			Consumer<T[]> onArrayAlloc) {
		return addVerticesContainer(container.copy(vertices, emptyArr, onArrayAlloc));
	}

	DataContainer.Long copyEdgesContainer(DataContainer.Long container, Consumer<long[]> onArrayAlloc) {
		return addEdgesContainer(container.copy(edges, onArrayAlloc));
	}

	DataContainer.Int copyEdgesContainer(DataContainer.Int container, Consumer<int[]> onArrayAlloc) {
		return addEdgesContainer(container.copy(edges, onArrayAlloc));
	}

	private <ContainerT extends DataContainer> ContainerT addVerticesContainer(ContainerT container) {
		verticesInternalContainers.addContainer(container);
		return container;
	}

	private <ContainerT extends DataContainer> ContainerT addEdgesContainer(ContainerT container) {
		edgesInternalContainers.addContainer(container);
		return container;
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, false, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, true, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public void addVertexRemoveListener(IndexRemoveListener listener) {
		vertices.addRemoveListener(listener);
	}

	@Override
	public void removeVertexRemoveListener(IndexRemoveListener listener) {
		vertices.removeRemoveListener(listener);
	}

	@Override
	public void addEdgeRemoveListener(IndexRemoveListener listener) {
		edges.addRemoveListener(listener);
	}

	@Override
	public void removeEdgeRemoveListener(IndexRemoveListener listener) {
		edges.removeRemoveListener(listener);
	}

	void checkVertex(int vertex) {
		Assertions.Graphs.checkVertex(vertex, vertices.size);
	}

	void checkEdge(int edge) {
		Assertions.Graphs.checkEdge(edge, edges.size);
	}

	static void clear(int[] dataContainer, int idx, int defaultVal) {
		dataContainer[idx] = defaultVal;
	}

	static void clear(long[] dataContainer, int idx, long defaultVal) {
		dataContainer[idx] = defaultVal;
	}

	void swapAndClear(int[] dataContainer, int removedIdx, int swappedIdx, int defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}

	void swapAndClear(long[] dataContainer, int removedIdx, int swappedIdx, long defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}

	<T> void swapAndClear(T[] dataContainer, int removedIdx, int swappedIdx, T defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}

	private boolean checkNoSelfEdges() {
		for (int m = edges().size(), e = 0; e < m; e++)
			if (source(e) == target(e))
				throw new IllegalArgumentException("Self edges are not allowed");
		return true;
	}
}
