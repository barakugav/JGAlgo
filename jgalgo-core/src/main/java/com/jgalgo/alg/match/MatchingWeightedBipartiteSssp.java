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

package com.jgalgo.alg.match;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.alg.bipartite.BipartiteGraphs;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSource;
import com.jgalgo.alg.shortestpath.ShortestPathSingleSourceDijkstra;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Maximum weighted matching algorithm using {@link ShortestPathSingleSource} for bipartite graphs.
 *
 * <p>
 * The running time of this algorithm is \(O(m n + n^2 \log n)\) and it uses linear space. If a different
 * {@link ShortestPathSingleSource} algorithm is provided using {@link #setSsspAlgo(ShortestPathSingleSource)} the
 * running time will be \(O(n)\) times the running time of the shortest path algorithm on a graph of size \(O(n)\).
 *
 * @author Barak Ugav
 */
public class MatchingWeightedBipartiteSssp extends MatchingAlgoAbstractBasedMaximum {

	private ShortestPathSingleSource ssspPositive = ShortestPathSingleSource.newInstance();
	private ShortestPathSingleSource ssspNegative = ShortestPathSingleSource.builder().negativeWeights(true).build();

	/**
	 * Create a new maximum weighted matching object.
	 */
	public MatchingWeightedBipartiteSssp() {}

	/**
	 * Set the {@link ShortestPathSingleSource} algorithm used by this algorithm.
	 *
	 * <p>
	 * The shortest path algorithm should support non negative floating points weights. The default implementation uses
	 * {@linkplain ShortestPathSingleSourceDijkstra Dijkstra's algorithm}.
	 *
	 * @param algo an shortest path algorithm
	 */
	public void setSsspAlgo(ShortestPathSingleSource algo) {
		ssspPositive = Objects.requireNonNull(algo);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException     if the bipartiteness vertices weights is not found. See
	 *                                      {@link BipartiteGraphs#VertexBiPartitionWeightKey}.
	 * @throws IllegalArgumentException if the graph is no bipartite with respect to the provided partition
	 */
	@Override
	protected IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		IWeightsBool partition = Assertions.onlyBipartite(g);
		Assertions.onlyBipartite(g, partition);

		int[] match = computeMaxMatching(g, w, partition);
		return new IndexMatching(g, match);
	}

	private int[] computeMaxMatching(IndexGraph gOrig, IWeightFunction wOrig, IWeightsBool partition) {
		wOrig = IWeightFunction.replaceNullWeightFunc(wOrig);
		final int n = gOrig.vertices().size();
		IndexGraph g = IndexGraphFactory
				.directed()
				.allowSelfEdges()
				.allowParallelEdges()
				.expectedVerticesNum(n + 2)
				.expectedEdgesNum(gOrig.edges().size() + n)
				.newGraph();
		g.addVertices(gOrig.vertices());
		final int s = g.addVertexInt(), t = g.addVertexInt();
		IWeightsDouble w = g.addEdgesWeights("weight", double.class);

		for (int e : range(gOrig.edges().size())) {
			int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
			if (!partition.get(u)) {
				assert partition.get(v);
				int temp = u;
				u = v;
				v = temp;
			}
			assert partition.get(u);
			assert !partition.get(v);

			double weight = wOrig.weight(e);
			if (weight >= 0) {
				int e0 = g.addEdge(u, v);
				assert e0 == e;
				w.set(e, weight);
			} else {
				/* no reason to match negative edges. we can ignore this edge */
				/* to match the original graph edges ids, we add a dummy edge */
				int e0 = g.addEdge(t, t);
				assert e0 == e;
				w.set(e, 0);
			}
		}

		int[] match = new int[n];
		Arrays.fill(match, -1);

		final double maxWeight = range(g.edges().size()).mapToDouble(w::weight).max().orElse(Double.MIN_VALUE);
		if (!Double.isFinite(maxWeight))
			throw new IllegalArgumentException("non finite weights");
		final double RemovedEdgeWeight = maxWeight * n;

		// Negate unmatched edges
		for (int e : range(g.edges().size()))
			w.set(e, -w.weight(e));
		// Connected unmatched vertices to fake vertices s,t
		for (int u : range(n)) {
			if (partition.get(u)) {
				w.set(g.addEdge(s, u), 0);
			} else {
				w.set(g.addEdge(u, t), 0);
			}
		}

		double[] potential = new double[n + 2];
		IWeightFunction spWeightFunc = e -> w.weight(e) + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];

		// Init state may include negative distances, use Bellman Ford to calculate
		// first potential values
		ShortestPathSingleSource.IResult sp =
				(ShortestPathSingleSource.IResult) ssspNegative.computeShortestPaths(g, w, Integer.valueOf(s));
		for (int v : range(n + 2))
			potential[v] = sp.distance(v);

		for (;;) {
			sp = (ShortestPathSingleSource.IResult) ssspPositive
					.computeShortestPaths(g, spWeightFunc, Integer.valueOf(s));
			IPath augPath = sp.getPath(t);
			double augPathWeight = -(sp.distance(t) + potential[t]);
			if (augPath == null || augPathWeight >= RemovedEdgeWeight || augPathWeight < 0)
				break;

			// avoid using augPath.iterator() as we modify the graph during iteration
			IntIterator it = new IntArrayList(augPath.edges()).iterator();
			// 'remove' edge from S to new matched vertex
			w.set(it.nextInt(), RemovedEdgeWeight);
			for (;;) {
				int matchedEdge = it.nextInt();

				// Reverse newly matched edge
				g.reverseEdge(matchedEdge);
				match[g.edgeSource(matchedEdge)] = match[g.edgeTarget(matchedEdge)] = matchedEdge;
				w.set(matchedEdge, -w.weight(matchedEdge));

				int unmatchedEdge = it.nextInt();

				if (!it.hasNext()) {
					// 'remove' edge from new matched vertex to T
					w.set(unmatchedEdge, RemovedEdgeWeight);
					break;
				}

				// Reverse newly unmatched edge
				g.reverseEdge(unmatchedEdge);
				w.set(unmatchedEdge, -w.weight(unmatchedEdge));
			}

			// Update potential based on the distances
			for (int u : range(n))
				potential[u] += sp.distance(u);
		}

		return match;
	}

	@Override
	protected IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
		if (!WeightFunction.isCardinality(w))
			throw new UnsupportedOperationException("weighted perfect matching is not supported by this algorithm");
		return computeMaximumWeightedMatching(g, null);
	}

}
