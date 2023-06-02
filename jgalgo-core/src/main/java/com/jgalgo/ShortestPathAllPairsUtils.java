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

package com.jgalgo;

import java.util.Arrays;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class ShortestPathAllPairsUtils {

	static abstract class AbstractImpl implements ShortestPathAllPairs {

		@Override
		public ShortestPathAllPairs.Result computeAllShortestPaths(Graph g, WeightFunction w) {
			if (g instanceof IndexGraph)
				return computeAllShortestPaths((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexGraphMap viMap = g.indexGraphVerticesMap();
			IndexGraphMap eiMap = g.indexGraphEdgesMap();
			w = WeightsImpl.indexWeightFuncFromIdWeightFunc(w, eiMap);

			ShortestPathAllPairs.Result indexResult = computeAllShortestPaths(iGraph, w);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		@Override
		public ShortestPathAllPairs.Result computeAllCardinalityShortestPaths(Graph g) {
			if (g instanceof IndexGraph)
				return computeAllCardinalityShortestPaths((IndexGraph) g);

			IndexGraph iGraph = g.indexGraph();
			IndexGraphMap viMap = g.indexGraphVerticesMap();
			IndexGraphMap eiMap = g.indexGraphEdgesMap();

			ShortestPathAllPairs.Result indexResult = computeAllCardinalityShortestPaths(iGraph);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		abstract ShortestPathAllPairs.Result computeAllShortestPaths(IndexGraph g, WeightFunction w);

		ShortestPathAllPairs.Result computeAllCardinalityShortestPaths(IndexGraph g) {
			return computeAllShortestPaths(g, WeightFunction.CardinalityWeightFunction);
		}

	}
	static abstract class ResultImpl implements ShortestPathAllPairs.Result {

		private ResultImpl() {}

		abstract void setDistance(int source, int target, double distance);

		abstract int getEdgeTo(int source, int target);

		abstract void setEdgeTo(int source, int target, int edge);

		static abstract class Abstract extends ResultImpl {

			private final IndexGraph g;
			private final int[][] edges;
			private Path negCycle;

			Abstract(IndexGraph g) {
				this.g = g;
				int n = g.vertices().size();
				edges = new int[n][n];
				for (int v = 0; v < n; v++) {
					Arrays.fill(edges[v], -1);
				}
			}

			@Override
			int getEdgeTo(int source, int target) {
				return edges[source][target];
			}

			@Override
			void setEdgeTo(int source, int target, int edge) {
				edges[source][target] = edge;
			}

			void setNegCycle(Path cycle) {
				Objects.requireNonNull(cycle);
				this.negCycle = cycle;
			}

			@Override
			public boolean foundNegativeCycle() {
				return negCycle != null;
			}

			@Override
			public Path getNegativeCycle() {
				if (!foundNegativeCycle())
					throw new IllegalStateException();
				return negCycle;
			}

			IndexGraph graph() {
				return g;
			}
		}

		static class Undirected extends Abstract {

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
					throw new IllegalStateException();
				return source != target ? distances[index(source, target)] : 0;
			}

			@Override
			public Path getPath(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException();
				if (distance(source, target) == Double.POSITIVE_INFINITY)
					return null;
				IntList path = new IntArrayList();
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e != -1;
					path.add(e);
					v = graph().edgeEndpoint(e, v);
				}
				return new PathImpl(graph(), source, target, path);
			}

		}

		static class Directed extends Abstract {
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
					throw new IllegalStateException();
				return distances[source][target];
			}

			@Override
			public Path getPath(int source, int target) {
				if (foundNegativeCycle())
					throw new IllegalStateException();
				if (distance(source, target) == Double.POSITIVE_INFINITY)
					return null;
				IntList path = new IntArrayList();
				for (int v = source; v != target;) {
					int e = getEdgeTo(v, target);
					assert e != -1;
					assert v == graph().edgeSource(e);
					path.add(e);
					v = graph().edgeTarget(e);
				}
				return new PathImpl(graph(), source, target, path);
			}

		}

	}

	static class ResFromSSSP implements ShortestPathAllPairs.Result {

		final ShortestPathSingleSource.Result[] ssspResults;

		ResFromSSSP(int n) {
			ssspResults = new ShortestPathSingleSource.Result[n];
		}

		@Override
		public double distance(int source, int target) {
			return ssspResults[source].distance(target);
		}

		@Override
		public Path getPath(int source, int target) {
			return ssspResults[source].getPath(target);
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException();
		}

	}

	private static class ResultFromIndexResult implements ShortestPathAllPairs.Result {

		private final ShortestPathAllPairs.Result res;
		private final IndexGraphMap viMap;
		private final IndexGraphMap eiMap;

		ResultFromIndexResult(ShortestPathAllPairs.Result res, IndexGraphMap viMap, IndexGraphMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public double distance(int source, int target) {
			return res.distance(viMap.idToIndex(source), viMap.idToIndex(target));
		}

		@Override
		public Path getPath(int source, int target) {
			Path indexPath = res.getPath(viMap.idToIndex(source), viMap.idToIndex(target));
			return PathImpl.pathFromIndexPath(indexPath, viMap, eiMap);
		}

		@Override
		public boolean foundNegativeCycle() {
			return res.foundNegativeCycle();
		}

		@Override
		public Path getNegativeCycle() {
			return PathImpl.pathFromIndexPath(res.getNegativeCycle(), viMap, eiMap);
		}

	}

}
