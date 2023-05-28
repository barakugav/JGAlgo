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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;

class SSSPUtils {

	static class BuilderImpl implements SSSP.Builder {

		private boolean intWeights;
		private boolean negativeWeights;
		private double maxDistance = Double.POSITIVE_INFINITY;
		private boolean dagGraphs;
		private boolean cardinalityWeight;

		@Override
		public SSSP build() {
			if (cardinalityWeight)
				return new SSSPCardinality();
			if (dagGraphs)
				return new SSSPDag();
			if (negativeWeights) {
				if (intWeights) {
					return new SSSPGoldberg();
				} else {
					return new SSSPBellmanFord();
				}
			} else {
				if (intWeights && maxDistance < Integer.MAX_VALUE) {
					return new SSSP() {
						private final SSSPDial ssspDial = new SSSPDial();
						private final SSSPDijkstra ssspDijkstra = new SSSPDijkstra();
						private final int maxDistance = (int) BuilderImpl.this.maxDistance;

						@Override
						public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
							final int n = g.vertices().size(), m = g.edges().size();
							int dialWork = n + m + maxDistance;
							int dijkstraWork = m + n * Utils.log2ceil(n);
							if (dialWork < dijkstraWork) {
								return ssspDial.computeShortestPaths(g, (EdgeWeightFunc.Int) w, source, maxDistance);
							} else {
								return ssspDijkstra.computeShortestPaths(g, w, source);
							}
						}

					};
				}
				return new SSSPDijkstra();
			}
		}

		@Override
		public SSSP.Builder setIntWeights(boolean enable) {
			intWeights = enable;
			return this;
		}

		@Override
		public SSSP.Builder setNegativeWeights(boolean enable) {
			negativeWeights = enable;
			return this;
		}

		@Override
		public SSSP.Builder setMaxDistance(double maxDistance) {
			this.maxDistance = maxDistance;
			return this;
		}

		@Override
		public SSSP.Builder setDag(boolean dagGraphs) {
			this.dagGraphs = dagGraphs;
			return this;
		}

		@Override
		public SSSP.Builder setCardinality(boolean cardinalityWeight) {
			this.cardinalityWeight = cardinalityWeight;
			return this;
		}
	}

	static class ResultImpl implements SSSP.Result {

		private final Graph g;
		private final int source;
		final double[] distances;
		final int[] backtrack;

		ResultImpl(Graph g, int source) {
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
					if (e == -1)
						break;
					path.add(e);
					v = g.edgeSource(e);
				}
			} else {
				for (int v = target;;) {
					int e = backtrack[v];
					if (e == -1)
						break;
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
			}
			IntArrays.reverse(path.elements(), 0, path.size());
			return new Path(g, source, target, path);
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

		static class Int implements SSSP.Result {

			private final Graph g;
			private final int source;
			final int[] distances;
			final int[] backtrack;

			Int(Graph g, int source) {
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
					if (e == -1)
						break;
					path.add(e);
					v = g.edgeEndpoint(e, v);
				}
				IntArrays.reverse(path.elements(), 0, path.size());
				return new Path(g, source, target, path);
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
