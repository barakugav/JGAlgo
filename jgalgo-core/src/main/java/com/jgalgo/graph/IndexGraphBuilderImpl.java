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
import java.util.Objects;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;

class IndexGraphBuilderImpl implements IndexGraphBuilder {

	private final boolean directed;
	final GraphElementSet.Mutable vertices;
	final GraphElementSet.Mutable edges;
	private int[] endpoints = IntArrays.EMPTY_ARRAY;

	final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	IndexGraphFactoryImpl.MutableImpl mutableImpl;
	IndexGraphFactoryImpl.ImmutableImpl immutableImpl;

	IndexGraphBuilderImpl(boolean directed) {
		this.directed = directed;
		vertices = GraphElementSet.Mutable.ofVertices(0);
		edges = GraphElementSet.Mutable.ofEdges(0);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(0, true);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(0, false);
		setDefaultImpls();
	}

	IndexGraphBuilderImpl(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		this.directed = g.isDirected();
		final int n = g.vertices().size();
		final int m = g.edges().size();

		if (!(g instanceof IndexGraph)) {
			if (!range(n).equals(g.vertices()))
				throw new IllegalArgumentException("vertices must be 0,1,2,...,n-1 but was " + g.vertices());
			if (!range(m).equals(g.edges()))
				throw new IllegalArgumentException("edges must be 0,1,2,...,m-1 but was " + g.edges());
		}

		vertices = GraphElementSet.Mutable.ofVertices(n);
		edges = GraphElementSet.Mutable.ofEdges(m);

		endpoints = new int[m * 2];

		if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			for (int e : range(m)) {
				int u = g0.edgeSource(e);
				int v = g0.edgeTarget(e);
				setEdgeEndpoints(e, u, v);
			}
		} else {
			for (int e : range(m)) {
				Integer e0 = Integer.valueOf(e);
				int u = g.edgeSource(e0).intValue();
				int v = g.edgeTarget(e0).intValue();
				setEdgeEndpoints(e, u, v);
			}
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size(), true);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size(), false);
		if (copyVerticesWeights)
			for (String key : g.verticesWeightsKeys())
				verticesUserWeights
						.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.verticesWeights(key), vertices, true));
		if (copyEdgesWeights)
			for (String key : g.edgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.edgesWeights(key), edges, false));

		setDefaultImpls();
	}

	private void setDefaultImpls() {
		mutableImpl = new IndexGraphFactoryImpl(directed).mutableImpl();
		immutableImpl = new IndexGraphFactoryImpl(directed).immutableImpl();
	}

	void setMutableImpl(IndexGraphFactoryImpl.MutableImpl mutableImpl) {
		this.mutableImpl = Objects.requireNonNull(mutableImpl);
	}

	void setImmutableImpl(IndexGraphFactoryImpl.ImmutableImpl immutableImpl) {
		this.immutableImpl = Objects.requireNonNull(immutableImpl);
	}

	@Override
	public IntSet vertices() {
		return vertices;
	}

	@Override
	public IntSet edges() {
		return edges;
	}

	@Override
	public int addVertexInt() {
		int u = vertices.add();
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (!GraphBaseMutable.isRange(this.vertices.size, vertices))
			throw new IllegalArgumentException("added vertices must be a consecutive range of integers starting from "
					+ this.vertices.size + " but was " + vertices);

		this.vertices.addAll(vertices.size());
		verticesUserWeights.ensureCapacity(this.vertices.size);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edges.add();
		ensureEdgeCapacity(e + 1);
		setEdgeEndpoints(e, source, target);
		return e;
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		@SuppressWarnings("unchecked")
		EdgeSet<Integer, Integer> edges0 = (EdgeSet<Integer, Integer>) edges;
		final Set<Integer> edgesIds;
		if (edges instanceof IEdgeSetView) {
			edgesIds = ((IEdgeSetView) edges).idsSet();
		} else {
			edgesIds = edges0;
		}
		if (GraphBaseMutable.isSortedRange(this.edges.size, edgesIds)) {
			if (edges instanceof IEdgeSet) {
				/* the variant with 're-assign ids' assigns indices by the iteration order */
				addEdgesReassignIds((IEdgeSet) edges);
				return;
			}

			checkEndpoints(edges0);
			int eIdx = this.edges.size;
			this.edges.addAll(edges.size());
			ensureEdgeCapacity(this.edges.size);
			for (EdgeIter<Integer, Integer> iter = edges0.iterator(); iter.hasNext();) {
				Integer e = iter.next();
				assert e.intValue() == eIdx; /* isSortedRange() */
				int source = iter.source().intValue();
				int target = iter.target().intValue();
				setEdgeEndpoints(eIdx++, source, target);
			}

		} else {
			/* The edges are not a sorted range. They may still be the valid range, simply unsorted. */
			/* Unfortunately, the following implementation requires an allocation. */

			int currentNum = this.edges.size();
			int[] newEdgesEndpoints = new int[edges.size() * 2];
			Arrays.fill(newEdgesEndpoints, -1);
			var op = new Object() {
				void accept(int edge, int source, int target) {
					checkVertex(source);
					checkVertex(target);
					int eIdx = edge - currentNum;
					if (eIdx < 0 || eIdx * 2 >= newEdgesEndpoints.length || newEdgesEndpoints[2 * eIdx] >= 0)
						throw new IllegalArgumentException(
								"added edges must be a consecutive range of integers starting from " + currentNum
										+ " but was " + edges);
					newEdgesEndpoints[eIdx * 2 + 0] = source;
					newEdgesEndpoints[eIdx * 2 + 1] = target;
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

			this.edges.addAll(edges.size());
			ensureEdgeCapacity(this.edges.size);
			for (int eIdx : range(newEdgesEndpoints.length / 2)) {
				int source = newEdgesEndpoints[eIdx * 2 + 0];
				int target = newEdgesEndpoints[eIdx * 2 + 1];
				setEdgeEndpoints(currentNum + eIdx, source, target);
			}
		}
	}

	@Override
	public IntSet addEdgesReassignIds(IEdgeSet edges) {
		checkEndpoints(edges);

		final int addedNum = edges.size();
		int eIdx = this.edges.size;
		this.edges.addAll(addedNum);
		ensureEdgeCapacity(this.edges.size);
		for (IEdgeIter eit = edges.iterator(); eit.hasNext(); eIdx++) {
			eit.nextInt(); /* ignore edge ID */
			int source = eit.sourceInt();
			int target = eit.targetInt();
			setEdgeEndpoints(eIdx, source, target);
		}
		return range(this.edges.size - addedNum, this.edges.size);
	}

	private void checkEndpoints(EdgeSet<Integer, Integer> edges) {
		if (edges instanceof IEdgeSet) {
			for (IEdgeIter iter = ((IEdgeSet) edges).iterator(); iter.hasNext();) {
				iter.nextInt();
				checkVertex(iter.sourceInt());
				checkVertex(iter.targetInt());
			}
		} else {
			for (EdgeIter<Integer, Integer> iter = edges.iterator(); iter.hasNext();) {
				iter.next();
				checkVertex(iter.source().intValue());
				checkVertex(iter.target().intValue());
			}
		}
	}

	@Override
	public void ensureVertexCapacity(int verticesNum) {
		verticesUserWeights.ensureCapacity(verticesNum);
	}

	@Override
	public void ensureEdgeCapacity(int edgesNum) {
		edgesUserWeights.ensureCapacity(edgesNum);
		if (edgesNum * 2 > endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, Math.max(2 * endpoints.length, edgesNum * 2)));
	}

	@Override
	public void clear() {
		vertices.clear();
		edges.clear();
		verticesUserWeights.clearContainers();
		edgesUserWeights.clearContainers();
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	private void setEdgeEndpoints(int e, int source, int target) {
		endpoints[edgeSourceIndex(e)] = source;
		endpoints[edgeTargetIndex(e)] = target;
	}

	private static int edgeSourceIndex(int e) {
		return e * 2 + 0;
	}

	private static int edgeTargetIndex(int e) {
		return e * 2 + 1;
	}

	int edgeSource(int edge) {
		checkEdge(edge);
		return endpoints[edgeSourceIndex(edge)];
	}

	int edgeTarget(int edge) {
		checkEdge(edge);
		return endpoints[edgeTargetIndex(edge)];
	}

	@Override
	public IndexGraph build() {
		return immutableImpl.newFromBuilder(this);
	}

	@Override
	public IndexGraph buildMutable() {
		return mutableImpl.newFromBuilder(this);
	}

	@Override
	public IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges) {
		return immutableImpl.newFromBuilderWithReIndex(this, reIndexVertices, reIndexEdges);
	}

	@Override
	public IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges) {
		return mutableImpl.newFromBuilderWithReIndex(this, reIndexVertices, reIndexEdges);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, true, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<String> verticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
		return edgesUserWeights.getWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, false, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	private void checkVertex(int vertex) {
		if (!vertices().contains(vertex))
			throw NoSuchVertexException.ofIndex(vertex);
	}

	private void checkEdge(int edge) {
		if (!edges().contains(edge))
			throw NoSuchEdgeException.ofIndex(edge);
	}

}
