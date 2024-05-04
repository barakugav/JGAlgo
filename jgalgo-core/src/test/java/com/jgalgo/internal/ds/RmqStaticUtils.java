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
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Random;
import com.jgalgo.internal.util.TestUtils;

class RmqStaticUtils extends TestUtils {

	private RmqStaticUtils() {}

	static void testRMQ65536(RmqStatic algo, long seed) {
		testRMQ(algo, 65536, 4096, seed);
	}

	static void testRMQ(RmqStatic algo, int n, int queriesNum, long seed) {
		int[] a = new int[n];
		int[][] queries = new int[queriesNum][];
		randRMQDataAndQueries(a, queries, seed);

		testRMQ(algo, a, queries);
	}

	static void testRMQ(RmqStatic rmq, int a[], int[][] queries) {
		RmqStatic.DataStructure rmqDS = rmq.preProcessSequence(RmqStaticComparator.ofIntArray(a), a.length);

		for (int idx : range(queries.length)) {
			int i = queries[idx][0];
			int j = queries[idx][1];
			int expectedIdx = queries[idx][2];
			int expected = a[expectedIdx];
			int actualIdx = rmqDS.findMinimumInRange(i, j);
			int actual = a[actualIdx];

			if (actual != expected) {
				System.err
						.println(" [" + i + "," + j + "] -> expected[" + expectedIdx + "]=" + expected + " actual["
								+ actualIdx + "]=" + actual);
				System.err.println("data size: " + a.length);
				System.err.println("queries num: " + queries.length);
				System.err.println(formatRMQDataAndQueries(a, queries));
				fail();
			}
		}
	}

	static void randRMQDataPlusMinusOne(int a[], long seed) {
		Random rand = new Random(seed);
		a[0] = 0;
		for (int i : range(1, a.length))
			a[i] = a[i - 1] + rand.nextInt(2) * 2 - 1;
	}

	static void randRMQQueries(int a[], int[][] queries, int blockSize, long seed) {
		Random rand = new Random(seed);
		for (int q = 0; q < queries.length;) {
			int i = rand.nextInt(a.length);
			if (i % blockSize == blockSize - 1 && a.length != blockSize)
				continue;
			int blockBase = (i / blockSize) * blockSize;
			int blockEnd = blockBase + blockSize;
			int j = rand.nextInt(blockEnd - i) + i;

			int m = i;
			for (int k = i; k <= j; k++)
				if (a[k] < a[m])
					m = k;
			queries[q++] = new int[] { i, j, m };
		}

	}

	static void randRMQDataAndQueries(int a[], int[][] queries, long seed) {
		randRMQDataAndQueries(a, queries, a.length, seed);
	}

	static void randRMQDataAndQueries(int a[], int[][] queries, int blockSize, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		randArray(a, 0, 64, seedGen.nextSeed());
		randRMQQueries(a, queries, blockSize, seedGen.nextSeed());
	}

	static CharSequence formatRMQDataAndQueries(int a[], int[][] queries) {
		StringBuilder s = new StringBuilder();

		final int dataPerLine = 32;
		final int queriesPerLine = 12;

		if (a.length == 0)
			s.append("{}\n");
		else {
			s.append("{");
			for (int i : range(a.length - 1)) {
				s.append(a[i]);
				s.append(((i + 1) % dataPerLine) == 0 ? ",\n" : ", ");
			}
			s.append(a[a.length - 1]);
			s.append("}\n");
		}

		if (queries.length == 0)
			s.append("{}\n");
		else {
			s.append("{");
			for (int i : range(queries.length - 1)) {
				int[] q = queries[i];
				s.append("{" + q[0] + "," + q[1] + "," + q[2] + "},");
				s.append(((i + 1) % queriesPerLine) == 0 ? "\n" : " ");
			}
			int[] q = queries[queries.length - 1];
			s.append("{" + q[0] + "," + q[1] + "," + q[2] + "}");
			s.append("}\n");
		}

		return s;
	}
}
