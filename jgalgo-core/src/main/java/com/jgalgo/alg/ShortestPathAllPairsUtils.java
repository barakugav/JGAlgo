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

package com.jgalgo.alg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import com.jgalgo.alg.ShortestPathSingleSource.IResult;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntContainers;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class ShortestPathAllPairsUtils {

	static abstract class AbstractImpl implements ShortestPathAllPairs {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> ShortestPathAllPairs.Result<V, E> computeAllShortestPaths(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (ShortestPathAllPairs.Result<V, E>) computeAllShortestPaths((IndexGraph) g, w0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				ShortestPathAllPairs.IResult indexResult = computeAllShortestPaths(iGraph, iw);
				return (ShortestPathAllPairs.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				ShortestPathAllPairs.IResult indexResult = computeAllShortestPaths(iGraph, iw);
				return new ObjResultFromIndexResult<>(indexResult, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetShortestPaths(Graph<V, E> g,
				Collection<V> verticesSubset, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IntCollection verticesSubset0 = IntContainers.toIntCollection((Collection<Integer>) verticesSubset);
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (ShortestPathAllPairs.Result<V, E>) computeSubsetShortestPaths((IndexGraph) g, verticesSubset0,
						w0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IntCollection iVerticesSubset =
						IndexIdMaps.idToIndexCollection((Collection<Integer>) verticesSubset, viMap);
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				ShortestPathAllPairs.IResult indexResult = computeSubsetShortestPaths(iGraph, iVerticesSubset, iw);
				return (ShortestPathAllPairs.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IntCollection iVerticesSubset = IndexIdMaps.idToIndexCollection(verticesSubset, viMap);
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				ShortestPathAllPairs.IResult indexResult = computeSubsetShortestPaths(iGraph, iVerticesSubset, iw);
				return new ObjResultFromIndexResult<>(indexResult, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> ShortestPathAllPairs.Result<V, E> computeAllCardinalityShortestPaths(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (ShortestPathAllPairs.Result<V, E>) computeAllCardinalityShortestPaths((IndexGraph) g);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				ShortestPathAllPairs.IResult indexResult = computeAllCardinalityShortestPaths(iGraph);
				return (ShortestPathAllPairs.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				ShortestPathAllPairs.IResult indexResult = computeAllCardinalityShortestPaths(iGraph);
				return new ObjResultFromIndexResult<>(indexResult, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetCardinalityShortestPaths(Graph<V, E> g,
				Collection<V> verticesSubset) {
			if (g instanceof IndexGraph) {
				IntCollection verticesSubset0 = IntContainers.toIntCollection((Collection<Integer>) verticesSubset);
				return (ShortestPathAllPairs.Result<V, E>) computeSubsetCardinalityShortestPaths((IndexGraph) g,
						verticesSubset0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IntCollection iVerticesSubset =
						IndexIdMaps.idToIndexCollection((Collection<Integer>) verticesSubset, viMap);
				ShortestPathAllPairs.IResult indexResult =
						computeSubsetCardinalityShortestPaths(iGraph, iVerticesSubset);
				return (ShortestPathAllPairs.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IntCollection iVerticesSubset = IndexIdMaps.idToIndexCollection(verticesSubset, viMap);
				ShortestPathAllPairs.IResult indexResult =
						computeSubsetCardinalityShortestPaths(iGraph, iVerticesSubset);
				return new ObjResultFromIndexResult<>(indexResult, viMap, eiMap);
			}
		}

		abstract ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w);

		abstract ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
				IWeightFunction w);

		ShortestPathAllPairs.IResult computeAllCardinalityShortestPaths(IndexGraph g) {
			return computeAllShortestPaths(g, IWeightFunction.CardinalityWeightFunction);
		}

		ShortestPathAllPairs.IResult computeSubsetCardinalityShortestPaths(IndexGraph g, IntCollection verticesSubset) {
			return computeSubsetShortestPaths(g, verticesSubset, IWeightFunction.CardinalityWeightFunction);
		}

	}

	static int[] vToResIdx(IndexGraph g, IntCollection verticesSubset) {
		int[] vToResIdx = new int[g.vertices().size()];
		Arrays.fill(vToResIdx, -1);
		boolean allVertices = verticesSubset == null;
		if (allVertices) {
			for (int n = g.vertices().size(), v = 0; v < n; v++)
				vToResIdx[v] = v;
		} else {
			int resIdx = 0;
			for (int v : verticesSubset)
				vToResIdx[v] = resIdx++;
		}
		return vToResIdx;
	}

	static abstract class ResultImpl implements ShortestPathAllPairs.IResult {

		private ResultImpl() {}

		abstract void setDistance(int source, int target, double distance);

		abstract int getEdgeTo(int source, int target);

		abstract void setEdgeTo(int source, int target, int edge);

		static abstract class AllVertices extends ResultImpl {

			final IndexGraph g;
			private final int[][] edges;
			private IPath negCycle;

			AllVertices(IndexGraph g) {
				this.g = g;
				int n = g.vertices().size();
				edges = new int[n][n];
				BigArrays.fill(edges, -1);
			}

			@Override
			int getEdgeTo(int source, int target) {
				return edges[source][target];
			}

			@Override
			void setEdgeTo(int source, int target, int edge) {
				edges[source][target] = edge;
			}

			void setNegCycle(IPath cycle) {
				Objects.requireNonNull(cycle);
				this.negCycle = cycle;
			}

			@Override
			public boolean foundNegativeCycle() {
				return negCycle != null;
			}

			@Override
			public IPath getNegativeCycle() {
				if (!foundNegativeCycle())
					throw new IllegalStateException("no negative cycle found");
				return negCycle;
			}

		}

		static class Undirected extends AllVertices {

			private final int n;
			private final double[] distances;

			Undirected(IndexGraph g) {
				super(g);
				n = g.vertices().size();
				distances = new double[n * (n - 1) / 2];
				Arrays.fill(distances, Double.POSITIVE_INFINITY);
			}

			private int index(int u, int v) {
				assert u != v;
				if (u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				/* index mapping assume always u < v (strictly!) */
				return (2 * n - u - 1) * u / 2 + v - u - 1;
			}

			@Override
			void setDistance(int source, int target, double distance) {
				distances[index(source, target)] = distance;
			}

			@Override
			public double distance(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException("negative cycle found, no shortest path exists");
				return source != target ? distances[index(source, target)] : 0;
			}

			@Override
			public IPath getPath(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException("negative cycle found, no shortest path exists");
				if (distance(source, target) == Double.POSITIVE_INFINITY)
					return null;
				IntList path = new IntArrayList();
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e != -1;
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
				return new PathImpl(g, source, target, path);
			}
		}

		static class Directed extends AllVertices {
			private final double[][] distances;

			Directed(IndexGraph g) {
				super(g);
				int n = g.vertices().size();
				distances = new double[n][n];
				for (int v = 0; v < n; v++) {
					Arrays.fill(distances[v], Double.POSITIVE_INFINITY);
					setDistance(v, v, 0);
				}
			}

			@Override
			void setDistance(int source, int target, double distance) {
				distances[source][target] = distance;
			}

			@Override
			public double distance(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException("negative cycle found, no shortest path exists");
				return distances[source][target];
			}

			@Override
			public IPath getPath(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException("negative cycle found, no shortest path exists");
				if (distance(source, target) == Double.POSITIVE_INFINITY)
					return null;
				IntList path = new IntArrayList();
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e != -1;
					assert v == g.edgeSource(e);
					path.add(e);
					v = g.edgeTarget(e);
				}
				return new PathImpl(g, source, target, path);
			}
		}

	}

	static abstract class ResFromSSSP implements ShortestPathAllPairs.IResult {

		final ShortestPathSingleSource.IResult[] ssspResults;

		ResFromSSSP(ShortestPathSingleSource.IResult[] ssspResults) {
			this.ssspResults = ssspResults;
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public IPath getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		static class AllVertices extends ResFromSSSP {

			AllVertices(IResult[] ssspResults) {
				super(ssspResults);
			}

			@Override
			public double distance(int source, int target) {
				return ssspResults[source].distance(target);
			}

			@Override
			public IPath getPath(int source, int target) {
				return ssspResults[source].getPath(target);
			}

		}

		static class VerticesSubset extends ResFromSSSP {

			final int[] vToResIdx;

			VerticesSubset(IResult[] ssspResults, int[] vToResIdx) {
				super(ssspResults);
				this.vToResIdx = vToResIdx;
			}

			@Override
			public double distance(int source, int target) {
				return ssspResults[vToResIdx[source]].distance(target);
			}

			@Override
			public IPath getPath(int source, int target) {
				return ssspResults[vToResIdx[source]].getPath(target);
			}

		}

	}

	private static class ObjResultFromIndexResult<V, E> implements ShortestPathAllPairs.Result<V, E> {

		private final ShortestPathAllPairs.IResult indexRes;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(ShortestPathAllPairs.IResult indexRes, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public double distance(V source, V target) {
			return indexRes.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public Path<V, E> getPath(V source, V target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return PathImpl.objPathFromIndexPath(indexPath, viMap, eiMap);
		}

		@Override
		public boolean foundNegativeCycle() {
			return indexRes.foundNegativeCycle();
		}

		@Override
		public Path<V, E> getNegativeCycle() {
			return PathImpl.objPathFromIndexPath(indexRes.getNegativeCycle(), viMap, eiMap);
		}
	}

	private static class IntResultFromIndexResult implements ShortestPathAllPairs.IResult {

		private final ShortestPathAllPairs.IResult indexRes;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(ShortestPathAllPairs.IResult indexRes, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public double distance(int source, int target) {
			return indexRes.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public IPath getPath(int source, int target) {
			IPath indexPath = indexRes.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return PathImpl.intPathFromIndexPath(indexPath, viMap, eiMap);
		}

		@Override
		public boolean foundNegativeCycle() {
			return indexRes.foundNegativeCycle();
		}

		@Override
		public IPath getNegativeCycle() {
			return PathImpl.intPathFromIndexPath(indexRes.getNegativeCycle(), viMap, eiMap);
		}
	}

}
