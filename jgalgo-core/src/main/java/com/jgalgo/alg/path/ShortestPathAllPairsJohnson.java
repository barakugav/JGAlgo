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

package com.jgalgo.alg.path;

import static com.jgalgo.internal.util.Range.range;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Johnson's algorithm for all pairs shortest path.
 *
 * <p>
 * Calculate the shortest path between each pair of vertices in a graph in \(O(n m + n^2 \log n)\) time using \(O(n^2)\)
 * space. Negative weights are supported.
 *
 * <p>
 * The algorithm is faster than using {@link ShortestPathSingleSourceBellmanFord} \(n\) times, as it uses
 * {@link ShortestPathSingleSourceBellmanFord} once to compute a potential for each vertex, resulting in an equivalent
 * positive weight function, allowing us to use {@link ShortestPathSingleSourceDijkstra} from each vertex as a source.
 *
 * @author Barak Ugav
 */
public class ShortestPathAllPairsJohnson extends ShortestPathAllPairsAbstract {

	private ShortestPathSingleSource negativeSssp = ShortestPathSingleSource.builder().negativeWeights(true).build();
	private boolean parallel = JGAlgoConfigImpl.ParallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a APSP algorithm.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathAllPairs#newInstance()} to get a default implementation for the
	 * {@link ShortestPathAllPairs} interface, or {@link ShortestPathAllPairs#builder()} for more customization options.
	 */
	public ShortestPathAllPairsJohnson() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	protected ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w) {
		return computeSubsetShortestPaths0(g, g.vertices(), w, true);
	}

