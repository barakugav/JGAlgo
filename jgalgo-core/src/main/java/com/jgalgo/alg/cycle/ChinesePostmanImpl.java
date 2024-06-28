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
import java.util.Optional;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.euler.EulerianTourAlgo;
import com.jgalgo.alg.match.IMatching;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.alg.path.ShortestPathAllPairs;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * An algorithm for the chinese postman problem using minimum weighted perfect matching and Eulerian tour.
 *
 * <p>
 * The chinese postman problem is to find a closed path that visits all edges in the graph at least once, with minimum
 * weight sum with respect to a given edge weight function. The algorithm uses the following steps:
 * <ol>
 * <li>Find all vertices with odd degree. If all vertices have even degree, an Eulerian tour should exists (if the graph
 * is connected).</li>
 * <li>Create a complete graph of the odd vertices, with edges weighted by the shortest paths between each pair.</li>
 * <li>Compute a minimum weighted perfect matching between the odd vertices.</li>
 * <li>Create a graph with the original vertices and edges, and for each edge resulted from the perfect matching, add an
 * edge between the two vertices in the original graph.</li>
 * <li>Compute an Eulerian tour in the new graph.</li>
 * <li>Construct the full result path by replacing each artificial edge connecting two odd vertices with the shortest
 * path between them.</li>
 * </ol>
 *
 * <p>
 * The running time and space are dominated by the shortest path all pairs and minimum perfect matching algorithms.
 * Other than that, the algorithm use linear time and space.
 *
 * @author Barak Ugav
 */
public class ChinesePostmanImpl extends ChinesePostmanAbstract {

	private final EulerianTourAlgo eulerianTourAlgo = EulerianTourAlgo.newInstance();
	private final ShortestPathAllPairs shortestPathAllPairsAlgo = ShortestPathAllPairs.newInstance();
	private final MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();

	/**
	 * Create a new instance of the Chinese postman algorithm.
	 *
	 * <p>
	 * Please prefer using {@link ChinesePostman#newInstance()} to get a default implementation for the
	 * {@link ChinesePostman} interface.
	 */
	public ChinesePostmanImpl() {}

	@Override
	protected Optional<IPath> computeShortestEdgeVisitorCircleIfExist(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);

		/* Find all vertices with odd degree */
		IntList oddVerticesList = new IntArrayList();
		for (int v : range(g.vertices().size()))
			if (nonSelfEdgesDegree(g, v) % 2 != 0)
				oddVerticesList.add(v);
		if (oddVerticesList.isEmpty())
			/* all vertices have even degree, an Eulerian tour should exists (if the graph is connected) */
			return eulerianTourAlgo.computeEulerianTourIfExist(g).map(p -> (IPath) p);
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
		assert range(eulerianGraph.vertices().size()).allMatch(v -> nonSelfEdgesDegree(eulerianGraph, v) % 2 == 0);

		/* Compute an Eulerian tour in the new graph */
		Optional<IPath> eulerianTour0 = eulerianTourAlgo.computeEulerianTourIfExist(eulerianGraph).map(p -> (IPath) p);
		if (eulerianTour0.isEmpty())
			return Optional.empty();
		IPath eulerianTour = eulerianTour0.get();

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
		return Optional.of(IPath.valueOf(g, pathSource, pathSource, path));
	}

	private static int nonSelfEdgesDegree(IndexGraph g, int v) {
		int nonSelfEdgesCount = 0;
		for (int e : g.outEdges(v))
			if (g.edgeSource(e) != g.edgeTarget(e))
				nonSelfEdgesCount++;
		return nonSelfEdgesCount;
	}

}
