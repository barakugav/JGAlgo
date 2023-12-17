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

import java.util.Objects;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterables;
import it.unimi.dsi.fastutil.ints.IntIterator;

abstract class IndexGraphBase extends GraphBase<Integer, Integer> implements IndexGraph {

	private final boolean isDirected;
	final GraphElementSet vertices;
	final GraphElementSet edges;
	private final IndexIntIdMap verticesIdMap;
	private final IndexIntIdMap edgesIdMap;
	long[] edgeEndpoints;

	static final int EndpointNone = -1;
	static final long DefaultEndpoints = sourceTarget2Endpoints(EndpointNone, EndpointNone);

	IndexGraphBase(boolean isDirected, GraphElementSet vertices, GraphElementSet edges) {
		this.isDirected = isDirected;
		this.vertices = Objects.requireNonNull(vertices);
		this.edges = Objects.requireNonNull(edges);
		verticesIdMap = IndexIntIdMap.identityVerticesMap(vertices);
		edgesIdMap = IndexIntIdMap.identityEdgesMap(edges);
	}

	IndexGraphBase(boolean isDirected, int n, int m, boolean mutable) {
		this.isDirected = isDirected;
		if (mutable) {
			this.vertices = GraphElementSet.Mutable.ofVertices(n);
			this.edges = GraphElementSet.Mutable.ofEdges(m);
		} else {
			this.vertices = GraphElementSet.Immutable.ofVertices(n);
			this.edges = GraphElementSet.Immutable.ofEdges(m);
		}
		verticesIdMap = IndexIntIdMap.identityVerticesMap(vertices);
		edgesIdMap = IndexIntIdMap.identityEdgesMap(edges);
	}

	@Override
	public final boolean isDirected() {
		return isDirected;
	}

	@Override
	public final GraphElementSet vertices() {
		return vertices;
	}

	@Override
	public final GraphElementSet edges() {
		return edges;
	}

	@Override
	public final int edgeSource(int edge) {
		Assertions.Graphs.checkId(edge, edges().size(), true);
		return source(edge);
	}

	@Override
	public final int edgeTarget(int edge) {
		Assertions.Graphs.checkId(edge, edges().size(), true);
		return target(edge);
	}

	/* unchecked version of edgeSource(edge) */
	final int source(int edge) {
		return endpoints2Source(edgeEndpoints[edge]);
	}

	/* unchecked version of edgeTarget(edge) */
	final int target(int edge) {
		return endpoints2Target(edgeEndpoints[edge]);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		Assertions.Graphs.checkId(edge, edges().size(), true);
		long endpoints = edgeEndpoints[edge];
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException("The given vertex (idx=" + endpoint
					+ ") is not an endpoint of the edge (idx=" + u + ", idx=" + v + ")");
		}
	}

	void setEndpoints(int edge, int source, int target) {
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, target);
	}

	static long sourceTarget2Endpoints(int source, int target) {
		return JGAlgoUtils.longPack(source, target);
	}

	static int endpoints2Source(long endpoints) {
		return JGAlgoUtils.long2low(endpoints);
	}

	static int endpoints2Target(long endpoints) {
		return JGAlgoUtils.long2high(endpoints);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		Assertions.Graphs.checkVertex(source, vertices().size());
		Assertions.Graphs.checkVertex(target, vertices().size());
		return isDirected() ? new EdgeSetSourceTargetDirected(source, target)
				: new EdgeSetSourceTargetUndirected(source, target);
	}

	abstract class EdgeSetAbstract extends AbstractIntSet implements IEdgeSet {

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			removeEdge(edge);
			return true;
		}

	}

	abstract class EdgeSetOutUndirected extends EdgeSetAbstract {

		final int source;

		EdgeSetOutUndirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && (source == source(edge) || source == target(edge));
		}

		@Override
		public void clear() {
			removeOutEdgesOf(source);
		}
	}

	abstract class EdgeSetInUndirected extends EdgeSetAbstract {

		final int target;

		EdgeSetInUndirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && (target == source(edge) || target == target(edge));
		}

		@Override
		public void clear() {
			removeInEdgesOf(target);
		}
	}

	abstract class EdgeSetOutDirected extends EdgeSetAbstract {

		final int source;

		EdgeSetOutDirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && source == source(edge);
		}

		@Override
		public void clear() {
			removeOutEdgesOf(source);
		}
	}

	abstract class EdgeSetInDirected extends EdgeSetAbstract {

		final int target;

		EdgeSetInDirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && target == target(edge);
		}

		@Override
		public void clear() {
			removeInEdgesOf(target);
		}
	}

	private abstract class EdgeSetSourceTarget extends EdgeSetAbstract {

		final int source, target;

		EdgeSetSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterSourceTarget(source, target);
		}

		@Override
		public int size() {
			return (int) IntIterables.size(this);
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public void clear() {
			while (!isEmpty())
				remove(iterator().nextInt());
		}
	}

	private class EdgeSetSourceTargetUndirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetUndirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			if (!(0 <= edge && edge < edges().size()))
				return false;
			int s = source(edge), t = target(edge);
			return (source == s && target == t) || (source == t && target == s);
		}
	}

	private class EdgeSetSourceTargetDirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetDirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && source == source(edge) && target == target(edge);
		}
	}

	private class EdgeIterSourceTarget implements IEdgeIter {

		private final int source, target;
		private IntIterator it;
		private int nextEdge = -1;
		private int lastEdge = -1;

		EdgeIterSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
			it = outEdges(source).iterator();
			advance();
		}

		private void advance() {
			while (it.hasNext()) {
				int e = it.nextInt();
				if (edgeEndpoint(e, source) == target) {
					nextEdge = e;
					return;
				}
			}
			nextEdge = -1;
		}

		@Override
		public boolean hasNext() {
			return nextEdge >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int ret = nextEdge;
			advance();
			return lastEdge = ret;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return nextEdge;
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			return target;
		}

		@Override
		public void remove() {
			if (lastEdge == -1)
				throw new IllegalStateException();

			/* we remove the edge using the graph API, not EdgeIter.remove(), so we must copy the iterator */
			if (it instanceof IEdgeIter)
				it = new IntArrayList(it).iterator();

			removeEdge(lastEdge);
		}
	}

	@Deprecated
	@Override
	public IndexIntIdMap indexGraphVerticesMap() {
		return verticesIdMap;
	}

	@Deprecated
	@Override
	public IndexIntIdMap indexGraphEdgesMap() {
		return edgesIdMap;
	}

}
