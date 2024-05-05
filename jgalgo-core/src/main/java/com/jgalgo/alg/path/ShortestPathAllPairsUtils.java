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
import java.util.Collection;
import java.util.Objects;
import com.jgalgo.alg.path.ShortestPathSingleSource.IResult;
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
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class ShortestPathAllPairsUtils {

	private ShortestPathAllPairsUtils() {}

	abstract static class AbstractImpl implements ShortestPathAllPairs {

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
				return (ShortestPathAllPairs.Result<V, E>) computeSubsetShortestPaths((IndexGraph) g, verticesSubset0,
						w0);

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

		abstract ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w);

		abstract ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
				IWeightFunction w);

	}

	static int[] vToResIdx(IndexGraph g, IntCollection verticesSubset) {
		int[] vToResIdx = new int[g.vertices().size()];
		Arrays.fill(vToResIdx, -1);
		boolean allVertices = verticesSubset == null;
		if (allVertices) {
			for (int v : range(g.vertices().size()))
				vToResIdx[v] = v;
		} else {
			int resIdx = 0;
			for (int v : verticesSubset)
				vToResIdx[v] = resIdx++;
		}
		return vToResIdx;
	}

	abstract static class IndexResult implements ShortestPathAllPairs.IResult {

		private IndexResult() {}

		abstract void setDistance(int source, int target, double distance);

		abstract int getEdgeTo(int source, int target);

		abstract void setEdgeTo(int source, int target, int edge);

		static class AllVertices extends IndexResult {

			final IndexGraph g;
			private final boolean directed;
			private final int n;
			private final int[][] edges;
			private final double[] distances;

			AllVertices(IndexGraph g) {
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

			@Override
			int getEdgeTo(int source, int target) {
				return edges[source][target];
			}

			@Override
			void setEdgeTo(int source, int target, int edge) {
				edges[source][target] = edge;
			}

			@Override
			void setDistance(int source, int target, double distance) {
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
				if (distance(source, target) == Double.POSITIVE_INFINITY)
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
		}

	}

	abstract static class IndexResultFromSssp implements ShortestPathAllPairs.IResult {

		final ShortestPathSingleSource.IResult[] ssspResults;

		IndexResultFromSssp(ShortestPathSingleSource.IResult[] ssspResults) {
			this.ssspResults = ssspResults;
		}

		static class AllVertices extends IndexResultFromSssp {

			AllVertices(IResult[] ssspResults) {
				super(ssspResults);
			}

			@Override
			public double distance(int source, int target) {
				Assertions.checkVertex(source, ssspResults.length);
				return ssspResults[source].distance(target);
			}

			@Override
			public IPath getPath(int source, int target) {
				Assertions.checkVertex(source, ssspResults.length);
				return ssspResults[source].getPath(target);
			}

		}

		static class VerticesSubset extends IndexResultFromSssp {

			final int[] vToResIdx;

			VerticesSubset(ShortestPathSingleSource.IResult[] ssspResults, int[] vToResIdx) {
				super(ssspResults);
				this.vToResIdx = vToResIdx;
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
				Assertions.checkVertex(vertex, vToResIdx.length);
				int idx = vToResIdx[vertex];
				if (idx < 0)
					throw new IllegalArgumentException("no results for vertex " + vertex);
				return idx;
			}
		}

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
