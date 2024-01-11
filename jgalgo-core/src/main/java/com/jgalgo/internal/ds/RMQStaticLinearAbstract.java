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
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

abstract class RMQStaticLinearAbstract implements RMQStatic {

	/*
	 * This implementation divides the elements sequence into blocks, for each block calculate the minimum in the block
	 * and the minimum within the block from each index to the borders of the block. In addition, we use the \(O(x \log
	 * x)\) implementation on the minimum values from each block (which we have less than n).
	 *
	 * To answer a query, if the two indices are not in the same block, we check the minimum from i to the end of the
	 * block, from j to the end of the block, and the minimum along all the blocks between them. If the two elements are
	 * not in the same block we have no implementation, and the implementations that extends this class will implement
	 * it in different methods.
	 *
	 * \(O(n)\) pre processing time, \(O(n)\) space, \(O(1)\) query.
	 */

	private final RMQStatic outerRMQ = new RMQStaticPowerOf2Table();

	abstract class PreProcessor extends DsBase {

		final RMQStaticComparator cmpPadded;
		final int blockNum;

		PreProcessor(RMQStaticComparator c, int n) {
			this.n = n;
			blockSize = getBlockSize(n);
			blockNum = (int) Math.ceil((double) n / blockSize);
			this.cmpOrig = c;
			this.cmpPadded = n < blockNum * blockSize ? new PaddedComparator(n, c) : c;
			blocksRightLeftMinimum = new byte[blockNum * (blockSize - 1) * 2];

			for (int b : range(blockNum)) {
				c = b < blockNum - 1 ? cmpOrig : cmpPadded;
				int base = b * blockSize;

				byte min = 0;
				for (byte i = 0; i < blockSize - 1; i++) {
					if (c.compare(base + i + 1, base + min) < 0)
						min = (byte) (i + 1);
					blockLeftMinimum(b, i, min);
				}

				min = (byte) (blockSize - 1);
				for (byte i = (byte) (blockSize - 2); i >= 0; i--) {
					if (c.compare(base + i, base + min) < 0)
						min = i;
					blockRightMinimum(b, i, min);
				}
			}

			xlogxTableDS = outerRMQ
					.preProcessSequence(
							(i, j) -> cmpOrig.compare(i * blockSize + blockMinimum(i), j * blockSize + blockMinimum(j)),
							blockNum);

		}

		void preProcessInnerBlocks() {
			var innerBlocksIdx = new Object() {
				int val = 0;
			};
			blockToInnerIdx = new int[blockNum];
			final int innerBlockAllocSize = blockSize * (blockSize - 1) / 2;
			Int2IntMap tables = new Int2IntOpenHashMap();
			for (int b : range(blockNum)) {
				int key = calcBlockKey(b);
				blockToInnerIdx[b] = key;
				tables.computeIfAbsent(key, k -> innerBlocksIdx.val++);
			}
			final int innerBlockNum = tables.size();
			Bitmap builtInnerBlocks = new Bitmap(innerBlockNum);
			innerBlocks = new byte[innerBlockNum * innerBlockAllocSize];
			for (int b : range(blockNum)) {
				int key = blockToInnerIdx[b];
				int innerIdx = tables.get(key);
				blockToInnerIdx[b] = innerIdx;
				if (!builtInnerBlocks.get(innerIdx)) {
					byte[] demoBlock = calcDemoBlock(key);
					buildInnerBlock(innerIdx, demoBlock);
					builtInnerBlocks.set(innerIdx);
				}
			}
			tables.clear();
		}

		private void buildInnerBlock(int innerBlock, byte[] demoBlock) {
			byte[] arr = innerBlocks;
			for (byte i = 0; i < blockSize - 1; i++)
				arr[innerBlockIndex(innerBlock, i, i + 1)] = demoBlock[i] < demoBlock[i + 1] ? i : (byte) (i + 1);
			for (byte i = 0; i < blockSize - 2; i++) {
				for (byte j = (byte) (i + 2); j < blockSize; j++) {
					byte m = arr[innerBlockIndex(innerBlock, i, j - 1)];
					arr[innerBlockIndex(innerBlock, i, j)] = demoBlock[m] < demoBlock[j] ? m : j;
				}
			}
		}

		abstract byte getBlockSize(int n);

