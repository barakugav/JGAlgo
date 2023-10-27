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

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

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

	RMQStaticLinearAbstract() {}

	abstract class PreProcessor {

		final RMQStaticComparator cmpOrig;
		final RMQStaticComparator cmpPadded;
		final int n;
		final byte blockSize;
		final int blockNum;

		private final byte[][] blocksRightMinimum;
		private final byte[][] blocksLeftMinimum;
		private final RMQStatic.DataStructure xlogxTableDS;

		final RMQStatic.DataStructure[] interBlocksDs;
		private final RMQStatic outerRMQ;
		private final RMQStatic innerRMQ;

		PreProcessor(RMQStaticComparator c, int n) {
			this.n = n;
			blockSize = getBlockSize(n);
			blockNum = (int) Math.ceil((double) n / blockSize);
			this.cmpOrig = c;
			this.cmpPadded = n < blockNum * blockSize ? new PaddedComparator(n, c) : c;
			blocksRightMinimum = new byte[blockNum][blockSize - 1];
			blocksLeftMinimum = new byte[blockNum][blockSize - 1];

			outerRMQ = new RMQStaticPowerOf2Table();
			// TODO probably better to use a simple lookup table, need to measure
			innerRMQ = new RMQStaticPowerOf2Table();

			for (int b = 0; b < blockNum; b++) {
				c = b < blockNum - 1 ? cmpOrig : cmpPadded;
				int base = b * blockSize;

				byte min = 0;
				for (byte i = 0; i < blockSize - 1; i++) {
					if (c.compare(base + i + 1, base + min) < 0)
						min = (byte) (i + 1);
					blocksLeftMinimum[b][i] = min;
				}

				min = (byte) (blockSize - 1);
				for (byte i = (byte) (blockSize - 2); i >= 0; i--) {
					if (c.compare(base + i, base + min) < 0)
						min = i;
					blocksRightMinimum[b][i] = min;
				}
			}

			xlogxTableDS =
					outerRMQ.preProcessSequence((i, j) -> cmpOrig.compare(i * blockSize + blocksRightMinimum[i][0],
							j * blockSize + blocksRightMinimum[j][0]), blockNum);

			interBlocksDs = new RMQStatic.DataStructure[blockNum];
		}

		void preProcessInnerBlocks() {
			int keySize = getBlockKeySize();
			if (keySize < Byte.SIZE) {
				Byte2ObjectMap<RMQStatic.DataStructure> tables = new Byte2ObjectOpenHashMap<>();
				for (int b = 0; b < blockNum; b++) {
					byte key = (byte) calcBlockKey(b);

					interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
						byte[] demoBlock = calcDemoBlock(k & 0xff);
						return innerRMQ.preProcessSequence(RMQStaticComparator.ofByteArray(demoBlock),
								demoBlock.length);
					});
				}
				tables.clear();

			} else if (keySize < Short.SIZE) {
				Short2ObjectMap<RMQStatic.DataStructure> tables = new Short2ObjectOpenHashMap<>();
				for (int b = 0; b < blockNum; b++) {
					short key = (short) calcBlockKey(b);

					interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
						byte[] demoBlock = calcDemoBlock(k & 0xffff);
						return innerRMQ.preProcessSequence(RMQStaticComparator.ofByteArray(demoBlock),
								demoBlock.length);
					});
				}
				tables.clear();

			} else {
				Int2ObjectMap<RMQStatic.DataStructure> tables = new Int2ObjectOpenHashMap<>();
				for (int b = 0; b < blockNum; b++) {
					int key = calcBlockKey(b);

					interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
						byte[] demoBlock = calcDemoBlock(k);
						return innerRMQ.preProcessSequence(RMQStaticComparator.ofByteArray(demoBlock),
								demoBlock.length);
					});
				}
				tables.clear();
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

	private static class DataStructure implements RMQStatic.DataStructure {

		private final int n;
		private final int blockSize;
		private final byte[][] blocksRightMinimum;
		private final byte[][] blocksLeftMinimum;
		private final RMQStatic.DataStructure xlogxTableDS;
		private final RMQStatic.DataStructure[] interBlocksDs;
		private final RMQStaticComparator cmpOrig;

		DataStructure(RMQStaticLinearAbstract.PreProcessor ds) {
			n = ds.n;
			blockSize = ds.blockSize;
			blocksRightMinimum = ds.blocksRightMinimum;
			blocksLeftMinimum = ds.blocksLeftMinimum;
			xlogxTableDS = ds.xlogxTableDS;
			interBlocksDs = ds.interBlocksDs;
			cmpOrig = ds.cmpOrig;
		}

		@Override
		public int findMinimumInRange(int i, int j) {
			if (!(0 <= i && i <= j && j < n))
				throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
			if (i == j)
				return i;

			int blk0 = i / blockSize;
			int blk1 = j / blockSize;
			int innerI = i % blockSize;
			int innerJ = j % blockSize;

			if (blk0 != blk1) {
				int blk0min = blk0 * blockSize + (innerI == blockSize - 1 ? innerI : blocksRightMinimum[blk0][innerI]);
				int blk1min = blk1 * blockSize + (innerJ == 0 ? innerJ : blocksLeftMinimum[blk1][innerJ - 1]);
				int min = cmpOrig.compare(blk0min, blk1min) < 0 ? blk0min : blk1min;

				if (blk0 + 1 != blk1) {
					int middleBlk = xlogxTableDS.findMinimumInRange(blk0 + 1, blk1 - 1);
					int middleMin = middleBlk * blockSize + blocksRightMinimum[middleBlk][0];
					min = cmpOrig.compare(min, middleMin) < 0 ? min : middleMin;
				}

				return min;
			} else {
				return calcRMQInnerBlock(blk0, innerI, innerJ);
			}

		}

		int calcRMQInnerBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j);
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
