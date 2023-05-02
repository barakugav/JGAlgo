package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

class UnionFindTestUtils extends TestUtils {

	static void randOps(Supplier<? extends UnionFind> builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(256, 8, 16), phase(64, 64, 256), phase(16, 1024, 2048), phase(2, 8096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			randOps(builder, n, m, seedGen.nextSeed());
		});
	}

	private static void randOps(Supplier<? extends UnionFind> builder, int n, int m, long seed) {
		Random rand = new Random(seed);

		UnionFind uf = builder.get();
		int[] set = new int[n];

		for (int i = 0; i < n; i++)
			set[i] = uf.make();

		final int OP_FIND = 0;
		final int OP_UNION = 1;
		final int OP_NUM = 2;

		while (m-- > 0) {
			switch (rand.nextInt(OP_NUM)) {
				case OP_FIND:
					int x = rand.nextInt(n);
					int actualSet = set[uf.find(x)];
					int expectedSet = set[x];
					assertEquals(expectedSet, actualSet, "unexpected set");
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
				default:
					throw new IllegalStateException();
			}
		}
	}

}