		abstract int getBlockKeySize();

		abstract int calcBlockKey(int b);

		abstract byte[] calcDemoBlock(int key);

		RMQStatic.DataStructure build() {
			return new DataStructure(this);
		}

	}

	private static class DataStructure extends DsBase implements RMQStatic.DataStructure {

		DataStructure(RMQStaticLinearAbstract.PreProcessor ds) {
			n = ds.n;
			blockSize = ds.blockSize;
			blocksRightLeftMinimum = ds.blocksRightLeftMinimum;
			xlogxTableDS = ds.xlogxTableDS;
			blockToInnerIdx = ds.blockToInnerIdx;
			innerBlocks = ds.innerBlocks;
			cmpOrig = ds.cmpOrig;
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			RMQStatics.checkIndices(i, j, n);
			if (i == j)
				return i;

			int blk0 = i / blockSize;
			int blk1 = j / blockSize;
			int innerI = i % blockSize;
			int innerJ = j % blockSize;

			if (blk0 != blk1) {
				int blk0min = blk0 * blockSize + (innerI == blockSize - 1 ? innerI : blockRightMinimum(blk0, innerI));
				int blk1min = blk1 * blockSize + (innerJ == 0 ? innerJ : blockLeftMinimum(blk1, innerJ - 1));
				int min = cmpOrig.compare(blk0min, blk1min) < 0 ? blk0min : blk1min;

				if (blk0 + 1 != blk1) {
					int middleBlk = xlogxTableDS.findMinimumInRange(blk0 + 1, blk1 - 1);
					int middleMin = middleBlk * blockSize + blockMinimum(middleBlk);
					min = cmpOrig.compare(min, middleMin) < 0 ? min : middleMin;
				}

				return min;
			} else {
				return calcRMQInnerBlock(blk0, innerI, innerJ);
			}

		}

		int calcRMQInnerBlock(int block, int i, int j) {
			int r = block * blockSize;
			if (i == j)
				return r + i;
			int innerBlock = blockToInnerIdx[block];
			return r + innerBlocks[innerBlockIndex(innerBlock, i, j)];
		}

		@Override
		public long sizeInBytes() {
			long s = 0;
			s += 4; // n
			s += 4; // blockSize
			s += 8 + blocksRightLeftMinimum.length;
			s += 8 + xlogxTableDS.sizeInBytes();
			s += 8 + 4 * blockToInnerIdx.length;
			s += 8 + innerBlocks.length;
			s += 8; // cmp
			return s;
		}

	}

	private static class DsBase {

		int n;
		int blockSize;
		byte[] blocksRightLeftMinimum;
		RMQStatic.DataStructure xlogxTableDS;
		int[] blockToInnerIdx;
		byte[] innerBlocks;
		RMQStaticComparator cmpOrig;

		int blockMinimum(int block) {
			return blockRightMinimum(block, 0);
		}

		int blockRightMinimum(int block, int i) {
			return blocksRightLeftMinimum[(block * (blockSize - 1) + i) * 2 + 0];
		}

		int blockLeftMinimum(int block, int i) {
			return blocksRightLeftMinimum[(block * (blockSize - 1) + i) * 2 + 1];
		}

		void blockRightMinimum(int block, int i, byte val) {
			blocksRightLeftMinimum[(block * (blockSize - 1) + i) * 2 + 0] = val;
		}

		void blockLeftMinimum(int block, int i, byte val) {
			blocksRightLeftMinimum[(block * (blockSize - 1) + i) * 2 + 1] = val;
		}

		int innerBlockIndex(int innerBlock, int i, int j) {
			int innerIdx = (2 * blockSize - i - 1) * i / 2 + j - i - 1;
			return innerBlock * innerBlockAllocSize() + innerIdx;
		}

		int innerBlockAllocSize() {
			return blockSize * (blockSize - 1) / 2;
		}

	}

	private static class PaddedComparator implements RMQStaticComparator {

		final int n;
		final RMQStaticComparator c;

		PaddedComparator(int n, RMQStaticComparator c) {
			this.n = n;
			this.c = c;
		}

		@Override
		public int compare(int i, int j) {
			if (i < n && j < n)
				return c.compare(i, j);
			return i >= n ? 1 : -1;
		}

	}

}
