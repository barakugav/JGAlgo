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
package com.jgalgo.alg.cycle;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.alg.euler.EulerianTourAlgo;
import com.jgalgo.alg.match.IMatching;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.alg.path.IPath;
import com.jgalgo.alg.path.ShortestPathAllPairs;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

class ChinesePostmanImpl extends ChinesePostmanUtils.AbstractImpl {

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

	@Override
	IPath computeShortestEdgeVisitorCircle(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		// if (!connectedComponentsAlgo.isWeaklyConnected(g))
		// throw new IllegalArgumentException("Graph is not connected, cannot compute shortest edge visitor circle");
		// If the graph is not connected, we will fail to find an Eulerian tour, so we just fail later

		/* Find all vertices with odd degree */
		IntList oddVerticesList = new IntArrayList();
		for (int v : range(g.vertices().size()))
			if (nonSelfEdgesDegree(g, v) % 2 != 0)
				oddVerticesList.add(v);
		if (oddVerticesList.isEmpty())
			/* all vertices have even degree, an Eulerian tour should exists (if the graph is connected) */
			return (IPath) eulerianTourAlgo.computeEulerianTour(g);
		int[] oddVertices = oddVerticesList.toIntArray();
		assert oddVertices.length % 2 == 0;

		/* Create a complete graph of the odd vertices, with edges weighted by the shortest paths between each pair */
		IndexGraphBuilder oddGraph0 = IndexGraphBuilder.undirected();
		oddGraph0.ensureVertexCapacity(oddVertices.length);
		oddGraph0.ensureEdgeCapacity(oddVertices.length * (oddVertices.length - 1) / 2);
		oddGraph0.addVertices(range(oddVertices.length));
		for (int v : range(oddVertices.length))
			for (int u : range(v + 1, oddVertices.length))
				oddGraph0.addEdge(v, u);
		IndexGraph oddGraph = oddGraph0.reIndexAndBuild(true, true).graph;
		ShortestPathAllPairs.IResult allPairsRes = (ShortestPathAllPairs.IResult) shortestPathAllPairsAlgo
				.computeSubsetShortestPaths(g, oddVerticesList, w);
		IWeightFunction oddW = e -> {
			int u = oddVertices[oddGraph.edgeSource(e)];
			int v = oddVertices[oddGraph.edgeTarget(e)];
			return allPairsRes.distance(u, v);
		};
		/* Compute a minimum weighted perfected matching between the odd vertices */
		IMatching oddMatching = (IMatching) matchingAlgo.computeMinimumPerfectMatching(oddGraph, oddW);

		/* Create a graph with the original vertices and edges, and add edges resulted from the perfect matching */
		IndexGraphBuilder b = IndexGraphBuilder.undirected();
		b.ensureEdgeCapacity(g.edges().size() + oddMatching.edges().size());
		b.addVertices(g.vertices());
		b.addEdges(EdgeSet.allOf(g));
		final int originalEdgesThreshold = b.edges().size();
		for (int e : oddMatching.edges()) {
			int u = oddVertices[oddGraph.edgeSource(e)];
			int v = oddVertices[oddGraph.edgeTarget(e)];
			b.addEdge(u, v);
		}
		/* The new graph is Eulerian */
		IndexGraph eulerianGraph = b.build();
		for (int v : range(eulerianGraph.vertices().size()))
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
		return IPath.valueOf(g, pathSource, pathSource, path);
	}

}
