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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Johnson's algorithm for all pairs shortest path.
 * <p>
 * Calculate the shortest path between each pair of vertices in a graph in \(O(n m + n^2 \log n)\) time using \(O(n^2)\)
 * space. Negative weights are supported.
 * <p>
 * The algorithm is faster than using {@link ShortestPathSingleSourceBellmanFord} \(n\) times, as it uses
 * {@link ShortestPathSingleSourceBellmanFord} once to compute a potential for each vertex, resulting in an equivalent
 * positive weight function, allowing us to use {@link ShortestPathSingleSourceDijkstra} from each vertex as a source.
 *
 * @author Barak Ugav
 */
class ShortestPathAllPairsJohnson extends ShortestPathAllPairsUtils.AbstractImpl {

	private ShortestPathSingleSource negativeSssp =
			ShortestPathSingleSource.newBuilder().setNegativeWeights(true).build();
	private boolean parallel = JGAlgoConfigImpl.ParallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a new APSP algorithm object.
	 */
	ShortestPathAllPairsJohnson() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	ShortestPathAllPairs.Result computeAllShortestPaths(IndexGraph g, WeightFunction w) {
		return computeSubsetShortestPaths0(g, g.vertices(), w, true);
	}

	@Override
	ShortestPathAllPairs.Result computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			WeightFunction w) {
		return computeSubsetShortestPaths0(g, verticesSubset, w, false);
	}

	private ShortestPathAllPairs.Result computeSubsetShortestPaths0(IndexGraph g, IntCollection verticesSubset,
			WeightFunction w, boolean allVertices) {
		if (w == null)
			w = WeightFunction.CardinalityWeightFunction;
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		final int n = g.vertices().size();

		boolean negWeight = false;
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			if (w.weight(e) < 0) {
				negWeight = true;
				break;
			}
		}

		if (!negWeight) {
			/* No negative weights, no need for potential */
			SuccessRes res = computeAPSPPositive(g, verticesSubset, w, allVertices);
			res.potential = new double[n];
			return res;
		}

		Pair<double[], Path> potential0 = calcPotential(g, w);
		if (potential0.second() != null)
			return new NegCycleRes(potential0.second());
		double[] potential = potential0.first();

		WeightFunction wPotential = JGAlgoUtils.potentialWeightFunc(g, w, potential);
		SuccessRes res = computeAPSPPositive(g, verticesSubset, wPotential, allVertices);
		res.potential = potential;
		return res;
	}

	private SuccessRes computeAPSPPositive(IndexGraph g, IntCollection verticesSubset, WeightFunction w,
			boolean allVertices) {
		final int verticesSubsetSize = verticesSubset.size();
		final ShortestPathSingleSource.Result[] ssspResults = new ShortestPathSingleSource.Result[verticesSubsetSize];
		int[] vToResIdx = ShortestPathAllPairsUtils.vToResIdx(g, allVertices ? null : verticesSubset);

		ForkJoinPool pool = JGAlgoUtils.getPool();
		if (verticesSubsetSize < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			ShortestPathSingleSource sssp = ShortestPathSingleSource.newInstance();
			for (int source : verticesSubset)
				ssspResults[vToResIdx[source]] = sssp.computeShortestPaths(g, w, source);

		} else {
			/* parallel */
			List<RecursiveAction> tasks = new ObjectArrayList<>(verticesSubsetSize);
			ThreadLocal<ShortestPathSingleSource> sssp =
					ThreadLocal.withInitial(() -> ShortestPathSingleSource.newInstance());
			for (int source : verticesSubset) {
				final int source0 = source;
				tasks.add(JGAlgoUtils.recursiveAction(
						() -> ssspResults[vToResIdx[source0]] = sssp.get().computeShortestPaths(g, w, source0)));
			}
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (RecursiveAction task : tasks)
				task.join();
		}

		if (allVertices) {
			return new SuccessRes.AllVertices(ssspResults);
		} else {
			return new SuccessRes.VerticesSubset(ssspResults, vToResIdx);
		}
	}

	private Pair<double[], Path> calcPotential(IndexGraph g, WeightFunction w) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		/* Add fake vertex */
		IndexGraphBuilder refgBuilder =
				g.isDirected() ? IndexGraphBuilder.newDirected() : IndexGraphBuilder.newUndirected();
		refgBuilder.expectedVerticesNum(n + 1);
		refgBuilder.expectedEdgesNum(m + n);
		for (int v = 0; v < n; v++)
			refgBuilder.addVertex();
		final int fakeV = refgBuilder.addVertex();
		for (int e = 0; e < m; e++)
			refgBuilder.addEdge(g.edgeSource(e), g.edgeTarget(e));
		final int fakeEdgesThreshold = refgBuilder.edges().size();
		for (int v = 0; v < n; v++) {
			int e = refgBuilder.addEdge(fakeV, v);
			assert e >= fakeEdgesThreshold;
		}
		IndexGraph refG = refgBuilder.build();

		WeightFunction refW;
		if (w instanceof WeightFunctionInt) {
			WeightFunctionInt wInt = (WeightFunctionInt) w;
			WeightFunctionInt refWInt = e -> e < fakeEdgesThreshold ? wInt.weightInt(e) : 0;
			refW = refWInt;
		} else {
			refW = e -> e < fakeEdgesThreshold ? w.weight(e) : 0;
		}
		ShortestPathSingleSource.Result res = negativeSssp.computeShortestPaths(refG, refW, fakeV);
		if (!res.foundNegativeCycle()) {
			double[] potential = new double[n];
			for (int v = 0; v < n; v++)
				potential[v] = res.distance(v);
			return Pair.of(potential, null);
		} else {
			Path negCycleRef = res.getNegativeCycle();
			IntList negCycle = new IntArrayList(negCycleRef.edges().size());
			for (int e : negCycleRef.edges())
				negCycle.add(e);
			return Pair.of(null, new PathImpl(g, negCycleRef.source(), negCycleRef.target(), negCycle));
		}
	}

	/**
	 * Set the algorithm used for negative weights graphs.
	 * <p>
	 * The algorithm first calculate a potential for each vertex using an SSSP algorithm for negative weights, than
	 * construct an equivalent positive weight function which is used by an SSSP algorithm for positive weights to
	 * compute all shortest paths.
	 *
	 * @param algo a SSSP implementation for graphs with negative weight function
	 */
	void setNegativeSsspAlgo(ShortestPathSingleSource algo) {
		negativeSssp = Objects.requireNonNull(algo);
	}

	private static class NegCycleRes implements ShortestPathAllPairs.Result {

		private final Path negCycle;

		public NegCycleRes(Path negCycle) {
			Objects.requireNonNull(negCycle);
			this.negCycle = negCycle;
		}

		@Override
		public double distance(int source, int target) {
			throw new IllegalStateException("negative cycle found, no shortest path exists");
		}

		@Override
		public Path getPath(int source, int target) {
			throw new IllegalStateException("negative cycle found, no shortest path exists");
		}

		@Override
		public boolean foundNegativeCycle() {
			return true;
		}

		@Override
		public Path getNegativeCycle() {
			return negCycle;
		}

	}

	private static abstract class SuccessRes implements ShortestPathAllPairs.Result {

		final ShortestPathSingleSource.Result[] ssspResults;
		double[] potential;

		SuccessRes(ShortestPathSingleSource.Result[] ssspResults) {
			this.ssspResults = ssspResults;
		}

		@Override
		public boolean foundNegativeCycle() {
			return false;
		}

		@Override
		public Path getNegativeCycle() {
			throw new IllegalStateException("no negative cycle found");
		}

		private static class AllVertices extends SuccessRes {

			AllVertices(ShortestPathSingleSource.Result[] ssspResults) {
				super(ssspResults);
			}

			@Override
			public double distance(int source, int target) {
				return ssspResults[source].distance(target) + potential[target] - potential[source];
			}

			@Override
			public Path getPath(int source, int target) {
				return ssspResults[source].getPath(target);
			}

		}

		private static class VerticesSubset extends SuccessRes {

			final int[] vToResIdx;

			VerticesSubset(ShortestPathSingleSource.Result[] ssspResults, int[] vToResIdx) {
				super(ssspResults);
				this.vToResIdx = vToResIdx;
			}

			@Override
			public double distance(int source, int target) {
				return ssspResults[vToResIdx[source]].distance(target) + potential[target] - potential[source];
			}

			@Override
			public Path getPath(int source, int target) {
				return ssspResults[vToResIdx[source]].getPath(target);
			}

		}

	}

}
