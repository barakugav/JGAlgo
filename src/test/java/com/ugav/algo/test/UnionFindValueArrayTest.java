package com.ugav.algo.test;

import java.util.List;
import java.util.Random;

import com.ugav.algo.UnionFindValue;
import com.ugav.algo.UnionFindValueArray;

public class UnionFindValueArrayTest extends TestUtils {

	@Test
	public static boolean randRegularUFOps() {
		return UnionFindTestUtils.randOps(UnionFindValueArray::new);
	}

	@Test
	public static boolean randOps() {
		List<Phase> phases = List.of(phase(256, 8, 16), phase(64, 64, 256), phase(16, 1024, 2048),
				phase(2, 8096, 16384));
		return runTestMultiple(phases, args -> {
			int n = args[0];
			int m = args[1];
			return randOps(n, m);
		});
	}

	private static boolean randOps(int n, int m) {
		Random rand = new Random(nextRandSeed());

		UnionFindValue uf = new UnionFindValueArray();
		int set[] = new int[n];
		double[] deltas = new double[n];

		for (int x = 0; x < n; x++) {
			double delta = rand.nextDouble() * 100 - 50;
			set[x] = uf.make(delta);
			deltas[x] = delta;
		}

		final int OP_FIND = 0;
		final int OP_UNION = 1;
		final int OP_ADD_DELTA = 2;
		final int OP_GET_DELTASUM = 3;
		final int OP_NUM = 4;

		int x;
		while (m-- > 0) {
			switch (rand.nextInt(OP_NUM)) {
			case OP_FIND:
				x = rand.nextInt(n);
				int actualSet = set[uf.find(x)];
				int expectedSet = set[x];
				if (actualSet != expectedSet) {
					printTestStr("Unexpected find result\n");
					return false;
				}
				break;
			case OP_UNION:
				int a = rand.nextInt(n), b = rand.nextInt(n);
				uf.union(a, b);
				int aset = set[a];
				int bset = set[b];
				for (int i = 0; i < n; i++)
					if (set[i] == bset)
						set[i] = aset;
				break;
			case OP_ADD_DELTA:
				x = rand.nextInt(n);
				double delta = rand.nextDouble() * 100 - 50;
				uf.addValue(x, delta);
				for (int i = 0; i < n; i++)
					if (set[i] == set[x])
						deltas[i] += delta;
				break;
			case OP_GET_DELTASUM:
				x = rand.nextInt(n);
				double actualDelta = uf.getValue(x);
				double expectedDelta = deltas[x];
				if (!doubleEql(actualDelta, expectedDelta, 1E-5)) {
					printTestStr("Unexpected value: " + actualDelta + " != " + expectedDelta + "\n");
					return false;
				}
				break;
			default:
				throw new InternalError();
			}
		}

		return true;
	}

}
