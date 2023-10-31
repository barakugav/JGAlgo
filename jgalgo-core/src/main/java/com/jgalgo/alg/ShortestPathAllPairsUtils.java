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
import java.util.Objects;
import com.jgalgo.alg.ShortestPathSingleSource.Result;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;
import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class ShortestPathAllPairsUtils {

	static abstract class AbstractImpl implements ShortestPathAllPairs {

		@Override
		public ShortestPathAllPairs.Result computeAllShortestPaths(IntGraph g, IWeightFunction w) {
			if (g instanceof IndexGraph)
				return computeAllShortestPaths((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			ShortestPathAllPairs.Result indexResult = computeAllShortestPaths(iGraph, iw);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		@Override
		public ShortestPathAllPairs.Result computeSubsetShortestPaths(IntGraph g, IntCollection verticesSubset,
				IWeightFunction w) {
			if (g instanceof IndexGraph)
				return computeSubsetShortestPaths((IndexGraph) g, verticesSubset, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			ShortestPathAllPairs.Result indexResult = computeSubsetShortestPaths(iGraph, verticesSubset, iw);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		@Override
		public ShortestPathAllPairs.Result computeAllCardinalityShortestPaths(IntGraph g) {
			if (g instanceof IndexGraph)
				return computeAllCardinalityShortestPaths((IndexGraph) g);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();

			ShortestPathAllPairs.Result indexResult = computeAllCardinalityShortestPaths(iGraph);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		@Override
		public ShortestPathAllPairs.Result computeSubsetCardinalityShortestPaths(IntGraph g,
				IntCollection verticesSubset) {
			if (g instanceof IndexGraph)
				return computeSubsetCardinalityShortestPaths((IndexGraph) g, verticesSubset);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();

			ShortestPathAllPairs.Result indexResult = computeSubsetCardinalityShortestPaths(iGraph, verticesSubset);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		abstract ShortestPathAllPairs.Result computeAllShortestPaths(IndexGraph g, IWeightFunction w);

		abstract ShortestPathAllPairs.Result computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
				IWeightFunction w);

		ShortestPathAllPairs.Result computeAllCardinalityShortestPaths(IndexGraph g) {
			return computeAllShortestPaths(g, IWeightFunction.CardinalityWeightFunction);
		}

		ShortestPathAllPairs.Result computeSubsetCardinalityShortestPaths(IndexGraph g, IntCollection verticesSubset) {
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

	static abstract class ResultImpl implements ShortestPathAllPairs.Result {

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

	static abstract class ResFromSSSP implements ShortestPathAllPairs.Result {

		final ShortestPathSingleSource.Result[] ssspResults;

		ResFromSSSP(ShortestPathSingleSource.Result[] ssspResults) {
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

			AllVertices(Result[] ssspResults) {
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

			VerticesSubset(Result[] ssspResults, int[] vToResIdx) {
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

	private static class ResultFromIndexResult implements ShortestPathAllPairs.Result {

		private final ShortestPathAllPairs.Result res;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		ResultFromIndexResult(ShortestPathAllPairs.Result res, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public double distance(int source, int target) {
			return res.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public IPath getPath(int source, int target) {
			IPath indexPath = res.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return PathImpl.intPathFromIndexPath(indexPath, viMap, eiMap);
		}

		@Override
		public boolean foundNegativeCycle() {
			return res.foundNegativeCycle();
		}

		@Override
		public IPath getNegativeCycle() {
			return PathImpl.intPathFromIndexPath(res.getNegativeCycle(), viMap, eiMap);
		}

	}

}
