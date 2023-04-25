package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntSupplier;

import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;

class CyclesFinderTestUtils extends TestUtils {

	static void testSimpleGraph(CyclesFinder cyclesFinder) {
		DiGraph g = new GraphArrayDirected(16);
		int e0 = g.addEdge(0, 1);
		int e1 = g.addEdge(1, 2);
		int e2 = g.addEdge(2, 1);
		int e3 = g.addEdge(2, 0);

		List<Path> actual = cyclesFinder.findAllCycles(g);

		Path c1 = new Path(g, 0, 0, IntList.of(e0, e1, e3));
		Path c2 = new Path(g, 1, 1, IntList.of(e1, e2));
		List<Path> expected = List.of(c1, c2);

		assertEquals(transformCyclesToCanonical(expected), transformCyclesToCanonical(actual));
	}

	static void testRandGraphs(CyclesFinder cyclesFinder, long seed) {

		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16, 8), phase(256, 16, 16), phase(128, 32, 32), phase(128, 32, 64),
				phase(64, 64, 64));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			DiGraph g = (DiGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();
			testGraph(g, cyclesFinder);
		});
	}

	private static void testGraph(DiGraph g, CyclesFinder cyclesFinder) {
		CyclesFinder validationAlgo = cyclesFinder instanceof CyclesFinderTarjan ? new CyclesFinderJohnson()
				: new CyclesFinderTarjan();
		List<Path> actual = cyclesFinder.findAllCycles(g);
		List<Path> expected = validationAlgo.findAllCycles(g);
		assertEquals(transformCyclesToCanonical(expected), transformCyclesToCanonical(actual), g.toString());
	}

	private static Set<IntList> transformCyclesToCanonical(List<Path> cycles) {
		Set<IntList> cycles0 = new TreeSet<>();
		for (Path cycle : cycles) {
			IntArrayList cycle0 = new IntArrayList(cycle);
			transformCycleToCanonical(cycle0);
			cycles0.add(cycle0);
		}
		if (cycles0.size() != cycles.size())
			throw new IllegalArgumentException("cycles list contains duplications");
		return cycles0;
	}

	private static void transformCycleToCanonical(IntArrayList c) {
		final int s = c.size();
		IntSupplier findMinIdx = () -> {
			int minIdx = -1, min = Integer.MAX_VALUE;
			for (int i = 0; i < s; i++) {
				int elm = c.getInt(i);
				if (minIdx == -1 || min > elm) {
					minIdx = i;
					min = elm;
				}
			}
			return minIdx;
		};

		/* reverse */
		int minIdx = findMinIdx.getAsInt();
		int next = c.getInt((minIdx + 1) % s);
		int prev = c.getInt((minIdx - 1 + s) % s);
		if (next > prev) {
			IntArrays.reverse(c.elements(), 0, s);
			minIdx = s - minIdx - 1;
			assert minIdx == findMinIdx.getAsInt();
		}

		/* rotate */
		rotate(c, minIdx);
	}

	private static void rotate(IntList l, int idx) {
		if (l.isEmpty() || idx == 0)
			return;
		int s = l.size();
		int[] temp = l.toIntArray();
		for (int i = 0; i < s; i++)
			l.set(i, temp[(i + idx) % s]);
	}

}
