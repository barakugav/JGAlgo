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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

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
public class RMQStaticCartesianTrees extends RMQStaticLinearAbstract {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	public RMQStaticCartesianTrees() {}

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQStaticComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);
		return new DS(c, n);
	}

	private class DS extends RMQStaticLinearAbstract.DS {

		private RMQStatic.DataStructure[] interBlocksDs;

		DS(RMQStaticComparator c, int n) {
			interBlocksDs = new RMQStatic.DataStructure[calcBlockNum(n, getBlockSize(n))];
			preProcessRMQOuterBlocks(c, n);
			preProcessRMQInnerBlocks();
		}

		private void preProcessRMQInnerBlocks() {
			RMQStatic innerRMQ = new RMQStaticLookupTable();
			Int2ObjectMap<RMQStatic.DataStructure> tables = new Int2ObjectOpenHashMap<>();

			for (int b = 0; b < blockNum; b++) {
				int key = calcBlockKey(b);

				interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
					int[] demoBlock = calcDemoBlock(k, blockSize);
					return innerRMQ.preProcessSequence(RMQStaticComparator.ofIntArray(demoBlock), demoBlock.length);
				});
			}
		}

		private int calcBlockKey(int b) {
			int[] nodes = new int[blockSize];
			int nodesCount = 0;

			int key = 0;
			int keyIdx = 0;

			int base = b * blockSize;
			for (int i = 0; i < blockSize; i++) {
				int x = base + i;
				while (nodesCount > 0 && c.compare(x, nodes[nodesCount - 1]) < 0) {
					nodesCount--;
					key |= 1 << (keyIdx++);
				}
				nodes[nodesCount++] = x;
				keyIdx++;
			}

			return key;
		}

		private int[] calcDemoBlock(int key, int blockSize) {
			int[] demoBlock = new int[blockSize];

			int[] nodes = new int[blockSize];
			int nodesCount = 0;

			int keyIdx = 0;

			for (int i = 0; i < demoBlock.length; i++) {
				int x = nodesCount > 0 ? nodes[nodesCount - 1] + blockSize : 0;
				while ((key & (1 << keyIdx)) != 0) {
					x = nodes[nodesCount-- - 1] - 1;
					keyIdx++;
				}
				nodes[nodesCount++] = x;
				keyIdx++;

				demoBlock[i] = x;
			}

			return demoBlock;
		}

		@Override
		int getBlockSize(int n) {
			return (int) Math.ceil(Utils.log2((double) n) / 4);
		}

		@Override
		int calcRMQInnerBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j);
		}

	}

}
