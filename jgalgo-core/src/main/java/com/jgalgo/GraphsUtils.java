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

import java.util.Collections;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class GraphsUtils {

	private GraphsUtils() {}

	static int[] calcDegree(Graph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (IntIterator eit = edges.iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	// static String formatAdjacencyMatrix(Graph g) {
	// return formatAdjacencyMatrix(g, e -> e == -1 ? "0" : "1");
	// }

	// static String formatAdjacencyMatrixWeighted(Graph g, EdgeWeightFunc w) {
	// double minWeight = Double.MAX_VALUE;
	// for (IntIterator it = g.edges().iterator(); it.hasNext();) {
	// int e = it.nextInt();
	// double ew = w.weight(e);
	// if (ew < minWeight)
	// minWeight = ew;
	// }

	// int unlimitedPrecision = 64;
	// int precision = minWeight >= 1 ? 2 : Math.min(unlimitedPrecision, (int) -Math.log10(minWeight));

	// return precision == unlimitedPrecision
	// ? formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Double.toString(w.weight(e)))
	// : formatAdjacencyMatrix(g,
	// e -> e == -1 ? "-" : String.format("%." + precision + "f", Double.valueOf(w.weight(e))));
	// }

	// static String formatAdjacencyMatrixWeightedInt(Graph g, EdgeWeightFunc.Int w) {
	// return formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Integer.toString(w.weightInt(e)));
	// }

	// static String formatAdjacencyMatrix(Graph g, Int2ObjectFunction<String> formatter) {
	// int n = g.vertices().size();
	// if (n == 0)
	// return "[]";

	// /* format all edges */
	// String[][] strs = new String[n][n];
	// for (int u = 0; u < n; u++) {
	// for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
	// int e = eit.nextInt();
	// strs[u][eit.target()] = formatter.apply(e);
	// }
	// }

	// /* calculate cell size */
	// int maxStr = 0;
	// for (int u = 0; u < n; u++) {
	// for (int v = 0; v < n; v++) {
	// if (strs[u][v] == null)
	// strs[u][v] = formatter.apply(null);
	// if (strs[u][v].length() > maxStr)
	// maxStr = strs[u][v].length();
	// }
	// }
	// int vertexLabelCellSize = String.valueOf(n - 1).length() + 1;
	// int cellSize = Math.max(maxStr + 1, vertexLabelCellSize);

	// /* format header row */
	// StringBuilder s = new StringBuilder();
	// s.append(strMult(" ", vertexLabelCellSize));
	// for (int v = 0; v < n; v++)
	// s.append(String.format("% " + cellSize + "d", Integer.valueOf(v)));
	// s.append('\n');

	// /* format adjacency matrix */
	// for (int u = 0; u < n; u++) {
	// s.append(String.format("% " + vertexLabelCellSize + "d", Integer.valueOf(u)));
	// for (int v = 0; v < n; v++) {
	// if (strs[u][v].length() < cellSize)
	// s.append(strMult(" ", cellSize - strs[u][v].length()));
	// s.append(strs[u][v]);
	// }
	// s.append('\n');
	// }

	// return s.toString();
	// }

	// private static String strMult(String s, int n) {
	// return String.join("", Collections.nCopies(n, s));
	// }

	static Graph referenceGraph(Graph g, Object refEdgeWeightKey) {
		Graph gRef = GraphBuilder.newDirected().setDirected(g.getCapabilities().directed()).build(g.vertices().size());
		Weights.Int edgeRef = gRef.addEdgesWeights(refEdgeWeightKey, int.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int eRef = gRef.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(eRef, e);
		}
		return gRef;
	}

	static boolean containsSelfLoops(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
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
		int n = g.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

	static interface UndirectedGraphImpl extends Graph {

		@Override
		default EdgeIter edgesIn(int target) {
			return edgesOut(target);
		}

		@Override
		default void removeEdgesOf(int source) {
			for (EdgeIter eit = edgesOut(source); eit.hasNext();) {
				eit.nextInt();
				eit.remove();
			}
		}

		@Override
		default void removeEdgesOutOf(int source) {
			removeEdgesOf(source);
		}

		@Override
		default void removeEdgesInOf(int target) {
			removeEdgesOf(target);
		}

		@Override
		default void reverseEdge(int edge) {
			// Do nothing
		}

		@Override
		default int degreeIn(int target) {
			return degreeOut(target);
		}

	}

	private abstract static class EmptyGraph implements Graph {

		@Override
		public IntSet vertices() {
			return IntSets.emptySet();
		}

		@Override
		public IntSet edges() {
			return IntSets.emptySet();
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			throw new IndexOutOfBoundsException(vertex);
		}

		// @Override
		// public void removeVertices(IntCollection vs) {
		// if (!vs.isEmpty())
		// throw new IndexOutOfBoundsException(vs.iterator().nextInt());
		// }

		@Override
		public EdgeIter edgesOut(int source) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public EdgeIter edgesIn(int target) {
			throw new IndexOutOfBoundsException(target);
		}

		@Override
		public EdgeIter getEdges(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public int addEdge(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public void removeEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		// @Override
		// public void removeEdges(IntCollection edges) {
		// if (!edges.isEmpty())
		// throw new IndexOutOfBoundsException(edges.iterator().nextInt());
		// }

		@Override
		public void reverseEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeSource(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public void clear() {}

		@Override
		public void clearEdges() {}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return null;
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVerticesWeights(Object key) {}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return Collections.emptySet();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return null;
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdgesWeights(Object key) {}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return Collections.emptySet();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getVerticesIDStrategy'");
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getEdgesIDStrategy'");
		}

	}

	static final Graph EmptyGraphUndirected = new EmptyGraph() {
		@Override
		public GraphCapabilities getCapabilities() {
			return EmptyCapabilitiesUndirected;
		}
	};

	static final Graph EmptyGraphDirected = new EmptyGraph() {
		@Override
		public GraphCapabilities getCapabilities() {
			return EmptyCapabilitiesDirected;
		}
	};

	private abstract static class EmptyCapabilities implements GraphCapabilities {

		@Override
		public boolean vertexAdd() {
			return false;
		}

		@Override
		public boolean vertexRemove() {
			return false;
		}

		@Override
		public boolean edgeAdd() {
			return false;
		}

		@Override
		public boolean edgeRemove() {
			return false;
		}

		@Override
		public boolean parallelEdges() {
			return false;
		}

		@Override
		public boolean selfEdges() {
			return false;
		}
	}

	static final GraphCapabilities EmptyCapabilitiesUndirected = new EmptyCapabilities() {
		@Override
		public boolean directed() {
			return false;
		}
	};

	static final GraphCapabilities EmptyCapabilitiesDirected = new EmptyCapabilities() {
		@Override
		public boolean directed() {
			return true;
		}
	};

	static double edgesWeightSum(IntIterator eit, EdgeWeightFunc w) {
		if (w instanceof EdgeWeightFunc.Int) {
			EdgeWeightFunc.Int w0 = (EdgeWeightFunc.Int) w;
			int sum = 0;
			while (eit.hasNext())
				sum += w0.weightInt(eit.nextInt());
			return sum;

		} else {
			double sum = 0;
			while (eit.hasNext())
				sum += w.weight(eit.nextInt());
			return sum;
		}
	}

}
