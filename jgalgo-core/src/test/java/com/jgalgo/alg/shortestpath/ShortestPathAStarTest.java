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

import java.util.List;
import java.util.Random;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithNoHeuristic(seedGen.nextSeed()), true, seedGen.nextSeed(),
						SsspPhases);
	}

	@Test
	public void testSsspUndirectedNoHeuristic() {
		final long seed = 0x97997bc1c8243730L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithNoHeuristic(seedGen.nextSeed()), false, seedGen.nextSeed(),
						SsspPhases);
	}

	@Test
	public void testRandGraphDirectedPerfectHeuristic() {
		final long seed = 0xf84561a561971620L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithPerfectHeuristic(seedGen.nextSeed()), true, seedGen.nextSeed(),
						SsspPhases);
	}

	@Test
	public void testSsspUndirectedPerfectHeuristic() {
		final long seed = 0xf33456751c101f3bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithPerfectHeuristic(seedGen.nextSeed()), false, seedGen.nextSeed(),
						SsspPhases);
	}

	@Test
	public void testRandGraphDirectedRandAdmissibleHeuristic() {
		final long seed = 0xb5366e9088af7540L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithRandAdmissibleHeuristic(seedGen.nextSeed()), true,
						seedGen.nextSeed(), SsspPhases);
	}

	@Test
	public void testSsspUndirectedRandAdmissibleHeuristic() {
		final long seed = 0x7a8fb412a411ca7bL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		ShortestPathSingleSourceTestUtils
				.testSsspPositiveInt(AStarAsSsspWithRandAdmissibleHeuristic(seedGen.nextSeed()), false,
						seedGen.nextSeed(), SsspPhases);
	}

	private static ShortestPathSingleSource AStarAsSsspWithNoHeuristic(long seed) {
		return AStarAsSssp(new HeuristicBuilder() {
			@Override
			public <V, E> ToDoubleFunction<V> buildHeuristic(HeuristicParams<V, E> params) {
				return v -> 0;
			}
		}, seed);
	}

	private static ShortestPathSingleSource AStarAsSsspWithPerfectHeuristic(long seed) {
		return AStarAsSssp(new HeuristicBuilder() {
			@Override
			public <V, E> ToDoubleFunction<V> buildHeuristic(HeuristicParams<V, E> params) {
				Graph<V, E> g = params.g;
				WeightFunction<E> w = params.w;
				if (params.g.isDirected())
					g = g.reverseView();
				ShortestPathSingleSource.Result<V, E> ssspRes =
						new ShortestPathSingleSourceDijkstra().computeShortestPaths(g, w, params.target);
				return v -> ssspRes.distance(v);
			}
		}, seed);
	}

	private static ShortestPathSingleSource AStarAsSsspWithRandAdmissibleHeuristic(long seed) {
		Random rand = new Random(seed);
		return AStarAsSssp(new HeuristicBuilder() {
			@Override
			public <V, E> ToDoubleFunction<V> buildHeuristic(HeuristicParams<V, E> params) {
				Graph<V, E> g = params.g;
				WeightFunction<E> w = params.w;
				if (params.g.isDirected())
					g = g.reverseView();

				Object2DoubleMap<E> w0 = new Object2DoubleOpenHashMap<>(g.edges().size());
				for (E e : g.edges())
					w0.put(e, w.weight(e) * rand.nextDouble());

				WeightFunction<E> w1 = e -> w0.getDouble(e);
				ShortestPathSingleSource.Result<V, E> ssspRes =
						new ShortestPathSingleSourceDijkstra().computeShortestPaths(g, w1, params.target);
				return v -> ssspRes.distance(v);
			}
		}, rand.nextLong());
	}

	private static class HeuristicParams<V, E> {
		final Graph<V, E> g;
		final WeightFunction<E> w;
		@SuppressWarnings("unused")
		final V source, target;

		HeuristicParams(Graph<V, E> g, WeightFunction<E> w, V source, V target) {
			this.g = g;
			this.w = w;
			this.source = source;
			this.target = target;
		}
	}

	@FunctionalInterface
	private static interface HeuristicBuilder {

		<V, E> ToDoubleFunction<V> buildHeuristic(HeuristicParams<V, E> params);

	}

	private static ShortestPathSingleSource AStarAsSssp(HeuristicBuilder vHeuristicBuilder, long seed) {
		Random rand = new Random(seed);
		return new ShortestPathSingleSource() {
			@SuppressWarnings("unchecked")
			@Override
			public <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
					V source) {
				final int n = g.vertices().size();
				Object2ObjectMap<V, Path<V, E>> paths = new Object2ObjectOpenHashMap<>(n);
				Object2DoubleMap<V> distances = new Object2DoubleOpenHashMap<>(n);
				distances.defaultReturnValue(Double.POSITIVE_INFINITY);

				ShortestPathAStar aStar = new ShortestPathAStar();
				for (V target : g.vertices()) {
					ToDoubleFunction<V> vHeuristic =
							vHeuristicBuilder.buildHeuristic(new HeuristicParams<>(g, w, source, target));
					Path<V, E> path;
					if (g instanceof IntGraph && rand.nextBoolean()) {
						IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
						int source0 = ((Integer) source).intValue();
						int target0 = ((Integer) target).intValue();
						IntToDoubleFunction vHeuristicInt = v -> vHeuristic.applyAsDouble((V) Integer.valueOf(v));
						path = (Path<V, E>) aStar
								.computeShortestPath((IntGraph) g, w0, source0, target0, vHeuristicInt);
					} else {
						path = aStar.computeShortestPath(g, w, source, target, vHeuristic);
					}
					if (path != null) {
						paths.put(target, path);
						distances.put(target, w.weightSum(path.edges()));
					}
				}

				return new ShortestPathSingleSource.Result<>() {

					@Override
					public double distance(V target) {
						if (!g.vertices().contains(target))
							throw NoSuchVertexException.ofVertex(target);
						return distances.getDouble(target);
					}

					@Override
					public Path<V, E> getPath(V target) {
						if (!g.vertices().contains(target))
							throw NoSuchVertexException.ofVertex(target);
						return paths.get(target);
					}

					@Override
					public E backtrackEdge(V target) {
						Path<V, E> path = getPath(target);
						if (path != null) {
							List<E> edges = path.edges();
							if (!edges.isEmpty())
								return edges.get(edges.size() - 1);
						}
						return null;
					}

					@Override
					public V source() {
						return source;
					}

					@Override
					public Graph<V, E> graph() {
						return g;
					}
				};
			}
		};
	}

}
