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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class AStarTest extends TestBase {

	private static List<Phase> SsspPhases = List.of(phase(64, 16, 32), phase(32, 64, 256), phase(4, 300, 900));

	@Test
	public void testRandGraphDirectedNoHeuristic() {
		final long seed = 0x4c6096c679a03079L;
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithNoHeuristic(), true, seed, SsspPhases);
	}

	@Test
	public void testSSSPUndirectedNoHeuristic() {
		final long seed = 0x97997bc1c8243730L;
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithNoHeuristic(), false, seed, SsspPhases);
	}

	@Test
	public void testRandGraphDirectedPerfectHeuristic() {
		final long seed = 0xf84561a561971620L;
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithPerfectHeuristic(), true, seed, SsspPhases);
	}

	@Test
	public void testSSSPUndirectedPerfectHeuristic() {
		final long seed = 0xf33456751c101f3bL;
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithPerfectHeuristic(), false, seed, SsspPhases);
	}

	@Test
	public void testRandGraphDirectedRandAdmissibleHeuristic() {
		final long seed = 0xb5366e9088af7540L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithRandAdmissibleHeuristic(seedGen.nextSeed()), true,
				seedGen.nextSeed(), SsspPhases);
	}

	@Test
	public void testSSSPUndirectedRandAdmissibleHeuristic() {
		final long seed = 0x7a8fb412a411ca7bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		SSSPTestUtils.testSSSPPositiveInt(AStarAsSSSPWithRandAdmissibleHeuristic(seedGen.nextSeed()), false,
				seedGen.nextSeed(), SsspPhases);
	}

	private static SSSP AStarAsSSSPWithNoHeuristic() {
		return AStarAsSSSP(params -> (v -> 0));
	}

	private static SSSP AStarAsSSSPWithPerfectHeuristic() {
		return AStarAsSSSP(params -> {
			Graph g = params.g;
			EdgeWeightFunc w = params.w;
			if (params.g.getCapabilities().directed()) {
				GraphReverseResult rev = reverseGraph(params.g, params.w);
				g = rev.g;
				w = rev.w;
			}

			SSSP.Result ssspRes = new SSSPDijkstra().computeShortestPaths(g, w, params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static SSSP AStarAsSSSPWithRandAdmissibleHeuristic(long seed) {
		Random rand = new Random(seed);
		return AStarAsSSSP(params -> {
			Graph g = params.g;
			EdgeWeightFunc w = params.w;
			if (params.g.getCapabilities().directed()) {
				GraphReverseResult rev = reverseGraph(params.g, params.w);
				g = rev.g;
				w = rev.w;
			}
			Int2DoubleMap w0 = new Int2DoubleOpenHashMap(g.edges().size());
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				w0.put(e, w.weight(e) * rand.nextDouble());
			}

			SSSP.Result ssspRes = new SSSPDijkstra().computeShortestPaths(g, e -> w0.get(e), params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static GraphReverseResult reverseGraph(Graph g, EdgeWeightFunc w) {
		int n = g.vertices().size();
		Graph revG = new GraphArrayDirected(n);
		Weights.Double revW = revG.addEdgesWeights("w", double.class);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			revW.set(revG.addEdge(v, u), w.weight(e));
		}
		GraphReverseResult res = new GraphReverseResult();
		res.g = revG;
		res.w = revW;
		return res;
	}

	private static class GraphReverseResult {
		Graph g;
		EdgeWeightFunc w;
	}

	private static class HeuristicParams {
		final Graph g;
		final EdgeWeightFunc w;
		@SuppressWarnings("unused")
		final int source, target;

		HeuristicParams(Graph g, EdgeWeightFunc w, int source, int target) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
		}
	}

	private static SSSP AStarAsSSSP(Function<HeuristicParams, IntToDoubleFunction> vHeuristicBuilder) {
		return new SSSP() {
			@Override
			public SSSP.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
				int n = g.vertices().size();
				Path[] paths = new Path[n];
				double[] distances = new double[n];
				Arrays.fill(distances, Double.POSITIVE_INFINITY);

				AStar astart = new AStar();
				for (int target = 0; target < n; target++) {
					IntToDoubleFunction vHeuristic = vHeuristicBuilder.apply(new HeuristicParams(g, w, source, target));
					Path path = astart.computeShortestPath(g, w, source, target, vHeuristic);
					if (path != null) {
						paths[target] = path;
						distances[target] = path.weight(w);
					}
				}

				return new SSSP.Result() {

					@Override
					public double distance(int v) {
						return distances[v];
					}

					@Override
					public Path getPath(int v) {
						return paths[v];
					}

					@Override
					public boolean foundNegativeCycle() {
						return false;
					}

					@Override
					public Path getNegativeCycle() {
						throw new IllegalStateException();
					}
				};
			}
		};
	}

}
