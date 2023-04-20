package com.jgalgo;

import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class RMQPlusMinusOneBenderFarachColton2000 extends RMQLinearAbstract {

	/*
	 * Extends the abstract linear implementation of RMQ and solves the inner block
	 * query by calculating in advance all the possible blocks, and creating a naive
	 * lookup table for each one of them. This is only possible because the
	 * difference between each consecutive elements is +1/-1.
	 *
	 * We define the block size to be logn/2 and therefore there are 2^blockSize
	 * possible different blocks, and the pre processing time is still O(n).
	 *
	 * O(n) pre processing time, O(n) space, O(1) query.
	 */

	@Override
	public RMQStatic.DataStructure preProcessSequence(RMQComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);
		return new DS(c, n);
	}

	private class DS extends RMQLinearAbstract.DS {

		private RMQStatic.DataStructure[] interBlocksDs;

		DS(RMQComparator c, int n) {
			interBlocksDs = new RMQStatic.DataStructure[calcBlockNum(n, getBlockSize(n))];
			preProcessRMQOuterBlocks(c, n);
			preProcessRMQInnerBlocks();
		}

		private void preProcessRMQInnerBlocks() {
			RMQStatic innerRMQ = new RMQLookupTable();
			Int2ObjectMap<RMQStatic.DataStructure> tables = new Int2ObjectOpenHashMap<>();

			for (int b = 0; b < blockNum; b++) {
				int key = calcBlockKey(b);

				interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
					int[] demoBlock = calcDemoBlock(k);
					return innerRMQ.preProcessSequence(RMQComparator.ofIntArray(demoBlock), demoBlock.length);
				});
			}
		}

		private int calcBlockKey(int b) {
			int key = 0;

			int base = b * blockSize;
			for (int i = blockSize - 2; i >= 0; i--) {
				key <<= 1;
				if (c.compare(base + i + 1, base + i) < 0)
					key |= 1;
			}

			return key;
		}

		private int[] calcDemoBlock(int key) {
			int[] demoBlock = new int[blockSize];

			demoBlock[0] = 0;
			for (int i = 1; i < demoBlock.length; i++)
				demoBlock[i] = demoBlock[i - 1] + ((key & (1 << (i - 1))) != 0 ? -1 : 1);

			return demoBlock;
		}

		@Override
		int getBlockSize(int n) {
			return n <= 1 ? 1 : (int) Math.ceil(Utils.log2((double) n) / 2);
		}

		@Override
		int calcRMQInnerBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j + 1);
		}

	}

}
