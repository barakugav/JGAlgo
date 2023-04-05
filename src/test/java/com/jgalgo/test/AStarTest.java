package com.jgalgo.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.jgalgo.AStar;
import com.jgalgo.DiGraph;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.GraphArrayDirected;
import com.jgalgo.Path;
import com.jgalgo.SSSP;
import com.jgalgo.SSSPDijkstra;
import com.jgalgo.Weights;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class AStarTest extends TestUtils {

	private static List<Phase> SsspPhases = List.of(phase(128, 16, 32), phase(64, 64, 256), phase(8, 512, 1024));

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

	private static Supplier<? extends SSSP> AStarAsSSSPWithNoHeuristic() {
		return AStarAsSSSP(params -> (v -> 0));
	}

	private static Supplier<? extends SSSP> AStarAsSSSPWithPerfectHeuristic() {
		return AStarAsSSSP(params -> {
			Graph g = params.g;
			EdgeWeightFunc w = params.w;
			if (params.g instanceof DiGraph) {
				GraphReverseResult rev = reverseGraph((DiGraph) params.g, params.w);
				g = rev.g;
				w = rev.w;
			}

			SSSP.Result ssspRes = new SSSPDijkstra().calcDistances(g, w, params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static Supplier<? extends SSSP> AStarAsSSSPWithRandAdmissibleHeuristic(long seed) {
		Random rand = new Random(seed);
		return AStarAsSSSP(params -> {
			Graph g = params.g;
			EdgeWeightFunc w = params.w;
			if (params.g instanceof DiGraph) {
				GraphReverseResult rev = reverseGraph((DiGraph) params.g, params.w);
				g = rev.g;
				w = rev.w;
			}
			Int2DoubleMap w0 = new Int2DoubleOpenHashMap(g.edges().size());
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				w0.put(e, w.weight(e) * rand.nextDouble());
			}

			SSSP.Result ssspRes = new SSSPDijkstra().calcDistances(g, e -> w0.get(e), params.target);
			return v -> ssspRes.distance(v);
		});
	}

	private static GraphReverseResult reverseGraph(DiGraph g, EdgeWeightFunc w) {
		int n = g.vertices().size();
		DiGraph revG = new GraphArrayDirected(n);
		Weights.Double revW = revG.addEdgesWeight("w").ofDoubles();
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
		DiGraph g;
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

	private static Supplier<? extends SSSP> AStarAsSSSP(
			Function<HeuristicParams, IntToDoubleFunction> vHeuristicBuilder) {
		return () -> new SSSP() {
			@Override
			public SSSP.Result calcDistances(Graph g, EdgeWeightFunc w, int source) {
				int n = g.vertices().size();
				Path[] paths = new Path[n];
				double[] distances = new double[n];
				Arrays.fill(distances, Double.POSITIVE_INFINITY);

				AStar astart = new AStar();
				for (int target = 0; target < n; target++) {
					IntToDoubleFunction vHeuristic = vHeuristicBuilder.apply(new HeuristicParams(g, w, source, target));
					Path path = astart.calcPath(g, w, source, target, vHeuristic);
					if (path != null) {
						paths[target] = path;
						distances[target] = SSSPTestUtils.getPathWeight(path, w);
					}
				}

				return new SSSP.Result() {

					@Override
					public double distance(int v) {
						return distances[v];
					}

					@Override
					public Path getPathTo(int v) {
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
