package com.jgalgo.test;

import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.RMQ;
import com.jgalgo.RMQ.ArrayIntComparator;

class RMQTestUtils extends TestUtils {

	private RMQTestUtils() {
	}

	static void testRMQ65536(Supplier<? extends RMQ> builder, long seed) {
		testRMQ(builder, 65536, 4096, seed);
	}

	static void testRMQ(Supplier<? extends RMQ> builder, int n, int queriesNum, long seed) {
		int[] a = new int[n];
		int[][] queries = new int[queriesNum][];
		randRMQDataAndQueries(a, queries, seed);

		testRMQ(builder, a, queries);
	}

	static void testRMQ(Supplier<? extends RMQ> builder, int a[], int[][] queries) {
		RMQ rmq = builder.get();
		rmq.preprocessRMQ(new ArrayIntComparator(a), a.length);

		for (int idx = 0; idx < queries.length; idx++) {
			int i = queries[idx][0];
			int j = queries[idx][1];
			int expectedIdx = queries[idx][2];
			int expected = a[expectedIdx];
			int actualIdx = rmq.calcRMQ(i, j);
			int actual = a[actualIdx];

			if (actual != expected) {
				System.err.println(" [" + i + "," + j + "] -> expected[" + expectedIdx + "]=" + expected + " actual["
						+ actualIdx + "]=" + actual);
				System.err.println("data size: " + a.length);
				System.err.println("queries num: " + queries.length);
				System.err.println(formatRMQDataAndQueries(a, queries));
				Assertions.fail();
			}
		}
	}

	static void randRMQDataPlusMinusOne(int a[], long seed) {
		Random rand = new Random(seed);
		a[0] = 0;
		for (int i = 1; i < a.length; i++)
			a[i] = a[i - 1] + rand.nextInt(2) * 2 - 1;
	}

	static void randRMQQueries(int a[], int[][] queries, int blockSize, long seed) {
		Random rand = new Random(seed);
		for (int q = 0; q < queries.length;) {
			int i = rand.nextInt(a.length);
			if (i % blockSize == blockSize - 1)
				continue;
			int blockBase = (i / blockSize) * blockSize;
			int blockEnd = blockBase + blockSize;
			int j = rand.nextInt(blockEnd - i) + i + 1;

			int m = i;
			for (int k = i; k < j; k++)
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
			for (int i = 0; i < a.length - 1; i++) {
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
			for (int i = 0; i < queries.length - 1; i++) {
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
