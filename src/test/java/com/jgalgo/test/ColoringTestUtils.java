package com.jgalgo.test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

import com.jgalgo.Coloring;
import com.jgalgo.UGraph;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ColoringTestUtils extends TestUtils {

	static void testRandGraphs(Supplier<? extends Coloring> coloringAlgoBuilder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16, 8), phase(128, 32, 64), phase(4, 2048, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			UGraph g = (UGraph) new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(false).parallelEdges(true)
					.selfEdges(false).cycles(true).connected(false).build();
			Coloring coloringAlgo = coloringAlgoBuilder.get();
			Coloring.Result coloring = coloringAlgo.computeColoring(g);
			validateColoring(g, coloring);
		});
	}

	static void validateColoring(UGraph g, Coloring.Result coloring) {
		int n = g.vertices().size();
		if (n == 0)
			return;

		IntSet seenColors = new IntOpenHashSet();
		for (int v = 0; v < n; v++)
			seenColors.add(coloring.colorOf(v));
		int[] seenColorsArr = seenColors.toIntArray();
		IntArrays.parallelQuickSort(seenColorsArr);
		int[] seenColorsArrExpected = new int[seenColorsArr.length];
		for (int i = 0; i < seenColorsArrExpected.length; i++)
			seenColorsArrExpected[i] = i;
		assertArrayEquals(seenColorsArrExpected, seenColorsArr, "colors are expected to be 0,1,2,3,...");

		assertEquals(seenColorsArr.length, coloring.colorsNum(), "wrong colors num");

		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int c1 = coloring.colorOf(u);
			int c2 = coloring.colorOf(v);
			assertNotEquals(c1, c2, "neighbor vertices " + u + "," + v + " have the same color: " + c1);
		}
	}

}
