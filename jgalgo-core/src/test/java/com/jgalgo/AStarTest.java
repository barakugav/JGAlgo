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

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class AStarTest extends TestBase {

	private static List<Phase> SsspPhases = List.of(phase(64, 16, 32), phase(32, 64, 256), phase(4, 300, 900));

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
			Graph g = params.g;
			WeightFunction w = params.w;
			if (params.g.getCapabilities().directed())
				g = g.reverseView();
			ShortestPathSingleSource.Result ssspRes =
					new ShortestPathSingleSourceDijkstra().computeShortestPaths(g, w, params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static ShortestPathSingleSource AStarAsSSSPWithRandAdmissibleHeuristic(long seed) {
		Random rand = new Random(seed);
		return AStarAsSSSP(params -> {
			Graph g = params.g;
			WeightFunction w = params.w;
			if (params.g.getCapabilities().directed())
				g = g.reverseView();

			Int2DoubleMap w0 = new Int2DoubleOpenHashMap(g.edges().size());
			for (int e : g.edges())
				w0.put(e, w.weight(e) * rand.nextDouble());

			ShortestPathSingleSource.Result ssspRes =
					new ShortestPathSingleSourceDijkstra().computeShortestPaths(g, e -> w0.get(e), params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static class HeuristicParams {
		final Graph g;
		final WeightFunction w;
		@SuppressWarnings("unused")
		final int source, target;

		HeuristicParams(Graph g, WeightFunction w, int source, int target) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
		}
	}

	private static ShortestPathSingleSource AStarAsSSSP(
			Function<HeuristicParams, IntToDoubleFunction> vHeuristicBuilder) {
		return new ShortestPathSingleSource() {
			@Override
			public ShortestPathSingleSource.Result computeShortestPaths(Graph g, WeightFunction w, int source) {
				final int n = g.vertices().size();
				Int2ObjectMap<Path> paths = new Int2ObjectOpenHashMap<>(n);
				Int2DoubleMap distances = new Int2DoubleOpenHashMap(n);
				distances.defaultReturnValue(Double.POSITIVE_INFINITY);

				AStar aStar = new AStar();
				for (int target : g.vertices()) {
					IntToDoubleFunction vHeuristic = vHeuristicBuilder.apply(new HeuristicParams(g, w, source, target));
					Path path = aStar.computeShortestPath(g, w, source, target, vHeuristic);
					if (path != null) {
						paths.put(target, path);
						distances.put(target, path.weight(w));
					}
				}

				return new ShortestPathSingleSource.Result() {

					@Override
					public double distance(int target) {
						return distances.get(target);
					}

					@Override
					public Path getPath(int target) {
						return paths.get(target);
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
