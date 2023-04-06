package com.jgalgo.test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import com.jgalgo.CyclesJohnson;
import com.jgalgo.DiGraph;
import com.jgalgo.GraphArrayDirected;
import com.jgalgo.Path;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;

public class CyclesJohnsonTest {

	@Test
	public void testSmallGraph() {
		DiGraph g = new GraphArrayDirected(16);
		int e0 = g.addEdge(0, 1);
		int e1 = g.addEdge(1, 2);
		int e2 = g.addEdge(2, 1);
		int e3 = g.addEdge(2, 0);

		Path c1 = new Path(g, 0, 0, IntList.of(e0, e1, e3));
		Path c2 = new Path(g, 1, 1, IntList.of(e1, e2));
		Set<Path> expected = new TreeSet<>(CyclesJohnsonTest::cyclesCmp);
		expected.addAll(List.of(c1, c2));

		Set<Path> actual = new TreeSet<>(CyclesJohnsonTest::cyclesCmp);
		actual.addAll(new CyclesJohnson().findAllCycles(g));
	}

	private static int cyclesCmp(Path c1, Path c2) {
		int c;
		if ((c = Integer.compare(c1.size(), c2.size())) != 0)
			return c;
		if (c1.isEmpty())
			return 0;
		if (c1.size() == 1)
			return Integer.compare(c1.getInt(0), c2.getInt(0));

		IntArrayList l1 = new IntArrayList(c1);
		IntArrayList l2 = new IntArrayList(c2);
		transformCycleToCanocical(l1);
		transformCycleToCanocical(l2);
		return l1.compareTo(l2);
	}

	private static void transformCycleToCanocical(IntArrayList c) {
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
