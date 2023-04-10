package com.jgalgo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RMQPlusMinusOneBenderFarachColton2000 extends RMQLinearAbstract {

	/*
	 * Extends the abstract linear implementation of RMQ and solves the inner block
	 * query by calculating in advance all the possible blocks, and creating a naive
	 * lookup table for each one of them. This is only possible because the
	 * difference between each consecutive elements is +1/-1.
	 *
	 * We define the block size to be logn/2 and therefore there are 2^blockSize
	 * possible different blocks, and the preprocessing time is still O(n).
	 *
	 * O(n) preprocessing time, O(n) space, O(1) query.
	 */

	private RMQ[] interBlocksDs;

	public RMQPlusMinusOneBenderFarachColton2000() {
	}

	@Override
	public void preprocessRMQ(RMQComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);

		interBlocksDs = new RMQ[calcBlockNum(n, getBlockSize(n))];
		preprocessRMQOuterBlocks(c, n);
		preprocessRMQInnerBlocks();
	}

	private void preprocessRMQInnerBlocks() {
		Map<Integer, RMQ> tables = new HashMap<>();

		for (int b = 0; b < blockNum; b++) {
			int key = calcBlockKey(b);

			interBlocksDs[b] = tables.computeIfAbsent(Integer.valueOf(key), k -> {
				int[] demoBlock = calcDemoBlock(k.intValue(), blockSize);
				RMQ innerRMQ = new RMQLookupTable();
				innerRMQ.preprocessRMQ(RMQComparator.ofIntArray(demoBlock), demoBlock.length);
				return innerRMQ;
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

	private static int[] calcDemoBlock(int key, int blockSize) {
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
		return block * blockSize + interBlocksDs[block].calcRMQ(i, j + 1);
	}

}
