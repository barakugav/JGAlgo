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

package com.jgalgo.alg.shortestpath;

import static com.jgalgo.internal.util.Numbers.log2;
import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.connect.StronglyConnectedComponentsAlgo;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Goldberg's SSSP algorithm for integer (positive and negative) weights on directed graphs.
 *
 * <p>
 * The algorithm operate on integer weights and uses the scaling approach. During the scaling iterations, a potential
 * function is maintained, which gives a equivalent weight function with values \(-1,0,1,2,3,\ldots\). The potential is
 * updated from iteration to iteration, until the full representation of the integer numbers is used, and the real
 * shortest paths and distances are computed. Let \(N\) be the absolute value of the minimum negative number. The
 * algorithm perform \(O(\log N)\) iteration, and each iteration is performed in time \(O(m \sqrt{n})\) time. In total,
 * the running time is \(O(m \sqrt{n} \log N)\).
 *
 * <p>
 * This algorithm is great in practice, and should be used for weights function with integer negative values.
 *
 * <p>
 * Based on 'Scaling algorithms for the shortest paths problem' by Goldberg, A.V. (1995).
 *
 * @author Barak Ugav
 */
public class ShortestPathSingleSourceGoldberg extends ShortestPathSingleSourceAbstract {

	private ShortestPathSingleSource positiveSsspAlgo = ShortestPathSingleSource.newInstance();
	private final ShortestPathSingleSourceDial ssspDial = new ShortestPathSingleSourceDial();
	private final ShortestPathSingleSource dagSssp = ShortestPathSingleSource.builder().dag(true).build();
	private final StronglyConnectedComponentsAlgo ccAlg = StronglyConnectedComponentsAlgo.newInstance();

	private final Diagnostics diagnostics = new Diagnostics();

	/**
	 * Create a Goldberg's SSSP algorithm for integer weights, with negative weights.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathSingleSource#newInstance()} to get a default implementation for the
	 * {@link ShortestPathSingleSource} interface, or {@link ShortestPathSingleSource#builder()} for more customization
	 * options.
	 */
	public ShortestPathSingleSourceGoldberg() {}

