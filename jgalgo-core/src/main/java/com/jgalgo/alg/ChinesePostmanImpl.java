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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class ChinesePostmanImpl implements ChinesePostman {

	// private final WeaklyConnectedComponentsAlgo connectedComponentsAlgo =
	// WeaklyConnectedComponentsAlgo.newInstance();
	private final EulerianTourAlgo eulerianTourAlgo = EulerianTourAlgo.newInstance();
	private final ShortestPathAllPairs shortestPathAllPairsAlgo = ShortestPathAllPairs.newInstance();
	private final MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();

	private static int nonSelfEdgesDegree(IndexGraph g, int v) {
		int nonSelfEdgesCount = 0;
		for (int e : g.outEdges(v))
			if (g.edgeSource(e) != g.edgeTarget(e))
				nonSelfEdgesCount++;
		return nonSelfEdgesCount;
	}

	IPath computeShortestEdgeVisitorCircle(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		// if (!connectedComponentsAlgo.isWeaklyConnected(g))
		// throw new IllegalArgumentException("Graph is not connected, cannot compute shortest edge visitor circle");
		// If the graph is not connected, we will fail to find an Eulerian tour, so we just fail later

		/* Find all vertices with odd degree */
		IntList oddVertices = new IntArrayList();
		for (int n = g.vertices().size(), v = 0; v < n; v++)
			if (nonSelfEdgesDegree(g, v) % 2 != 0)
				oddVertices.add(v);
		if (oddVertices.isEmpty())
			/* all vertices have even degree, an Eulerian tour should exists (if the graph is connected) */
			return (IPath) eulerianTourAlgo.computeEulerianTour(g);
		assert oddVertices.size() % 2 == 0;

		/* Find the shortest path between each pair of odd degree vertices */
		ShortestPathAllPairs.IResult allPairsRes =
				(ShortestPathAllPairs.IResult) shortestPathAllPairsAlgo.computeSubsetShortestPaths(g, oddVertices, w);
		/* Create a complete graph of the odd vertices, with edges weighted by the shortest paths between each pair */
		IndexGraph oddGraph = Graphs.newCompleteGraphUndirected(oddVertices.size());
		IWeightFunction oddW = e -> {
			int u = oddVertices.getInt(oddGraph.edgeSource(e));
			int v = oddVertices.getInt(oddGraph.edgeTarget(e));
			return allPairsRes.distance(u, v);
		};
		/* Compute a minimum weighted perfected matching between the odd vertices */
		IMatching oddMatching = (IMatching) matchingAlgo.computeMinimumWeightedPerfectMatching(oddGraph, oddW);

		/* Create a graph with the original vertices and edges, and add edges resulted from the perfect matching */
		IndexGraphBuilder b = IndexGraphBuilder.newUndirected();
		b.expectedVerticesNum(g.vertices().size());
		b.expectedEdgesNum(g.edges().size() + oddMatching.edges().size());
		for (int n = g.vertices().size(), v = 0; v < n; v++)
			b.addVertex();
		for (int m = g.edges().size(), e = 0; e < m; e++)
			b.addEdge(g.edgeSource(e), g.edgeTarget(e));
		final int originalEdgesThreshold = b.edges().size();
		for (int e : oddMatching.edges()) {
			int u = oddVertices.getInt(oddGraph.edgeSource(e));
			int v = oddVertices.getInt(oddGraph.edgeTarget(e));
			b.addEdge(u, v);
		}
		/* The new graph is Eulerian */
		IndexGraph eulerianGraph = b.build();
		for (int n = eulerianGraph.vertices().size(), v = 0; v < n; v++)
			assert nonSelfEdgesDegree(eulerianGraph, v) % 2 == 0;

		/* Compute an Eulerian tour in the new graph */
		IPath eulerianTour = (IPath) eulerianTourAlgo.computeEulerianTour(eulerianGraph);
		/* Replace each artificial edge connecting two odd vertices with the shortest path between them */
		IntList path = new IntArrayList(eulerianTour.edges().size());
		for (IEdgeIter eit = eulerianTour.edgeIter(); eit.hasNext();) {
			int e = eit.nextInt();
			if (e < originalEdgesThreshold) {
				/* an original edge */
				path.add(e);
			} else {
				/* artificial edge connecting two odd vertices */
				path.addAll(allPairsRes.getPath(eit.sourceInt(), eit.targetInt()).edges());
			}
		}

		int pathSource = eulerianTour.sourceInt();
		return new PathImpl(g, pathSource, pathSource, path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Path<V, E> computeShortestEdgeVisitorCircle(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (Path<V, E>) computeShortestEdgeVisitorCircle((IndexGraph) g, w0);

		} else if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g0.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g0.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
			IPath indexResult = computeShortestEdgeVisitorCircle(iGraph, iw);
			return (Path<V, E>) PathImpl.intPathFromIndexPath(indexResult, viMap, eiMap);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IPath indexResult = computeShortestEdgeVisitorCircle(iGraph, iw);
			return PathImpl.objPathFromIndexPath(indexResult, viMap, eiMap);
		}
	}

}
