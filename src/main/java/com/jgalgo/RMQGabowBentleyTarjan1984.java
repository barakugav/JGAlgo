package com.jgalgo;

import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

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
	 * We define the block size to be logn/4, and the pre processing time is still
	 * O(n).
	 *
	 * O(n) pre processing time, O(n) space, O(1) query.
	 */

	private RMQ[] interBlocksDs;

	public RMQGabowBentleyTarjan1984() {
	}

	@Override
	public void preProcessRMQ(RMQComparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);

		interBlocksDs = new RMQ[calcBlockNum(n, getBlockSize(n))];
		preProcessRMQOuterBlocks(c, n);
		preProcessRMQInnerBlocks();
	}

	private void preProcessRMQInnerBlocks() {
		Int2ObjectMap<RMQ> tables = new Int2ObjectOpenHashMap<>();

		for (int b = 0; b < blockNum; b++) {
			int key = calcBlockKey(b);

			interBlocksDs[b] = tables.computeIfAbsent(key, k -> {
				int[] demoBlock = calcDemoBlock(k, blockSize);
				RMQ innerRMQ = new RMQLookupTable();
				innerRMQ.preProcessRMQ(RMQComparator.ofIntArray(demoBlock), demoBlock.length);
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