	/**
	 * Set the algorithm used for positive weights graphs.
	 *
	 * <p>
	 * The algorithm first calculate a potential for each vertex and construct an equivalent positive weight function
	 * which is used by an SSSP algorithm for positive weights to compute the final shortest paths.
	 *
	 * @param algo a SSSP implementation for graphs with positive weight function
	 */
	void setPositiveSsspAlgo(ShortestPathSingleSource algo) {
		positiveSsspAlgo = Objects.requireNonNull(algo);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed or the edge weights function is not of type
	 *                                      {@link IWeightFunctionInt}
	 */
	@Override
	protected ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		Assertions.onlyDirected(g);
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);
		if (!WeightFunction.isInteger(w))
			throw new IllegalArgumentException("Only integer weights are supported");
		return computeShortestPaths0(g, (IWeightFunctionInt) w, source);
	}

	private ShortestPathSingleSource.IResult computeShortestPaths0(IndexGraph g, IWeightFunctionInt w, int source) {
		final int minWeight = range(g.edges().size()).map(w::weightInt).min().orElse(Integer.MAX_VALUE);
		if (minWeight >= 0)
			// All weights are positive, use Dijkstra
			return (ShortestPathSingleSource.IResult) positiveSsspAlgo
					.computeShortestPaths(g, w, Integer.valueOf(source));

		/* calculate a potential function (or find a negative cycle) */
		int[] potential = calcPotential(g, w, minWeight);

		/* create a (positive) weight function using the potential */
		IWeightFunctionInt pw = ShortestPathUtils.potentialWeightFunc(g, w, potential);

		/* run positive SSSP */
		ShortestPathSingleSource.IResult dijkstraRes = (ShortestPathSingleSource.IResult) positiveSsspAlgo
				.computeShortestPaths(g, pw, Integer.valueOf(source));
		return createResults(potential, dijkstraRes);
	}

	private int[] calcPotential(IndexGraph g, IWeightFunctionInt w0, int minWeight) {
		diagnostics.runBegin();
		final int n = g.vertices().size();
		final int m = g.edges().size();
		w0 = WeightFunctions.localEdgeWeightFunction(g, w0);
		int[] potential = new int[n];

		for (int e : Graphs.selfEdges(g))
			if (w0.weightInt(e) < 0)
				throw new NegativeCycleException(g,
						IPath.valueOf(g, g.edgeSource(e), g.edgeTarget(e), Fastutil.list(e)));

		Bitmap connected = new Bitmap(n);
		int[] layerSize = new int[n + 1];

		/* updated weight function including the potential */
		int[] w = new int[m];

		/* gNeg is the graph g with only 0,-1 edges */
		IndexGraph gNeg = IndexGraphFactory.directed().expectedVerticesNum(n).newGraph();
		gNeg.addVertices(g.vertices());
		int[] gNegEdgeRefs = new int[m];

		/* G is the graph of strong connected components of gNeg, each vertex is a super vertex of gNeg */
		IndexGraph G = IndexGraphFactory.directed().allowParallelEdges().expectedVerticesNum(n + 2).newGraph();
		/* Two fake vertices used to add 0-edges and (r-i)-edges to all other (super) vertices */

		/*
		 * In sparse (random) graphs, the running time seems very slow, as the algorithm require a lot of iterations to
		 * find the potential values, and most of the time is spent in the long-path flow. In these cases, we prefer the
		 * big-layer flow.
		 */
		final double density = (double) g.edges().size() / n * (n + 1) / 2;
		final double alpha = Math.max(0.25, Math.min(3 / -Math.log(density), 2));

		/* Run log(-minWeight) scaling iterations */
		final int minWeightWordsize = log2(-minWeight);
		for (int weightMask = minWeightWordsize; weightMask >= 0; weightMask--) {
			if (weightMask != minWeightWordsize)
				for (int v : range(n))
					potential[v] *= 2;
			diagnostics.scalingIteration();

			/* updated potential function until there are no more negative vertices with current weight function */
			/* we do at most \sqrt{n} such iterations */
			for (;;) {
				diagnostics.potentialIteration();
				/* update current weight function according to latest potential */
				for (int e : range(m))
					w[e] = calcWeightWithPotential(g, e, w0, potential, weightMask);

				/* populate gNeg with all 0,-1 edges */
				gNeg.clearEdges();
				for (int e : range(m)) {
					if (w[e] <= 0) {
						int u = g.edgeSource(e), v = g.edgeTarget(e);
						if (u != v)
							gNegEdgeRefs[gNeg.addEdge(u, v)] = e;
					}
				}

				/* Find all strong connected components in the graph */
				IVertexPartition connectivityRes = (IVertexPartition) ccAlg.findStronglyConnectedComponents(gNeg);
				final int N = connectivityRes.numberOfBlocks();

				/*
				 * Contract each strong connected component and search for a negative edge within it, if found -
				 * negative cycle found
				 */
				G.clear();
				IWeightsInt GWeights = G.addEdgesWeights("weights", int.class, Integer.valueOf(-1));
				G.addVertices(range(N));
				for (int u : range(n)) {
					int U = connectivityRes.vertexBlock(u);
					for (IEdgeIter eit = gNeg.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						int V = connectivityRes.vertexBlock(v);
						int weight = w[gNegEdgeRefs[e]];
						if (U != V) {
							GWeights.set(G.addEdge(U, V), weight);

						} else if (weight < 0) {
							// negative cycle
							IPath negCycle0 = IPath.findPath(gNeg, v, u);
							IntList negCycle = new IntArrayList(negCycle0.edges().size() + 1);
							for (int e2 : negCycle0.edges())
								negCycle.add(gNegEdgeRefs[e2]);
							negCycle.add(gNegEdgeRefs[e]);
							throw new NegativeCycleException(g, IPath.valueOf(g, v, v, negCycle));
						}
					}
				}

				// Create a fake vertex S, connect with 0 edges to all and calc distances
				int fakeS1 = G.addVertexInt();
				for (int U : range(N))
					GWeights.set(G.addEdge(fakeS1, U), 0);
				ShortestPathSingleSource.IResult ssspRes = (ShortestPathSingleSource.IResult) dagSssp
						.computeShortestPaths(G, GWeights, Integer.valueOf(fakeS1));

				// Divide super vertices into layers by distance
				int layerNum = 0;
				int vertexInMaxLayer = -1;
				Arrays.fill(layerSize, 0, N, 0);
				for (int V : range(N)) {
					int l = -(int) ssspRes.distance(V);
					if (l + 1 > layerNum) {
						layerNum = l + 1;
						vertexInMaxLayer = V;
					}
					layerSize[l]++;
				}
				if (layerNum == 1)
					break; // no negative vertices, done

				// Find biggest layer
				int biggestLayer = -1;
				for (int l = layerNum - 1; l > 0; l--)
					if (biggestLayer < 0 || layerSize[l] > layerSize[biggestLayer])
						biggestLayer = l;
				if (layerSize[biggestLayer] >= Math.sqrt(N) * alpha) {
					diagnostics.bigLayer();
					// A layer with sqrt(|V|) was found, decrease potential of layers l,l+1,l+2,...
					for (int v : range(n)) {
						int V = connectivityRes.vertexBlock(v), l = -(int) ssspRes.distance(V);
						if (l >= biggestLayer)
							potential[v]--;
					}
				} else {
					diagnostics.longPath();
					/*
					 * No big layer is found, use path which has at least sqrt(|V|) vertices. Connect a fake vertex to
					 * all vertices, with edge r-i to negative vertex v_i on the path and with edge r to all other
					 * vertices
					 */
					int fakeS2 = G.addVertexInt();
					connected.clear();
					int assignedWeight = layerNum - 2;
					for (IEdgeIter it = ssspRes.getPath(vertexInMaxLayer).edgeIter(); it.hasNext();) {
						int e = it.nextInt();
						int ew = GWeights.weightInt(e);
						if (ew < 0) {
							int V = it.targetInt();
							GWeights.set(G.addEdge(fakeS2, V), assignedWeight--);
							connected.set(V);
						}
					}
					for (int V : range(N))
						if (!connected.get(V))
							GWeights.set(G.addEdge(fakeS2, V), layerNum - 1);

					// Add the remaining edges to the graph, not only 0,-1 edges
					for (int e : range(m)) {
						int weight = w[e];
						if (weight > 0) {
							int U = connectivityRes.vertexBlock(g.edgeSource(e));
							int V = connectivityRes.vertexBlock(g.edgeTarget(e));
							if (U != V)
								GWeights.set(G.addEdge(U, V), weight);
						}
					}

					// Calc distance with abs weight function to update potential function
					for (int weight, mG = G.edges().size(), e = 0; e < mG; e++)
						if ((weight = GWeights.get(e)) < 0)
							GWeights.set(e, -weight);
					ssspRes = ssspDial.computeShortestPaths(G, GWeights, fakeS2, layerNum);
					for (int v : range(n))
						potential[v] += ssspRes.distance(connectivityRes.vertexBlock(v));
				}
			}
		}

		return potential;
	}

	private static int calcWeightWithPotential(IndexGraph g, int e, IWeightFunctionInt w, int[] potential,
			int weightMask) {
		int weight = w.weightInt(e);
		// weight = ceil(weight / 2^weightMask)
		if (weightMask != 0) {
			if (weight <= 0) {
				weight = -((-weight) >> weightMask);
			} else {
				weight += 1 << (weightMask - 1);
				weight = weight >> weightMask;
				if (weight == 0)
					weight = 1;
			}
		}
		return weight + potential[g.edgeSource(e)] - potential[g.edgeTarget(e)];
	}

	private static ShortestPathSingleSource.IResult createResults(int[] potential,
			ShortestPathSingleSource.IResult dijkstraRes) {
		final IndexGraph g = (IndexGraph) dijkstraRes.graph();
		final int n = g.vertices().size();
		final int source = dijkstraRes.sourceInt();
		int sourcePotential = potential[source];
		double[] distances;
		int[] backtrack;
		if (dijkstraRes instanceof IndexResult) {
			IndexResult res = (IndexResult) dijkstraRes;
			distances = res.distances;
			backtrack = res.backtrack;
			for (int v : range(n))
				distances[v] += potential[v] - sourcePotential;

		} else {
			distances = new double[n];
			backtrack = new int[n];
			for (int v : range(n)) {
				distances[v] = dijkstraRes.distance(v) + potential[v] - sourcePotential;
				backtrack[v] = dijkstraRes.backtrackEdge(v);
			}
		}
		return new IndexResult(g, source, distances, backtrack);
	}

	Object getDiagnostic(String key) {
		return Long.valueOf(diagnostics.get(key));
	}

	private static class Diagnostics {

		private static final boolean Enable = false;

		private long runCount;
		private long scalingIterations;
		private long potentialIterations;
		private long bigLayer;
		private long longPath;

		void runBegin() {
			if (Enable)
				runCount++;
		}

		void scalingIteration() {
			if (Enable)
				scalingIterations++;
		}

		void potentialIteration() {
			if (Enable)
				potentialIterations++;
		}

		void bigLayer() {
			if (Enable)
				bigLayer++;
		}

		void longPath() {
			if (Enable)
				longPath++;
		}

		long get(String key) {
			switch (key) {
				case "runCount":
					return runCount;
				case "scalingIterations":
					return scalingIterations;
				case "potentialIterations":
					return potentialIterations;
				case "bigLayer":
					return bigLayer;
				case "longPath":
					return longPath;
				default:
					throw new IllegalArgumentException("unknown diagnostic key: " + key);
			}
		}

	}

}
