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
import it.unimi.dsi.fastutil.ints.IntArrays;

class ShortestPathSingleSourceUtils {

	static abstract class AbstractImpl implements ShortestPathSingleSource {

		@Override
		public ShortestPathSingleSource.Result computeShortestPaths(Graph g, WeightFunction w, int source) {
			if (g instanceof IndexGraph)
				return computeShortestPaths((IndexGraph) g, w, source);

			IndexGraph iGraph = g.indexGraph();
			IndexGraphMap viMap = g.indexGraphVerticesMap();
			IndexGraphMap eiMap = g.indexGraphEdgesMap();
			w = WeightsImpl.indexWeightFuncFromIdWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);

			ShortestPathSingleSource.Result indexResult = computeShortestPaths(iGraph, w, iSource);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		@Override
		public ShortestPathSingleSource.Result computeCardinalityShortestPaths(Graph g, int source) {
			if (g instanceof IndexGraph)
				return computeCardinalityShortestPaths((IndexGraph) g, source);

			IndexGraph iGraph = g.indexGraph();
			IndexGraphMap viMap = g.indexGraphVerticesMap();
			IndexGraphMap eiMap = g.indexGraphEdgesMap();
			int iSource = viMap.idToIndex(source);

			ShortestPathSingleSource.Result indexResult = computeCardinalityShortestPaths(iGraph, iSource);
			return new ResultFromIndexResult(indexResult, viMap, eiMap);
		}

		abstract ShortestPathSingleSource.Result computeShortestPaths(IndexGraph g, WeightFunction w, int source);

		ShortestPathSingleSource.Result computeCardinalityShortestPaths(IndexGraph g, int source) {
			return computeShortestPaths(g, WeightFunction.CardinalityWeightFunction, source);
		}

	}

	private static class ResultFromIndexResult implements ShortestPathSingleSource.Result {

		private final ShortestPathSingleSource.Result result;
		private final IndexGraphMap viMap;
		private final IndexGraphMap eiMap;

		ResultFromIndexResult(ShortestPathSingleSource.Result result, IndexGraphMap viMap, IndexGraphMap eiMap) {
			this.result = Objects.requireNonNull(result);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public double distance(int target) {
			return result.distance(viMap.idToIndex(target));
		}

		@Override
		public Path getPath(int target) {
			return PathImpl.pathFromIndexPath(result.getPath(viMap.idToIndex(target)), viMap, eiMap);
		}

		@Override
		public boolean foundNegativeCycle() {
			return result.foundNegativeCycle();
		}

		@Override
		public Path getNegativeCycle() {
			return PathImpl.pathFromIndexPath(result.getNegativeCycle(), viMap, eiMap);
		}
	}

	static class BuilderImpl implements ShortestPathSingleSource.Builder {

		private boolean intWeights;
		private boolean negativeWeights;
		private double maxDistance = Double.POSITIVE_INFINITY;
		private boolean dagGraphs;
		private boolean cardinalityWeight;

		@Override
		public ShortestPathSingleSource build() {
			if (cardinalityWeight)
				return new ShortestPathSingleSourceCardinality();
			if (dagGraphs)
				return new ShortestPathSingleSourceDag();
			if (negativeWeights) {
				if (intWeights) {
					return new ShortestPathSingleSourceGoldberg();
				} else {
					return new ShortestPathSingleSourceBellmanFord();
				}
			} else {
				if (intWeights && maxDistance < Integer.MAX_VALUE) {
					return new ShortestPathSingleSourceUtils.AbstractImpl() {
						private final ShortestPathSingleSourceDial ssspDial = new ShortestPathSingleSourceDial();
						private final ShortestPathSingleSourceDijkstra ssspDijkstra =
								new ShortestPathSingleSourceDijkstra();
						private final int maxDistance = (int) BuilderImpl.this.maxDistance;

						@Override
						ShortestPathSingleSource.Result computeShortestPaths(IndexGraph g, WeightFunction w,
								int source) {
							final int n = g.vertices().size(), m = g.edges().size();
							int dialWork = n + m + maxDistance;
							int dijkstraWork = m + n * Utils.log2ceil(n);
							if (dialWork < dijkstraWork) {
								return ssspDial.computeShortestPaths(g, (WeightFunction.Int) w, source, maxDistance);
							} else {
								return ssspDijkstra.computeShortestPaths(g, w, source);
							}
						}

					};
				}
				return new ShortestPathSingleSourceDijkstra();
			}
		}

		@Override
		public ShortestPathSingleSource.Builder setIntWeights(boolean enable) {
			intWeights = enable;
			return this;
		}

		@Override
		public ShortestPathSingleSource.Builder setNegativeWeights(boolean enable) {
			negativeWeights = enable;
			return this;
		}

		@Override
		public ShortestPathSingleSource.Builder setMaxDistance(double maxDistance) {
			this.maxDistance = maxDistance;
			return this;
		}

		@Override
		public ShortestPathSingleSource.Builder setDag(boolean dagGraphs) {
			this.dagGraphs = dagGraphs;
			return this;
		}

		@Override
		public ShortestPathSingleSource.Builder setCardinality(boolean cardinalityWeight) {
			this.cardinalityWeight = cardinalityWeight;
			return this;
		}
	}

	static class ResultImpl implements ShortestPathSingleSource.Result {

		private final IndexGraph g;
		private final int source;
		final double[] distances;
		final int[] backtrack;

		ResultImpl(IndexGraph g, int source) {
			this.g = g;
			this.source = source;
			int n = g.vertices().size();
			distances = new double[n];
			backtrack = new int[n];
			Arrays.fill(distances, Double.POSITIVE_INFINITY);
			Arrays.fill(backtrack, -1);
		}

		@Override
		public double distance(int target) {
			return distances[target];
		}

		@Override
		public Path getPath(int target) {
			if (distances[target] == Double.POSITIVE_INFINITY)
				return null;
			IntArrayList path = new IntArrayList();
			if (g.getCapabilities().directed()) {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1) {
						assert v == source;
						break;
					}
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1) {
						assert v == source;
						break;
					}
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return new PathImpl(g, source, target, path);
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		@Override
		public String toString() {
			return Arrays.toString(distances);
		}

		static class Int implements ShortestPathSingleSource.Result {

			private final IndexGraph g;
			private final int source;
			final int[] distances;
			final int[] backtrack;

			Int(IndexGraph g, int source) {
				this.g = g;
				this.source = source;
				int n = g.vertices().size();
				distances = new int[n];
				backtrack = new int[n];
				Arrays.fill(distances, Integer.MAX_VALUE);
				Arrays.fill(backtrack, -1);
			}

			@Override
			public double distance(int target) {
				int d = distances[target];
				return d != Integer.MAX_VALUE ? d : Double.POSITIVE_INFINITY;
			}

			@Override
			public Path getPath(int target) {
				if (distances[target] == Integer.MAX_VALUE)
					return null;
				IntArrayList path = new IntArrayList();
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1) {
						assert v == source;
						break;
					}
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
				IntArrays.reverse(path.elements(), 0, path.size());
				return new PathImpl(g, source, target, path);
			}

			@Override
			public boolean foundNegativeCycle() {
				return false;
			}

			@Override
			public Path getNegativeCycle() {
				throw new IllegalStateException("no negative cycle found");
			}

			@Override
			public String toString() {
				return Arrays.toString(distances);
			}
		}

	}
}
