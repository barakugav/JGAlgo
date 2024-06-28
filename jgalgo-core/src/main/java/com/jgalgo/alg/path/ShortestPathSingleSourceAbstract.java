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
package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * Abstract class for computing shortest path from a single source in graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ShortestPathSingleSourceAbstract implements ShortestPathSingleSource {

	/**
	 * Default constructor.
	 */
	public ShortestPathSingleSourceAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
			V source) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue();
			return (ShortestPathSingleSource.Result<V, E>) computeShortestPaths((IndexGraph) g, w0, source0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			ShortestPathSingleSource.IResult indexResult =
					NegativeCycleException.runAndConvertException(g, () -> computeShortestPaths(iGraph, iw, iSource));
			return resultFromIndexResult(g, indexResult);
		}
	}

	protected abstract ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w,
			int source);

	/**
	 * Result of a single source shortest path computation on an {@linkplain IndexGraph index graph}.
	 *
	 * @author Barak Ugav
	 */
	protected static final class IndexResult implements ShortestPathSingleSource.IResult {

		private final IndexGraph g;
		private final int source;
		final double[] distances;
		final int[] backtrack;

		private IntGraph shortestPathTreeUndirected;
		private IntGraph shortestPathTreeDirected;

		/**
		 * Create a new result object for an index graph, initialized with {@code Double.POSITIVE_INFINITY} distances
		 * and {@code -1} backtrack edges.
		 *
		 * @param g      the index graph. Should not be modified as long as this object is used.
		 * @param source the source vertex
		 */
		public IndexResult(IndexGraph g, int source) {
			this.g = Objects.requireNonNull(g);
			this.source = source;
			int n = g.vertices().size();
			distances = new double[n];
			backtrack = new int[n];
			Arrays.fill(distances, Double.POSITIVE_INFINITY);
			Arrays.fill(backtrack, -1);
		}

		/**
		 * Create a new result object for an index graph, initialized with the given distances and backtrack edges.
		 *
		 * @param g         the index graph. Should not be modified as long as this object is used.
		 * @param source    the source vertex
		 * @param distances array with distance from the source per vertex in the graph
		 * @param backtrack array with the edge used to reach each vertex from the source
		 */
		public IndexResult(IndexGraph g, int source, double[] distances, int[] backtrack) {
			int n = g.vertices().size();
			if (distances.length != n || backtrack.length != n)
				throw new IllegalArgumentException("distances and backtrack arrays must be of size " + n);
			this.g = g;
			this.source = source;
			this.distances = distances;
			this.backtrack = backtrack;
		}

		@Override
		public double distance(int target) {
			Assertions.checkVertex(target, distances.length);
			return distances[target];
		}

		@Override
		public IPath getPath(int target) {
			if (distance(target) == Double.POSITIVE_INFINITY)
				return null;
			IntArrayList path = new IntArrayList();
			if (g.isDirected()) {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e < 0) {
						assert v == source;
						break;
					}
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e < 0) {
						assert v == source;
						break;
					}
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return IPath.valueOf(g, source, target, path);
		}

		@Override
		public int backtrackEdge(int target) {
			Assertions.checkVertex(target, backtrack.length);
			return backtrack[target];
		}

		@Override
		public IntGraph shortestPathTree(boolean directed) {
			if (directed) {
				if (shortestPathTreeDirected == null)
					shortestPathTreeDirected = ShortestPathSingleSource.IResult.super.shortestPathTree(true);
				return shortestPathTreeDirected;

			} else {
				if (shortestPathTreeUndirected == null)
					shortestPathTreeUndirected = ShortestPathSingleSource.IResult.super.shortestPathTree(false);
				return shortestPathTreeUndirected;
			}
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public IndexGraph graph() {
			return g;
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> ShortestPathSingleSource.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			ShortestPathSingleSource.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (ShortestPathSingleSource.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

	static class ObjResultFromIndexResult<V, E> implements ShortestPathSingleSource.Result<V, E> {

		private final ShortestPathSingleSource.IResult indexRes;
		private final Graph<V, E> g;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;
		private Graph<V, E> shortestPathTreeUndirected;
		private Graph<V, E> shortestPathTreeDirected;

		ObjResultFromIndexResult(Graph<V, E> g, ShortestPathSingleSource.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public double distance(V target) {
			return indexRes.distance(viMap.idToIndex(target));
		}

		@Override
		public Path<V, E> getPath(V target) {
			return Path.pathFromIndexPath(g, indexRes.getPath(viMap.idToIndex(target)));
		}

		@Override
		public E backtrackEdge(V target) {
			int tIdx = viMap.idToIndex(target);
			int eIdx = indexRes.backtrackEdge(tIdx);
			return eIdx < 0 ? null : eiMap.indexToId(eIdx);
		}

		@Override
		public Graph<V, E> shortestPathTree(boolean directed) {
			if (directed) {
				if (shortestPathTreeDirected == null)
					shortestPathTreeDirected = createShortestPathTree(true);
				return shortestPathTreeDirected;

			} else {
				if (shortestPathTreeUndirected == null)
					shortestPathTreeUndirected = createShortestPathTree(false);
				return shortestPathTreeUndirected;
			}
		}

		private Graph<V, E> createShortestPathTree(boolean directed) {
			IndexGraph ig = g.indexGraph();
			GraphBuilder<V, E> b = GraphBuilder.newInstance(directed);
			b.addVertex(source());
			for (int vIdx : range(ig.vertices().size()))
				if (indexRes.backtrackEdge(vIdx) >= 0)
					b.addVertex(viMap.indexToId(vIdx));
			for (int vIdx : range(ig.vertices().size())) {
				int eIdx = indexRes.backtrackEdge(vIdx);
				if (eIdx < 0)
					continue;
				int uIdx = ig.edgeEndpoint(eIdx, vIdx);
				V u = viMap.indexToId(uIdx);
				V v = viMap.indexToId(vIdx);
				E e = eiMap.indexToId(eIdx);
				b.addEdge(u, v, e);
			}
			return b.build();
		}

		@Override
		public V source() {
			return viMap.indexToId(indexRes.sourceInt());
		}

		@Override
		public Graph<V, E> graph() {
			return g;
		}
	}

	static class IntResultFromIndexResult implements ShortestPathSingleSource.IResult {

		private final ShortestPathSingleSource.IResult indexRes;
		private final IntGraph g;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;
		private IntGraph shortestPathTreeUndirected;
		private IntGraph shortestPathTreeDirected;

		IntResultFromIndexResult(IntGraph g, ShortestPathSingleSource.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public double distance(int target) {
			return indexRes.distance(viMap.idToIndex(target));
		}

		@Override
		public IPath getPath(int target) {
			return (IPath) Path.pathFromIndexPath(g, indexRes.getPath(viMap.idToIndex(target)));
		}

		@Override
		public int backtrackEdge(int target) {
			int tIdx = viMap.idToIndex(target);
			int eIdx = indexRes.backtrackEdge(tIdx);
			return eIdx < 0 ? -1 : eiMap.indexToIdInt(eIdx);
		}

		@Override
		public IntGraph shortestPathTree(boolean directed) {
			if (directed) {
				if (shortestPathTreeDirected == null)
					shortestPathTreeDirected = createShortestPathTree(true);
				return shortestPathTreeDirected;

			} else {
				if (shortestPathTreeUndirected == null)
					shortestPathTreeUndirected = createShortestPathTree(false);
				return shortestPathTreeUndirected;
			}
		}

		private IntGraph createShortestPathTree(boolean directed) {
			IndexGraph ig = g.indexGraph();
			IntGraphBuilder b = IntGraphBuilder.newInstance(directed);
			b.addVertex(sourceInt());
			for (int vIdx : range(ig.vertices().size()))
				if (indexRes.backtrackEdge(vIdx) >= 0)
					b.addVertex(viMap.indexToIdInt(vIdx));
			for (int vIdx : range(ig.vertices().size())) {
				int eIdx = indexRes.backtrackEdge(vIdx);
				if (eIdx < 0)
					continue;
				int uIdx = ig.edgeEndpoint(eIdx, vIdx);
				int u = viMap.indexToIdInt(uIdx);
				int v = viMap.indexToIdInt(vIdx);
				int e = eiMap.indexToIdInt(eIdx);
				b.addEdge(u, v, e);
			}
			return b.build();
		}

		@Override
		public int sourceInt() {
			return viMap.indexToIdInt(indexRes.sourceInt());
		}

		@Override
		public IntGraph graph() {
			return g;
		}
	}

}
