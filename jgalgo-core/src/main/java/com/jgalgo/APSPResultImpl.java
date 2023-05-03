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

abstract class APSPResultImpl implements APSP.Result {

	private APSPResultImpl() {}

	abstract void setDistance(int source, int target, double distance);

	abstract int getEdgeTo(int source, int target);

	abstract void setEdgeTo(int source, int target, int edge);

	static abstract class Abstract extends APSPResultImpl {

		private final Graph g;
		private final int[][] edges;
		private Path negCycle;

		Abstract(Graph g) {
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

		Graph graph() {
			return g;
		}
	}

	static class Undirected extends Abstract {

		private final int n;
		private final double[] distances;

		Undirected(Graph g) {
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
			return new Path(graph(), source, target, path);
		}

	}

	static class Directed extends Abstract {
		private final double[][] distances;

		Directed(Graph g) {
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
			return new Path(graph(), source, target, path);
		}

	}

}