	@Override
	protected ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w) {
		return computeSubsetShortestPaths0(g, verticesSubset, w, false);
	}

	private ShortestPathAllPairs.IResult computeSubsetShortestPaths0(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w, boolean allVertices) {
		w = WeightFunctions.localEdgeWeightFunction(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);
		final int n = g.vertices().size();

		boolean negWeight = false;
		for (int e : range(g.edges().size())) {
			if (w.weight(e) < 0) {
				negWeight = true;
				break;
			}
		}

		if (!negWeight) {
			/* No negative weights, no need for potential */
			Res res = computeApspPositive(g, verticesSubset, w, allVertices);
			res.potential = new double[n];
			return res;
		}

		double[] potential = calcPotential(g, w);
		IWeightFunction wPotential = ShortestPathUtils.potentialWeightFunc(g, w, potential);

		Res res = computeApspPositive(g, verticesSubset, wPotential, allVertices);
		res.potential = potential;
		return res;
	}

	private Res computeApspPositive(IndexGraph g, IntCollection verticesSubset, IWeightFunction w,
			boolean allVertices) {
		final int verticesSubsetSize = verticesSubset.size();
		final ShortestPathSingleSource.IResult[] ssspResults = new ShortestPathSingleSource.IResult[verticesSubsetSize];
		int[] vToSubsetIdx = ShortestPathAllPairsAbstract.IndexResultVerticesSubsetFromSssp
				.indexVerticesSubset(g, allVertices ? null : verticesSubset);

		ForkJoinPool pool = JGAlgoUtils.getPool();
		if (verticesSubsetSize < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			ShortestPathSingleSource sssp = ShortestPathSingleSource.newInstance();
			for (int source : verticesSubset)
				ssspResults[vToSubsetIdx[source]] =
						(ShortestPathSingleSource.IResult) sssp.computeShortestPaths(g, w, Integer.valueOf(source));

		} else {
			/* parallel */
			List<RecursiveAction> tasks = new ObjectArrayList<>(verticesSubsetSize);
			ThreadLocal<ShortestPathSingleSource> sssp =
					ThreadLocal.withInitial(() -> ShortestPathSingleSource.newInstance());
			for (int source : verticesSubset) {
				final int source0 = source;
				tasks
						.add(JGAlgoUtils
								.recursiveAction(() -> ssspResults[vToSubsetIdx[source0]] =
										(ShortestPathSingleSource.IResult) sssp
												.get()
												.computeShortestPaths(g, w, Integer.valueOf(source0))));
			}
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (RecursiveAction task : tasks)
				task.join();
		}

		if (allVertices) {
			return new Res.AllVertices(ssspResults);
		} else {
			return new Res.VerticesSubset(ssspResults, vToSubsetIdx);
		}
	}

	private double[] calcPotential(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		/* Add fake vertex */
		IndexGraphBuilder refgBuilder = IndexGraphBuilder.newInstance(g.isDirected());
		refgBuilder.ensureVertexCapacity(n + 1);
		refgBuilder.ensureEdgeCapacity(m + n);
		refgBuilder.addVertices(g.vertices());
		final int fakeV = refgBuilder.addVertexInt();
		refgBuilder.addEdges(EdgeSet.allOf(g));
		final int fakeEdgesThreshold = refgBuilder.edges().size();
		for (int v : range(n)) {
			int e = refgBuilder.addEdge(fakeV, v);
			assert e >= fakeEdgesThreshold;
		}
		IndexGraph refG = refgBuilder.build();

		IWeightFunction refW;
		if (WeightFunction.isInteger(w)) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			IWeightFunctionInt refWInt = e -> e < fakeEdgesThreshold ? wInt.weightInt(e) : 0;
			refW = refWInt;
		} else {
			refW = e -> e < fakeEdgesThreshold ? w.weight(e) : 0;
		}
		ShortestPathSingleSource.IResult res;
		try {
			res = (ShortestPathSingleSource.IResult) negativeSssp
					.computeShortestPaths(refG, refW, Integer.valueOf(fakeV));
		} catch (NegativeCycleException e) {
			IPath p = (IPath) e.cycle();
			throw new NegativeCycleException(g, IPath.valueOf(g, p.sourceInt(), p.targetInt(), p.edges()));
		}

		double[] potential = new double[n];
		for (int v : range(n))
			potential[v] = res.distance(v);
		return potential;
	}

	/**
	 * Set the algorithm used for negative weights graphs.
	 *
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

	private abstract static class Res implements ShortestPathAllPairs.IResult {

		final ShortestPathSingleSource.IResult[] ssspResults;
		double[] potential;

		Res(ShortestPathSingleSource.IResult[] ssspResults) {
			this.ssspResults = ssspResults;
		}

		private static class AllVertices extends Res {

			AllVertices(ShortestPathSingleSource.IResult[] ssspResults) {
				super(ssspResults);
			}

			@Override
			public double distance(int source, int target) {
				Assertions.checkVertex(source, ssspResults.length);
				return ssspResults[source].distance(target) + potential[target] - potential[source];
			}

			@Override
			public IPath getPath(int source, int target) {
				Assertions.checkVertex(source, ssspResults.length);
				return ssspResults[source].getPath(target);
			}

		}

		private static class VerticesSubset extends Res {

			final int[] vToSubsetIdx;

			VerticesSubset(ShortestPathSingleSource.IResult[] ssspResults, int[] vToSubsetIdx) {
				super(ssspResults);
				this.vToSubsetIdx = vToSubsetIdx;
			}

			@Override
			public double distance(int source, int target) {
				int sourceIdx = resultIdx(source);
				resultIdx(target); /* checks that target is in the subset */
				return ssspResults[sourceIdx].distance(target) + potential[target] - potential[source];
			}

			@Override
			public IPath getPath(int source, int target) {
				int sourceIdx = resultIdx(source);
				resultIdx(target); /* checks that target is in the subset */
				return ssspResults[sourceIdx].getPath(target);
			}

			private int resultIdx(int vertex) {
				Assertions.checkVertex(vertex, vToSubsetIdx.length);
				int idx = vToSubsetIdx[vertex];
				if (idx < 0)
					throw new IllegalArgumentException("no results for vertex " + vertex);
				return idx;
			}

		}

	}

}
