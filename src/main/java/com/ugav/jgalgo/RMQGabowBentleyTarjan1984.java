package com.ugav.jgalgo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RMQGabowBentleyTarjan1984 extends RMQLinearAbstract {

	/*
	 * Extends the abstract linear implementation of RMQ and solves the inner block
	 * query by calculating in advance all the possible blocks, and creating a naive
	 * lookup table for each one of them.
	 *
	 * Each block is essentially equivalent to another if the Cartesian tree of it
	 * is the same. The number of Cartesian trees of size n the Catalan number n,
	 * which is bounded by 4^n.
	 *
	 * We define the block size to be logn/4, and the preprocessing time is still
	 * O(n).
	 *
	 * O(n) preprocessing time, O(n) space, O(1) query.
	 */

	private RMQ[] interBlocksDs;

	public RMQGabowBentleyTarjan1984() {
	}

	@Override
	public void preprocessRMQ(RMQ.Comparator c, int n) {
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
				innerRMQ.preprocessRMQ(new ArrayIntComparator(demoBlock), demoBlock.length);
				return innerRMQ;
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

	private static int[] calcDemoBlock(int key, int blockSize) {
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
		return block * blockSize + interBlocksDs[block].calcRMQ(i, j + 1);
	}

}
