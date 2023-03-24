package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("boxing")
public class SplitFindMinArrayTest extends TestUtils {

	private static void testSplitFind(Supplier<? extends SplitFind> builder) {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 64), phase(32, 512, 512), phase(8, 4096, 4096),
				phase(2, 16384, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			testSplitFind(builder, n, m);
		});
	}

	private static void testSplitFind(Supplier<? extends SplitFind> builder, int n, int m) {
		Random rand = new Random(nextRandSeed());
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
				Assertions.assertEquals(expected, actual, "find failed!");
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
				throw new IllegalStateException();
			}
		}
	}

	private static void testSplitFindMin(Supplier<? extends SplitFindMin<Double>> builder) {
		List<Phase> phases = List.of(phase(128, 16, 16), phase(64, 64, 64), phase(8, 512, 512), phase(1, 4096, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			testSplitFindMin(builder, n, m);
		});
	}

	private static void testSplitFindMin(Supplier<? extends SplitFindMin<Double>> builder, int n, int m) {
		Random rand = new Random(nextRandSeed());
		SplitFindMin<Double> sf = builder.get();

		List<Double> keys = new ArrayList<>(n);
		for (int i = 0; i < n; i++)
			keys.add(nextDouble(rand, 0, 100));
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
				Assertions.assertEquals(expected, actual, "find failed!");
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
				Assertions.assertEquals(expectedKey, actualKey, "findmin failed!");
				break;
			case OP_DECREASEKEY:
				x = rand.nextInt(n);
				sf.decreaseKey(x, sf.getKey(x) * rand.nextDouble());
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}

	@Test
	public void testSplitFind() {
		testSplitFind(SplitFindMinArray::new);
	}

	@Test
	public void testSplitFindMin() {
		testSplitFindMin(SplitFindMinArray::new);
	}

}
