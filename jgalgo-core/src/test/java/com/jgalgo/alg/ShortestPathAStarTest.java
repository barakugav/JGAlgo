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

import java.util.Random;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ShortestPathAStarTest extends TestBase {

	private static PhasedTester SsspPhases;
	static {
		SsspPhases = new PhasedTester();
		SsspPhases.addPhase().withArgs(16, 32).repeat(64);
		SsspPhases.addPhase().withArgs(64, 256).repeat(32);
		SsspPhases.addPhase().withArgs(300, 900).repeat(4);
	}

	@Test
	public void testRandGraphDirectedNoHeuristic() {
		final long seed = 0x4c6096c679a03079L;
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(AStarAsSSSPWithNoHeuristic(), true, seed, SsspPhases);
	}

	@Test
	public void testSSSPUndirectedNoHeuristic() {
		final long seed = 0x97997bc1c8243730L;
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(AStarAsSSSPWithNoHeuristic(), false, seed, SsspPhases);
	}

	@Test
	public void testRandGraphDirectedPerfectHeuristic() {
		final long seed = 0xf84561a561971620L;
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(AStarAsSSSPWithPerfectHeuristic(), true, seed,
				SsspPhases);
	}

	@Test
	public void testSSSPUndirectedPerfectHeuristic() {
		final long seed = 0xf33456751c101f3bL;
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(AStarAsSSSPWithPerfectHeuristic(), false, seed,
				SsspPhases);
	}

	@Test
	public void testRandGraphDirectedRandAdmissibleHeuristic() {
		final long seed = 0xb5366e9088af7540L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(
				AStarAsSSSPWithRandAdmissibleHeuristic(seedGen.nextSeed()), true, seedGen.nextSeed(), SsspPhases);
	}

	@Test
	public void testSSSPUndirectedRandAdmissibleHeuristic() {
		final long seed = 0x7a8fb412a411ca7bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils.testSSSPPositiveInt(
				AStarAsSSSPWithRandAdmissibleHeuristic(seedGen.nextSeed()), false, seedGen.nextSeed(), SsspPhases);
	}

	private static ShortestPathSingleSource AStarAsSSSPWithNoHeuristic() {
		return AStarAsSSSP(params -> (v -> 0));
	}

	private static ShortestPathSingleSource AStarAsSSSPWithPerfectHeuristic() {
		return AStarAsSSSP(params -> {
			IntGraph g = params.g;
			IWeightFunction w = params.w;
			if (params.g.isDirected())
				g = g.reverseView();
			ShortestPathSingleSource.IResult ssspRes =
					(ShortestPathSingleSource.IResult) new ShortestPathSingleSourceDijkstra().computeShortestPaths(g, w,
							Integer.valueOf(params.target));
			return v -> ssspRes.distance(v);
		});
	}

	private static ShortestPathSingleSource AStarAsSSSPWithRandAdmissibleHeuristic(long seed) {
		Random rand = new Random(seed);
		return AStarAsSSSP(params -> {
			IntGraph g = params.g;
			IWeightFunction w = params.w;
			if (params.g.isDirected())
				g = g.reverseView();

			Int2DoubleMap w0 = new Int2DoubleOpenHashMap(g.edges().size());
			for (int e : g.edges())
				w0.put(e, w.weight(e) * rand.nextDouble());

			IWeightFunction w1 = e -> w0.get(e);
			ShortestPathSingleSource.IResult ssspRes =
					(ShortestPathSingleSource.IResult) new ShortestPathSingleSourceDijkstra().computeShortestPaths(g,
							w1, Integer.valueOf(params.target));
			return v -> ssspRes.distance(v);
		});
	}

	private static class HeuristicParams {
		final IntGraph g;
		final IWeightFunction w;
		@SuppressWarnings("unused")
		final int source, target;

		HeuristicParams(IntGraph g, IWeightFunction w, int source, int target) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
		}
	}

	@SuppressWarnings("unchecked")
	private static ShortestPathSingleSource AStarAsSSSP(
			Function<HeuristicParams, IntToDoubleFunction> vHeuristicBuilder) {
		return new ShortestPathSingleSource() {
			@Override
			public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
					V source) {
				if (!(g instanceof IntGraph))
					throw new IllegalArgumentException("non int graphs are not supported");
				IntGraph g0 = (IntGraph) g;
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue();
				final int n = g.vertices().size();
				Int2ObjectMap<IPath> paths = new Int2ObjectOpenHashMap<>(n);
				Int2DoubleMap distances = new Int2DoubleOpenHashMap(n);
				distances.defaultReturnValue(Double.POSITIVE_INFINITY);

				ShortestPathAStar aStar = new ShortestPathAStar();
				for (int target : g0.vertices()) {
					IntToDoubleFunction vHeuristic =
							vHeuristicBuilder.apply(new HeuristicParams(g0, w0, source0, target));
					IPath path = aStar.computeShortestPath(g0, w0, source0, target, vHeuristic);
					if (path != null) {
						paths.put(target, path);
						distances.put(target, w0.weightSum(path.edges()));
					}
				}

				return (ShortestPathSingleSource.Result<V, E>) new ShortestPathSingleSource.IResult() {

					@Override
					public double distance(int target) {
						return distances.get(target);
					}

					@Override
					public IPath getPath(int target) {
						return paths.get(target);
					}

					@Override
					public boolean foundNegativeCycle() {
						return false;
					}

					@Override
					public IPath getNegativeCycle() {
						throw new IllegalStateException("no negative cycle found");
					}
				};
			}
		};
	}

}
