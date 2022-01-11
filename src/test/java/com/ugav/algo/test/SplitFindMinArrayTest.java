package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.SplitFind;
import com.ugav.algo.SplitFindMin;
import com.ugav.algo.SplitFindMinArray;

public class SplitFindMinArrayTest {

	private static boolean testSplitFind(Supplier<? extends SplitFind> builder) {
		int[][] phases = { { 128, 16, 16 }, { 64, 64, 64 }, { 32, 512, 512 }, { 8, 4096, 4096 }, { 2, 16384, 16384 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			return testSplitFind(builder, n, m);
		});
	}

	private static boolean testSplitFind(Supplier<? extends SplitFind> builder, int n, int m) {
		Random rand = new Random(TestUtils.nextRandSeed());
		SplitFind sf = builder.get();

		sf.init(n);

		int[] sequence = new int[n];
		for (int x = 0; x < n; x++)
			sequence[x] = 0;
		int sequencesNum = 1;

		final int OP_FIND = 0;
		final int OP_SPLIT = 1;
		final int OPS_NUM = 2;

		int x;
		while (m-- > 0) {
			switch (rand.nextInt(OPS_NUM)) {
			case OP_FIND:
				x = rand.nextInt(n);
				int expected = sequence[x];
				int actual = sequence[sf.find(x)];
				if (actual != expected) {
					TestUtils.printTestStr("find failed! " + actual + " != " + expected + "\n");
					return false;
				}
				break;
			case OP_SPLIT:
				x = rand.nextInt(n);
				sf.split(x);
				int seqOld = sequence[x];
				int seqNew = sequencesNum++;
				for (int i = x; i < n && sequence[i] == seqOld; i++)
					sequence[i] = seqNew;
				break;
			default:
				throw new InternalError();
			}
		}
		return true;
	}

	private static boolean testSplitFindMin(Supplier<? extends SplitFindMin<Double>> builder) {
		int[][] phases = { { 128, 16, 16 }, { 64, 64, 64 }, { 8, 512, 512 }, { 1, 4096, 4096 } };
		return TestUtils.runTestMultiple(phases, args -> {
			int n = args[1];
			int m = args[2];
			return testSplitFindMin(builder, n, m);
		});
	}

	private static boolean testSplitFindMin(Supplier<? extends SplitFindMin<Double>> builder, int n, int m) {
		Random rand = new Random(TestUtils.nextRandSeed());
		SplitFindMin<Double> sf = builder.get();

		List<Double> keys = new ArrayList<>(n);
		for (int i = 0; i < n; i++)
			keys.add(rand.nextDouble() * 100);
		sf.init(keys, null);

		int[] sequence = new int[n];
		for (int x = 0; x < n; x++)
			sequence[x] = 0;
		int sequencesNum = 1;

		final int OP_FIND = 0;
		final int OP_SPLIT = 1;
		final int OP_FINDMIN = 2;
		final int OP_DECREASEKEY = 3;
		final int OPS_NUM = 4;

		int x;
		while (m-- > 0) {
			switch (rand.nextInt(OPS_NUM)) {
			case OP_FIND:
				x = rand.nextInt(n);
				int expected = sequence[x];
				int actual = sequence[sf.find(x)];
				if (actual != expected) {
					TestUtils.printTestStr("find failed! " + actual + " != " + expected + "\n");
					return false;
				}
				break;
			case OP_SPLIT:
				x = rand.nextInt(n);
				sf.split(x);
				int seqOld = sequence[x];
				int seqNew = sequencesNum++;
				for (int i = x; i < n && sequence[i] == seqOld; i++)
					sequence[i] = seqNew;
				break;
			case OP_FINDMIN:
				x = rand.nextInt(n);
				double expectedKey = Double.MAX_VALUE;
				for (int i = x - 1; i >= 0 && sequence[i] == sequence[x]; i--)
					if (sf.getKey(i) < expectedKey)
						expectedKey = sf.getKey(i);
				for (int i = x; i < n && sequence[i] == sequence[x]; i++)
					if (sf.getKey(i) < expectedKey)
						expectedKey = sf.getKey(i);
				double actualKey = sf.getKey(sf.findMin(x));
				if (actualKey != expectedKey) {
					TestUtils.printTestStr("findmin failed! " + actualKey + " != " + expectedKey + "\n");
					return false;
				}
				break;
			case OP_DECREASEKEY:
				x = rand.nextInt(n);
				sf.decreaseKey(x, sf.getKey(x) * rand.nextDouble());
				break;
			default:
				throw new InternalError();
			}
		}
		return true;
	}

	@Test
	public static boolean splitFind() {
		return testSplitFind(SplitFindMinArray::new);
	}

	@Test
	public static boolean splitFindMin() {
		return testSplitFindMin(SplitFindMinArray::new);
	}

}
