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

import java.util.Objects;
import com.jgalgo.internal.util.JGAlgoUtils;

/**
 * Static RMQ algorithm using \(O(n \log n)\) space and answering a query in \(O(1)\) time.
 * <p>
 * An array of size {@code [log n][n]} is created, and at each entry {@code [k][i]} the index of the minimum element in
 * range {@code [i, i+k)} is stored. A query can be answered by two access to the array, giving a total query time of
 * \(O(1)\).
 *
 * @author Barak Ugav
 */
class RMQStaticPowerOf2Table implements RMQStatic {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	RMQStaticPowerOf2Table() {}

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException("Invalid length: " + n);
		Objects.requireNonNull(c);
		if (n <= DSu08.LIMIT)
			return new DSu08(c, (byte) n);
		if (n <= DSu16.LIMIT)
			return new DSu16(c, (short) n);
		return new DSu32(c, n);
	}

	private static class DSu32 implements RMQStatic.DataStructure {
		private final int n;
		private final int[][] arr;
		private final RMQStaticComparator c;

		DSu32(RMQStaticComparator c, int n) {
			this.n = n;
			arr = new int[JGAlgoUtils.log2ceil(n + 1) - 1][n - 1];
			this.c = c;

			for (int i = 0; i < n - 1; i++)
				arr[0][i] = c.compare(i, i + 1) < 0 ? i : i + 1;

			for (int k = 1; k < arr.length; k++) {
				int pkSize = 1 << k;
				int kSize = 1 << (k + 1);
				for (int i = 0; i < n - kSize + 1; i++) {
					int idx0 = arr[k - 1][i];
					int idx1 = arr[k - 1][Math.min(i + pkSize, n - pkSize)];
					arr[k][i] = c.compare(idx0, idx1) < 0 ? idx0 : idx1;
				}
			}

		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RMQStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			j++;

			int k = JGAlgoUtils.log2(j - i);
			int kSize = 1 << k;

			int idx0 = arr[k - 1][i];
			int idx1 = arr[k - 1][j - kSize];
			return c.compare(idx0, idx1) < 0 ? idx0 : idx1;
		}

		@Override
		public long sizeInBytes() {
			int len;
			long s = 0;
			s += 4; // n
			s += 8 + ((len = arr.length) == 0 ? 0 : len * (8 + 4 * arr[0].length));
			s += 8; // c
			return s;
		}
	}

	private static class DSu16 implements RMQStatic.DataStructure {
		private final short n;
		private final short[][] arr;
		private final RMQStaticComparator c;
		private static final int LIMIT = (1 << (Short.SIZE - 1)) - 1;

		DSu16(RMQStaticComparator c, short n) {
			this.n = n;
			arr = new short[JGAlgoUtils.log2ceil(n + 1) - 1][n - 1];
			this.c = c;

			for (short i = 0; i < n - 1; i++)
				arr[0][i] = c.compare(i, i + 1) < 0 ? i : (short) (i + 1);

			for (int k = 1; k < arr.length; k++) {
				int pkSize = 1 << k;
				int kSize = 1 << (k + 1);
				for (int i = 0; i < n - kSize + 1; i++) {
					short idx0 = arr[k - 1][i];
					short idx1 = arr[k - 1][Math.min(i + pkSize, n - pkSize)];
					arr[k][i] = c.compare(idx0, idx1) < 0 ? idx0 : idx1;
				}
			}

		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RMQStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			j++;

			int k = JGAlgoUtils.log2(j - i);
			int kSize = 1 << k;

			int idx0 = arr[k - 1][i];
			int idx1 = arr[k - 1][j - kSize];
			return c.compare(idx0, idx1) < 0 ? idx0 : idx1;
		}

		@Override
		public long sizeInBytes() {
			int len;
			long s = 0;
			s += 2; // n
			s += 8 + ((len = arr.length) == 0 ? 0 : len * (8 + 2 * arr[0].length));
			s += 8; // c
			return s;
		}
	}

	private static class DSu08 implements RMQStatic.DataStructure {
		private final byte n;
		private final byte[][] arr;
		private final RMQStaticComparator c;
		private static final int LIMIT = (1 << (Byte.SIZE - 1)) - 1;

		DSu08(RMQStaticComparator c, byte n) {
			this.n = n;
			arr = new byte[JGAlgoUtils.log2ceil(n + 1) - 1][n - 1];
			this.c = c;

			for (byte i = 0; i < n - 1; i++)
				arr[0][i] = c.compare(i, i + 1) < 0 ? i : (byte) (i + 1);

			for (int k = 1; k < arr.length; k++) {
				int pkSize = 1 << k;
				int kSize = 1 << (k + 1);
				for (int i = 0; i < n - kSize + 1; i++) {
					byte idx0 = arr[k - 1][i];
					byte idx1 = arr[k - 1][Math.min(i + pkSize, n - pkSize)];
					arr[k][i] = c.compare(idx0, idx1) < 0 ? idx0 : idx1;
				}
			}

		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RMQStatics.checkIndices(i, j, n);
			if (i == j)
				return i;
			j++;

			int k = JGAlgoUtils.log2(j - i);
			int kSize = 1 << k;

			int idx0 = arr[k - 1][i];
			int idx1 = arr[k - 1][j - kSize];
			return c.compare(idx0, idx1) < 0 ? idx0 : idx1;
		}

		@Override
		public long sizeInBytes() {
			int len;
			long s = 0;
			s += 1; // n
			s += 8 + ((len = arr.length) == 0 ? 0 : len * (8 + 1 * arr[0].length));
			s += 8; // c
			return s;
		}
	}

}
