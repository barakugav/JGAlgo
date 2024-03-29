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

package com.jgalgo.internal.ds;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class UnionFindValueArrayTest extends TestBase {

	@Test
	public void testRandRegularUFOps() {
		final long seed = 0x8b1924e294905671L;
		UnionFindTestUtils.randOps(UnionFindValueArray::new, seed);
	}

	@Test
	public void testRandOps() {
		final long seed = 0x8c06f8977b257d8cL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(8, 16).repeat(256);
		tester.addPhase().withArgs(64, 256).repeat(64);
		tester.addPhase().withArgs(1024, 2048).repeat(16);
		tester.addPhase().withArgs(8096, 16384).repeat(2);
		tester.run((n, m) -> {
			randOps(n, m, seedGen.nextSeed());
		});
	}

	private static void randOps(int n, int m, long seed) {
		Random rand = new Random(seed);

		UnionFindValue uf = UnionFindValue.newInstance();
		int[] set = new int[n];
		double[] deltas = new double[n];

		for (int x : range(n)) {
			double delta = nextDouble(rand, -50, 50);
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
					assertEquals(expectedSet, actualSet, "Unexpected find result");
					break;
				case OP_UNION:
					int a = rand.nextInt(n), b = rand.nextInt(n);
					uf.union(a, b);
					int aset = set[a];
					int bset = set[b];
					for (int i : range(n))
						if (set[i] == bset)
							set[i] = aset;
					break;
				case OP_ADD_DELTA:
					x = rand.nextInt(n);
					double delta = nextDouble(rand, -50, 50);
					uf.addValue(x, delta);
					for (int i : range(n))
						if (set[i] == set[x])
							deltas[i] += delta;
					break;
				case OP_GET_DELTASUM:
					x = rand.nextInt(n);
					double actualDelta = uf.getValue(x);
					double expectedDelta = deltas[x];
					assertEquals(expectedDelta, actualDelta, 1E-5, "Unexpected value");
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}

}
