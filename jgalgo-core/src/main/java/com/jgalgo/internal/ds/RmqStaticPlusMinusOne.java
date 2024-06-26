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

import static com.jgalgo.internal.util.Numbers.log2;
import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import com.jgalgo.alg.tree.LowestCommonAncestorStaticRmq;

/**
 * Static RMQ for sequences for which the different between any pair of consecutive elements is \(\pm 1\).
 *
 * <p>
 * The algorithm divide the sequence into blocks of size \(O(\log n / 2)\), and then create all possible lookup tables
 * of the sub sequences of the smaller blocks. Because the different between any pair of consecutive elements, the
 * number of such blocks is \(O(\sqrt{n})\) and the total space and preprocessing time of all these blocks is \(O(n)\).
 *
 * <p>
 * To answer on queries which does not fall in the same block, the minimum of each block is stored, and
 * {@link RmqStaticPowerOf2Table} is used on the \(O(n / \log n)\) elements, which is linear in total.
 *
 * <p>
 * This algorithm is used for the static implementation of the lowest common ancestor algorithm, see
 * {@link LowestCommonAncestorStaticRmq}.
 *
 * <p>
 * Based on 'Fast Algorithms for Finding Nearest Common Ancestors' by D. Harel, R. Tarjan (1984).
 *
 * @author Barak Ugav
 */
public class RmqStaticPlusMinusOne extends RmqStaticLinearAbstract {

	/**
	 * Construct a new static RMQ algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link RmqStatic#newInstance()} to get a default implementation for the {@link RmqStatic}
	 * interface.
	 */
	public RmqStaticPlusMinusOne() {}

	@Override
	public RmqStatic.DataStructure preProcessSequence(RmqStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);
		return new PreProcessor(c, n).build();
	}

	private class PreProcessor extends RmqStaticLinearAbstract.PreProcessor {

		PreProcessor(RmqStaticComparator c, int n) {
			super(c, n);
			preProcessInnerBlocks();
		}

		@Override
		byte getBlockSize(int n) {
			int s = n <= 1 ? 1 : (int) Math.ceil(log2((double) n) * 2 / 3);
			/* choose block size of at least 5, as 2^(5-1) is 16 (small) */
			return (byte) Math.min(Math.max(s, 5), n);
		}

		@Override
		int getBlockKeySize() {
			return blockSize - 1;
		}

		@Override
		int calcBlockKey(int b) {
			RmqStaticComparator c = b < blockNum - 1 ? cmpOrig : cmpPadded;
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
			for (int i : range(1, demoBlock.length))
				demoBlock[i] = (byte) (demoBlock[i - 1] + ((key & (1 << (i - 1))) != 0 ? -1 : 1));

			return demoBlock;
		}

	}

}
