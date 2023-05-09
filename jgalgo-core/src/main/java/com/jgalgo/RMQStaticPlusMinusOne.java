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
 * Static RMQ for sequences for which the different between any pair of consecutive elements is \(\pm 1\).
 * <p>
 * The algorithm divide the sequence into blocks of size \(O(\log n / 2)\), and then create all possible lookup tables
 * of the sub sequences of the smaller blocks. Because the different between any pair of consecutive elements, the
 * number of such blocks is \(O(\sqrt{n})\) and the total space and preprocessing time of all these blocks is \(O(n)\).
 * <p>
 * To answer on queries which does not fall in the same block, the minimum of each block is stored, and
 * {@link RMQStaticPowerOf2Table} is used on the \(O(n / \log n)\) elements, which is linear in total.
 * <p>
 * This algorithm is used for the static implementation of the lowest common ancestor algorithm, see
 * {@link LCAStaticRMQ}.
 * <p>
 * Based on 'Fast Algorithms for Finding Nearest Common Ancestors' by D. Harel, R. Tarjan (1984).
 *
 * @author Barak Ugav
 */
class RMQStaticPlusMinusOne extends RMQStaticLinearAbstract {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	RMQStaticPlusMinusOne() {}

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);
		return new DS(c, n);
	}

	private class DS extends RMQStaticLinearAbstract.DS {

		DS(RMQStaticComparator c, int n) {
			super(c, n);
		}

		@Override
		byte getBlockSize(int n) {
			int s = n <= 1 ? 1 : (int) Math.ceil(Utils.log2((double) n) * 2 / 3);
			/* choose block size of at least 4, as the 2^(5-1) is 16 (small) */
			return (byte) Math.min(Math.max(s, 5), n);
		}

		@Override
		int getBlockKeySize() {
			return blockSize - 1;
		}

		@Override
		int calcBlockKey(int b) {
			RMQStaticComparator c = b < blockNum - 1 ? cmpOrig : cmpPadded;
			int key = 0;

			int base = b * blockSize;
			for (int i = blockSize - 2; i >= 0; i--) {
				key <<= 1;
				if (c.compare(base + i + 1, base + i) < 0)
					key |= 1;
			}

			return key;
		}

		@Override
		byte[] calcDemoBlock(int key) {
			byte[] demoBlock = new byte[blockSize];

			demoBlock[0] = 0;
			for (int i = 1; i < demoBlock.length; i++)
				demoBlock[i] = (byte) (demoBlock[i - 1] + ((key & (1 << (i - 1))) != 0 ? -1 : 1));

			return demoBlock;
		}

		@Override
		int calcRMQInnerBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j);
		}

	}

}
