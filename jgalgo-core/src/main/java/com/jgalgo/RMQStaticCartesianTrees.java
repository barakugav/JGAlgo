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
 * Static RMQ which uses Cartesian trees answering a query in constant time and requiring linear preprocessing time.
 * <p>
 * The sequence is divided into blocks of size \((\log n) / 4\), and for each block a Cartesian tree is created. The
 * total number of possible Cartesian trees of such size is bounded by \(O(n)\) (Catalan number).
 * <p>
 * To answer on queries which does not fall in the same block, the minimum of each block is stored, and
 * {@link RMQStaticPowerOf2Table} is used on the \(O(n / \log n)\) elements, which is linear in total.
 * <p>
 * The algorithm required \(O(n)\) preprocessing time and space and answer queries in \(O(1)\) time.
 * <p>
 * Based on 'Scaling and related techniques for geometry problems' by Harold N. Gabow; Jon Louis Bentley; Robert E.
 * Tarjan (1984).
 *
 * @author Barak Ugav
 */
class RMQStaticCartesianTrees extends RMQStaticLinearAbstract {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	RMQStaticCartesianTrees() {}

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);
		return new DS(c, n);
	}

	private class DS extends RMQStaticLinearAbstract.DS {

		private final byte[] tempNodesArray;

		DS(RMQStaticComparator c, int n) {
			super(c, n);
			tempNodesArray = new byte[blockSize];
			preProcessInnerBlocks();
		}

		@Override
		byte getBlockSize(int n) {
			int s = (int) Math.ceil(Utils.log2((double) n) / 3);
			/* choose block size of at least 4, as the Catalan number of 4 is 14 (small) */
			return (byte) Math.min(Math.max(s, 4), n);
		}

		@Override
		int getBlockKeySize() {
			return blockSize * 2;
		}

		@Override
		int calcBlockKey(int b) {
			RMQStaticComparator c = b < blockNum - 1 ? cmpOrig : cmpPadded;
			byte[] nodes = tempNodesArray;
			int nodesCount = 0;

			int key = 0;
			int keyIdx = 0;

			int base = b * blockSize;
			for (byte i = 0; i < blockSize; i++) {
				byte x = i;
				while (nodesCount > 0 && c.compare(base + x, base + nodes[nodesCount - 1]) < 0) {
					nodesCount--;
					key |= 1 << (keyIdx++);
				}
				nodes[nodesCount++] = x;
				keyIdx++;
			}

			return key;
		}

		@Override
		byte[] calcDemoBlock(int key) {
			byte[] demoBlock = new byte[blockSize];

			byte[] nodes = tempNodesArray;
			int nodesCount = 0;

			int keyIdx = 0;

			for (int i = 0; i < demoBlock.length; i++) {
				byte x = (byte) (nodesCount > 0 ? nodes[nodesCount - 1] + blockSize : 0);
				while ((key & (1 << keyIdx)) != 0) {
					x = (byte) (nodes[nodesCount-- - 1] - 1);
					keyIdx++;
				}
				nodes[nodesCount++] = x;
				keyIdx++;

				demoBlock[i] = x;
			}

			return demoBlock;
		}

		@Override
		int calcRMQInnerBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j);
		}

	}

}
