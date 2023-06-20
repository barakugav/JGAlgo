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

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphsUtils {

	private GraphsUtils() {}

	static int[] calcDegree(IndexGraph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (int e : edges) {
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	static boolean containsSelfEdges(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		for (int u : ig.vertices()) {
			for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				if (u == eit.target())
					return true;
			}
		}
		return false;
	}

	static boolean containsParallelEdges(Graph g) {
		if (!g.getCapabilities().parallelEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		int n = ig.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

	static class GraphCapabilitiesBuilder {

		private boolean parallelEdges;
		private boolean parallelEdgesValid;
		private boolean selfEdges;
		private boolean selfEdgesValid;
		private boolean directed;
		private boolean directedValid;

		private GraphCapabilitiesBuilder(boolean directed) {
			this.directed = directed;
			directedValid = true;
		}

		static GraphCapabilitiesBuilder newUndirected() {
			return new GraphCapabilitiesBuilder(false);
		}

		static GraphCapabilitiesBuilder newDirected() {
			return new GraphCapabilitiesBuilder(true);
		}

		GraphCapabilities build() {
			if (!parallelEdgesValid || !selfEdgesValid || !directedValid)
				throw new IllegalStateException();
			return new GraphCapabilitiesImpl(parallelEdges, selfEdges, directed);
		}

		GraphCapabilitiesBuilder parallelEdges(boolean enable) {
			parallelEdges = enable;
			parallelEdgesValid = true;
			return this;
		}

		GraphCapabilitiesBuilder selfEdges(boolean enable) {
			selfEdges = enable;
			selfEdgesValid = true;
			return this;
		}

		GraphCapabilitiesBuilder directed(boolean enable) {
			directed = enable;
			directedValid = true;
			return this;
		}

	}

	private static class GraphCapabilitiesImpl implements GraphCapabilities {

		private final boolean parallelEdges;
		private final boolean selfEdges;
		private final boolean directed;

		GraphCapabilitiesImpl(boolean parallelEdges, boolean selfEdges, boolean directed) {
			this.parallelEdges = parallelEdges;
			this.selfEdges = selfEdges;
			this.directed = directed;
		}

		@Override
		public boolean parallelEdges() {
			return parallelEdges;
		}

		@Override
		public boolean selfEdges() {
			return selfEdges;
		}

		@Override
		public boolean directed() {
			return directed;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder().append('<');
			s.append(" parallelEdges=").append(parallelEdges ? 'v' : 'x');
			s.append(" selfEdges=").append(selfEdges ? 'v' : 'x');
			s.append(" directed=").append(directed ? 'v' : 'x');
			return s.append('>').toString();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof GraphCapabilities))
				return false;
			GraphCapabilities o = (GraphCapabilities) other;
			return parallelEdges == o.parallelEdges() && selfEdges == o.selfEdges() && directed == o.directed();
		}

		@Override
		public int hashCode() {
			int h = 0;
			/* we must use addition as the order shouldn't matter */
			h += parallelEdges ? 1 : 0;
			h += selfEdges ? 1 : 0;
			h += directed ? 1 : 0;
			return h;
		}
	}

	static double weightSum(IntIterable collection, WeightFunction w) {
		return weightSum(collection.iterator(), w);
	}

	static double weightSum(IntIterator it, WeightFunction w) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			int cardinality = 0;
			for (; it.hasNext(); it.nextInt())
				cardinality++;
			return cardinality;
		}

		if (w instanceof WeightFunction.Int) {
			WeightFunction.Int wInt = (WeightFunction.Int) w;
			int sum = 0;
			while (it.hasNext())
				sum += wInt.weightInt(it.nextInt());
			return sum;

		} else {
			double sum = 0;
			while (it.hasNext())
				sum += w.weight(it.nextInt());
			return sum;
		}
	}

	static final IndexIdMap IndexIdMapIdentify = new IndexGraphMapIdentify();

	private static class IndexGraphMapIdentify implements IndexIdMap {
		@Override
		public int indexToId(int index) {
			return index;
		}

		@Override
		public int idToIndex(int id) {
			return id;
		}
	}

}
