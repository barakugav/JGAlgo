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

package com.jgalgo;

import java.util.Objects;

/**
 * Naive lookup table for RMQ queries.
 * <p>
 * For a sequence of size \(n\), a lookup table of size \(O(n^2)\) will be constructed, such that for each two indices
 * \(i,j\) the index of the minimum element in the range \([i,j]\) can be simply accessed from the table in \(O(1)\)
 * time.
 * <p>
 * This algorithm require \(O(n^2)\) pre processing time and space, and answer queries in \(O(1)\).
 *
 * @author Barak Ugav
 */
public class RMQStaticLookupTable implements RMQStatic {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	public RMQStaticLookupTable() {}

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid length: " + n);
		Objects.requireNonNull(c);

		if (n <= DS8.LIMIT)
			return new DS8(c, (byte) n);
		else if (n <= DS128.LIMIT)
			return new DS128(c, (short) n);
		else if (n <= DS32768.LIMIT)
			return new DS32768(c, n);
		else
			throw new IllegalArgumentException("N too big (" + n + ")");
	}

	private static int arrSize(int n) {
		return n * (n - 1) / 2;
	}

	private static int indexOf(int n, int i, int j) {
		return (2 * n - i - 1) * i / 2 + j - i - 1;
	}

	private static class DS8 implements RMQStatic.DataStructure {

		private final byte n;
		private final byte[] arr;
		private static final int LIMIT = 1 << ((Byte.SIZE - 1) / 2);

		DS8(RMQStaticComparator c, byte n) {
			arr = new byte[arrSize(n)];
			this.n = n;

			for (byte i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (byte) (i + 1);
			for (byte i = 0; i < n - 2; i++) {
				for (byte j = (byte) (i + 2); j < n; j++) {
					byte m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (!(0 <= i && i <= j && j < n))
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}
	}

	private static class DS128 implements RMQStatic.DataStructure {

		private final short[] arr;
		private final short n;

		private static final int LIMIT = 1 << ((Short.SIZE - 1) / 2);

		DS128(RMQStaticComparator c, short n) {
			arr = new short[arrSize(n)];
			this.n = n;

			for (short i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : (short) (i + 1);
			for (short i = 0; i < n - 2; i++) {
				for (short j = (short) (i + 2); j < n; j++) {
					short m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (!(0 <= i && i <= j && j < n))
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}
	}

	private static class DS32768 implements RMQStatic.DataStructure {

		private final int[] arr;
		private final int n;

		private static final int LIMIT = 1 << ((Integer.SIZE - 1) / 2);

		DS32768(RMQStaticComparator c, int n) {
			arr = new int[arrSize(n)];
			this.n = n;

			for (int i = 0; i < n - 1; i++)
				arr[indexOf(n, i, i + 1)] = c.compare(i, i + 1) < 0 ? i : i + 1;
			for (int i = 0; i < n - 2; i++) {
				for (int j = i + 2; j < n; j++) {
					int m = arr[indexOf(n, i, j - 1)];
					arr[indexOf(n, i, j)] = c.compare(m, j) < 0 ? m : j;
				}
			}
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (!(0 <= i && i <= j && j < n))
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i == j)
				return i;
			return arr[indexOf(n, i, j)];
		}
	}

}
