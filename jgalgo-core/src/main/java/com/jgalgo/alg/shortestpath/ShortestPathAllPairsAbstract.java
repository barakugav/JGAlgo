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

package com.jgalgo.alg.shortestpath;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing shortest path between all pairs in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ShortestPathAllPairsAbstract implements ShortestPathAllPairs {

	/**
	 * Default constructor.
	 */
	public ShortestPathAllPairsAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> ShortestPathAllPairs.Result<V, E> computeAllShortestPaths(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (ShortestPathAllPairs.Result<V, E>) computeAllShortestPaths((IndexGraph) g, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			ShortestPathAllPairs.IResult indexResult =
					NegativeCycleException.runAndConvertException(g, () -> computeAllShortestPaths(iGraph, iw));
			return resultFromIndexResult(g, indexResult);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetShortestPaths(Graph<V, E> g,
			Collection<V> verticesSubset, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IntCollection verticesSubset0 = IntAdapters.asIntCollection((Collection<Integer>) verticesSubset);
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (ShortestPathAllPairs.Result<V, E>) computeSubsetShortestPaths((IndexGraph) g, verticesSubset0, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IntCollection iVerticesSubset = IndexIdMaps.idToIndexCollection(verticesSubset, viMap);
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			ShortestPathAllPairs.IResult indexResult = NegativeCycleException
					.runAndConvertException(g, () -> computeSubsetShortestPaths(iGraph, iVerticesSubset, iw));
			return resultFromIndexResult(g, indexResult);
		}
	}

	protected abstract ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w);

	protected abstract ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g,
			IntCollection verticesSubset, IWeightFunction w);

	/**
	 * The result object for the shortest path between all pairs of vertices in an index graph.
	 *
	 * @author Barak Ugav
	 */
	protected static final class IndexResult implements ShortestPathAllPairs.IResult {

		final IndexGraph g;
		private final boolean directed;
		private final int n;
		private final int[][] edges;
		private final double[] distances;
		private IntSet[] reachableVerticesFrom;
		private IntSet[] reachableVerticesTo;

		/**
		 * Create a new result object for the given graph.
		 *
		 * @param g the graph on which the shortest paths were computed. Should not be modified during the lifetime of
		 *              this object
		 */
		public IndexResult(IndexGraph g) {
			this.g = g;
			directed = g.isDirected();
			n = g.vertices().size();
			edges = new int[n][n];
			BigArrays.fill(edges, -1);
			if (directed) {
				distances = new double[n * n];
			} else {
				distances = new double[n * (n - 1) / 2];
			}
			Arrays.fill(distances, Double.POSITIVE_INFINITY);
			if (directed)
				for (int v : range(n))
					setDistance(v, v, 0);
		}

		public static IndexResult fromSsspResult(IndexGraph g, ShortestPathSingleSource.IResult[] ssspResults) {
			final int n = g.vertices().size();
			final boolean directed = g.isDirected();
			IndexResult res = new IndexResult(g);
			assert ssspResults.length == n;

			final boolean knownSsspRes =
					Arrays.stream(ssspResults).allMatch(r -> r instanceof ShortestPathSingleSourceAbstract.IndexResult);
			if (knownSsspRes) {
				for (int u : range(n)) {
					ShortestPathSingleSourceAbstract.IndexResult uRes =
							(ShortestPathSingleSourceAbstract.IndexResult) ssspResults[u];
					for (int v : range(directed ? 0 : u + 1, n))
						res.setDistance(u, v, uRes.distances[v]);
				}
				for (int s : range(n)) {
					ShortestPathSingleSourceAbstract.IndexResult sRes =
							(ShortestPathSingleSourceAbstract.IndexResult) ssspResults[s];
					for (int vFirst : range(n)) {
						if (s == vFirst) {
							res.setEdgeTo(s, s, -1);
							continue;
						}
						if (sRes.backtrack[vFirst] < 0)
							continue;
						for (int v = vFirst;;) {
							int e = sRes.backtrack[v];
							assert e >= 0;
							v = g.edgeEndpoint(e, v);
							if (v == s) {
								final int sTowardV = e;
								for (v = vFirst;;) {
									res.setEdgeTo(s, v, sTowardV);
									v = g.edgeEndpoint(sRes.backtrack[v], v);
									if (v == s)
										break;
								}
								break;
							}
						}
					}
				}

			} else {
				for (int u : range(n))
					for (int v : range(directed ? 0 : u + 1, n))
						res.setDistance(u, v, ssspResults[u].distance(v));
				for (int s : range(n)) {
					ShortestPathSingleSource.IResult sRes = ssspResults[s];
					for (int vFirst : range(n)) {
						if (s == vFirst) {
							res.setEdgeTo(s, s, -1);
							continue;
						}
						if (sRes.backtrackEdge(vFirst) < 0)
							continue;
						for (int v = vFirst;;) {
							int e = sRes.backtrackEdge(v);
							assert e >= 0;
							v = g.edgeEndpoint(e, v);
							if (v == s) {
								final int sTowardV = e;
								for (v = vFirst;;) {
									res.setEdgeTo(s, v, sTowardV);
									v = g.edgeEndpoint(sRes.backtrackEdge(v), v);
									if (v == s)
										break;
								}
								break;
							}
						}
					}
				}
			}
			return res;
		}

		public int getEdgeTo(int source, int target) {
			Assertions.checkVertex(source, n);
			Assertions.checkVertex(target, n);
			return edges[source][target];
		}

		public void setEdgeTo(int source, int target, int edge) {
			edges[source][target] = edge;
		}

		public void setDistance(int source, int target, double distance) {
			if (!directed && source == target)
				return;
			distances[index(source, target)] = distance;
		}

		@Override
		public double distance(int source, int target) {
			Assertions.checkVertex(source, n);
			Assertions.checkVertex(target, n);
			return source != target ? distances[index(source, target)] : 0;
		}

		private int index(int u, int v) {
			if (directed) {
				return u * n + v;
			} else {
				assert u != v;
				if (u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				/* index mapping assume always u < v (strictly!) */
				return (2 * n - u - 1) * u / 2 + v - u - 1;
			}
		}

		@Override
		public IPath getPath(int source, int target) {
			if (getEdgeTo(source, target) < 0 && source != target)
				return null;
			IntList path = new IntArrayList();
			if (directed) {
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e >= 0;
					assert v == g.edgeSource(e);
					path.add(e);
					v = g.edgeTarget(e);
				}
			} else {
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e >= 0;
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			return IPath.valueOf(g, source, target, path);
		}

		@Override
		public boolean isReachable(int source, int target) {
			return getEdgeTo(source, target) >= 0 || source == target;
		}

		@Override
		public IntSet reachableVerticesFrom(int source) {
			if (reachableVerticesFrom == null)
				reachableVerticesFrom = new IntSet[n];
			if (reachableVerticesFrom[source] == null) {
				int reachableNum = 0;
				for (int v = 0; v < source; v++)
					if (getEdgeTo(source, v) >= 0)
						reachableNum += 1;
				reachableNum += 1; // source can reach itself
				for (int v = source + 1; v < n; v++)
					if (getEdgeTo(source, v) >= 0)
						reachableNum += 1;

				int[] vs = new int[reachableNum];
				int resIdx = 0;
				for (int v = 0; v < source; v++)
					if (getEdgeTo(source, v) >= 0)
						vs[resIdx++] = v;
				vs[resIdx++] = source; // source can reach itself
				for (int v = source + 1; v < n; v++)
					if (getEdgeTo(source, v) >= 0)
						vs[resIdx++] = v;
				reachableVerticesFrom[source] = ImmutableIntArraySet
						.newInstance(vs, v -> 0 <= v && v < n && (getEdgeTo(source, v) >= 0 || v == source));
			}
			return reachableVerticesFrom[source];
		}

		@Override
		public IntSet reachableVerticesTo(int target) {
			if (!g.isDirected())
				return reachableVerticesFrom(target);

			if (reachableVerticesTo == null)
				reachableVerticesTo = new IntSet[n];
			if (reachableVerticesTo[target] == null) {
				int reachableNum = 0;
				for (int u = 0; u < target; u++)
					if (getEdgeTo(u, target) >= 0)
						reachableNum += 1;
				reachableNum += 1; // target can reach itself
				for (int u = target + 1; u < n; u++)
					if (getEdgeTo(u, target) >= 0)
						reachableNum += 1;

				int[] vs = new int[reachableNum];
				int resIdx = 0;
				for (int u = 0; u < target; u++)
					if (getEdgeTo(u, target) >= 0)
						vs[resIdx++] = u;
				vs[resIdx++] = target; // target can reach itself
				for (int u = target + 1; u < n; u++)
					if (getEdgeTo(u, target) >= 0)
						vs[resIdx++] = u;
				reachableVerticesTo[target] = ImmutableIntArraySet
						.newInstance(vs, u -> 0 <= u && u < n && (getEdgeTo(u, target) >= 0 || u == target));
			}
			return reachableVerticesTo[target];
		}
	}

	static class IndexResultVerticesSubsetFromSssp implements ShortestPathAllPairs.IResult {

		final ShortestPathSingleSource.IResult[] ssspResults;
		final int[] vToSubsetIdx;

		IndexResultVerticesSubsetFromSssp(ShortestPathSingleSource.IResult[] ssspResults, int[] vToSubsetIdx) {
			this.ssspResults = ssspResults;
			this.vToSubsetIdx = vToSubsetIdx;
		}

		@Override
		public double distance(int source, int target) {
			int sourceIdx = resultIdx(source);
			resultIdx(target); /* checks that target is in the subset */
			return ssspResults[sourceIdx].distance(target);
		}

		@Override
		public IPath getPath(int source, int target) {
			int sourceIdx = resultIdx(source);
			resultIdx(target); /* checks that target is in the subset */
			return ssspResults[sourceIdx].getPath(target);
		}

		private int resultIdx(int vertex) {
			Assertions.checkVertex(vertex, vToSubsetIdx.length);
			int idx = vToSubsetIdx[vertex];
			if (idx < 0)
				throw new IllegalArgumentException("no results for vertex " + vertex);
			return idx;
		}

		@Override
		public boolean isReachable(int source, int target) {
			int sourceIdx = resultIdx(source);
			resultIdx(target); /* checks that target is in the subset */
			return ssspResults[sourceIdx].isReachable(target);
		}

		@Override
		public IntSet reachableVerticesFrom(int source) {
			int sourceIdx = resultIdx(source);
			return ssspResults[sourceIdx].reachableVertices();
		}

		@Override
		public IntSet reachableVerticesTo(int target) {
			if (!ssspResults[0].graph().isDirected())
				return reachableVerticesFrom(target);
			throw new UnsupportedOperationException(
					"Computing reachableVerticesTo from a partial (vertices subset) APSP is not supported."
							+ " As an alternative, consider computing the APSP or Path.reachableVertices of the reverse graph (Graph.reverseView())");
		}
	}

	static int[] indexVerticesSubset(IndexGraph g, IntCollection verticesSubset) {
		if (verticesSubset == null)
			return range(g.vertices().size()).toIntArray();
		int[] vToSubsetIdx = new int[g.vertices().size()];
		Arrays.fill(vToSubsetIdx, -1);
		int resIdx = 0;
		for (int v : verticesSubset)
			vToSubsetIdx[v] = resIdx++;
		return vToSubsetIdx;
	}

	private static class ObjResultFromIndexResult<V, E> implements ShortestPathAllPairs.Result<V, E> {

		private final ShortestPathAllPairs.IResult indexRes;
		private final Graph<V, E> g;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, ShortestPathAllPairs.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double distance(V source, V target) {
			return indexRes.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public Path<V, E> getPath(V source, V target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return Path.pathFromIndexPath(g, indexPath);
		}

		@Override
		public boolean isReachable(V source, V target) {
			return indexRes.isReachable(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public Set<V> reachableVerticesFrom(V source) {
			return IndexIdMaps.indexToIdSet(indexRes.reachableVerticesFrom(viMap.idToIndex(source)), viMap);
		}

		@Override
		public Set<V> reachableVerticesTo(V target) {
			return IndexIdMaps.indexToIdSet(indexRes.reachableVerticesTo(viMap.idToIndex(target)), viMap);
		}
	}

	private static class IntResultFromIndexResult implements ShortestPathAllPairs.IResult {

		private final ShortestPathAllPairs.IResult indexRes;
		private final IntGraph g;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, ShortestPathAllPairs.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double distance(int source, int target) {
			return indexRes.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public IPath getPath(int source, int target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return (IPath) Path.pathFromIndexPath(g, indexPath);
		}

		@Override
		public boolean isReachable(int source, int target) {
			return indexRes.isReachable(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public IntSet reachableVerticesFrom(int source) {
			return IndexIdMaps.indexToIdSet(indexRes.reachableVerticesFrom(viMap.idToIndex(source)), viMap);
		}

		@Override
		public IntSet reachableVerticesTo(int target) {
			return IndexIdMaps.indexToIdSet(indexRes.reachableVerticesTo(viMap.idToIndex(target)), viMap);
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> ShortestPathAllPairs.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			ShortestPathAllPairs.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (ShortestPathAllPairs.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
