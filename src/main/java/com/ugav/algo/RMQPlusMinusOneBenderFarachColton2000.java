package com.ugav.algo;

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

	private RMQPlusMinusOneBenderFarachColton2000() {
	}

	private static final RMQPlusMinusOneBenderFarachColton2000 INSTANCE = new RMQPlusMinusOneBenderFarachColton2000();

	public static RMQPlusMinusOneBenderFarachColton2000 getInstace() {
		return INSTANCE;
	}

	@Override
	public RMQ.Result preprocessRMQ(RMQ.Comparator c, int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(c);

		RMQPlusMinusOneBenderFarachColton2000.DataStructure ds = new RMQPlusMinusOneBenderFarachColton2000.DataStructure(
				n, c);

		preprocessRMQ(ds);

		return ds;
	}

	int[] calcDemoBlock(int key, int blockSize) {
		int demoBlock[] = new int[blockSize];

		demoBlock[0] = 0;
		for (int i = 1; i < demoBlock.length; i++)
			demoBlock[i] = demoBlock[i - 1] + ((key & (1 << (i - 1))) != 0 ? -1 : 1);

		return demoBlock;
	}

	int calcBlockKey(RMQPlusMinusOneBenderFarachColton2000.DataStructure ds, int b) {
		int key = 0;

		int base = b * ds.blockSize;
		for (int i = ds.blockSize - 2; i >= 0; i--) {
			key <<= 1;
			if (ds.c.compare(base + i + 1, base + i) < 0)
				key |= 1;
		}

		return key;
	}

	@Override
	void initInterBlock(RMQLinearAbstract.DataStructure ds0) {
		RMQPlusMinusOneBenderFarachColton2000.DataStructure ds = (RMQPlusMinusOneBenderFarachColton2000.DataStructure) ds0;

		Map<Integer, RMQ.Result> tables = new HashMap<>();

		for (int b = 0; b < ds.blockNum; b++) {
			int key = calcBlockKey(ds, b);

			ds.interBlocksDs[b] = tables.computeIfAbsent(Integer.valueOf(key), k -> {
				int demoBlock[] = calcDemoBlock(k.intValue(), ds.blockSize);
				return RMQLookupTable.getInstace().preprocessRMQ(new ArrayIntComparator(demoBlock), demoBlock.length);
			});
		}
	}

	static class DataStructure extends RMQLinearAbstract.DataStructure {

		final RMQ.Result interBlocksDs[];

		DataStructure(int n, Comparator c) {
			super(n, c);
			interBlocksDs = new RMQ.Result[blockNum];
		}

		@Override
		int getBlockSize(int n) {
			return n <= 1 ? 1 : (int) Math.ceil(Utils.log2((double) n) / 2);
		}

		@Override
		int queryInterBlock(int block, int i, int j) {
			return block * blockSize + interBlocksDs[block].query(i, j + 1);
		}

	}

}
