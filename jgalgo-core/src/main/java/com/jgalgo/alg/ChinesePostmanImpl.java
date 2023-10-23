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

import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class ChinesePostmanImpl implements ChinesePostman {

	private final ConnectedComponentsAlgo connectedComponentsAlgo = ConnectedComponentsAlgo.newBuilder().build();
	private final EulerianTourAlgo eulerianTourAlgo = EulerianTourAlgo.newBuilder().build();
	private final ShortestPathAllPairs shortestPathAllPairsAlgo = ShortestPathAllPairs.newBuilder().build();
	private final MatchingAlgo matchingAlgo = MatchingAlgo.newBuilder().build();

	private static int nonSelfEdgesDegree(IndexGraph g, int v) {
		int nonSelfEdgesCount = 0;
		for (EdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
			eit.nextInt();
			if (eit.target() != v)
				nonSelfEdgesCount++;
		}
		return nonSelfEdgesCount;
	}

	Path computeShortestEdgeVisitorCircle(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		if (connectedComponentsAlgo.findConnectedComponents(g).getNumberOfCcs() != 1)
			throw new IllegalArgumentException("Graph is not connected, cannot compute shortest edge visitor circle");

		/* Find all vertices with odd degree */
		IntList oddVertices = new IntArrayList();
		for (int n = g.vertices().size(), v = 0; v < n; v++)
			if (nonSelfEdgesDegree(g, v) % 2 != 0)
				oddVertices.add(v);
		if (oddVertices.isEmpty())
			/* all vertices have even degree, an Eulerian tour should exists (if the graph is connected) */
			return eulerianTourAlgo.computeEulerianTour(g);
		assert oddVertices.size() % 2 == 0;

		/* Find the shortest path between each pair of odd degree vertices */
		ShortestPathAllPairs.Result allPairsRes =
				shortestPathAllPairsAlgo.computeSubsetShortestPaths(g, oddVertices, w);
		/* Create a complete graph of the odd vertices, with edges weighted by the shortest paths between each pair */
		IndexGraph oddGraph = Graphs.newCompleteGraphUndirected(oddVertices.size());
		WeightFunction oddW = e -> {
			int u = oddVertices.getInt(oddGraph.edgeSource(e));
			int v = oddVertices.getInt(oddGraph.edgeTarget(e));
			return allPairsRes.distance(u, v);
		};
		/* Compute a minimum weighted perfected matching between the odd vertices */
		Matching oddMatching = matchingAlgo.computeMinimumWeightedPerfectMatching(oddGraph, oddW);

		/* Create a graph with the original vertices and edges, and add edges resulted from the perfect matching */
		IndexGraphBuilder b = IndexGraphBuilder.newFrom(g);
		final int originalEdgesThreshold = g.edges().size();
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
		Path eulerianTour = eulerianTourAlgo.computeEulerianTour(eulerianGraph);
		/* Replace each artificial edge connecting two odd vertices with the shortest path between them */
		IntList path = new IntArrayList(eulerianTour.size());
		for (EdgeIter eit = eulerianTour.edgeIter(); eit.hasNext();) {
			int e = eit.nextInt();
			if (e < originalEdgesThreshold) {
				/* an original edge */
				path.add(e);
			} else {
				/* artificial edge connecting two odd vertices */
				path.addAll(allPairsRes.getPath(eit.source(), eit.target()));
			}
		}

		int pathSource = eulerianTour.source();
		return new PathImpl(g, pathSource, pathSource, path);
	}

	@Override
	public Path computeShortestEdgeVisitorCircle(Graph g, WeightFunction w) {
		if (g instanceof IndexGraph)
			return computeShortestEdgeVisitorCircle((IndexGraph) g, w);
		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		WeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
		Path indexResult = computeShortestEdgeVisitorCircle(iGraph, iw);
		return PathImpl.pathFromIndexPath(indexResult, viMap, eiMap);
	}

}
