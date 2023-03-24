package com.ugav.jgalgo;

abstract class RMQLinearAbstract implements RMQ {

	/*
	 * This implementation divides the elements sequence into blocks, for each block
	 * calculate the minimum in the block and the minimum within the block from each
	 * index to the borders of the block. In addition, we use the O(x log x)
	 * implementation on the minimum values from each block (which we have less than
	 * n).
	 *
	 * To answer a query, if the two indices are not in the same block, we check the
	 * minimum from i to the end of the block, from j to the end of the block, and
	 * the minimum along all the blocks between them. If the two elements are not in
	 * the same block we have no implementation, and the implementations that
	 * extends this class will implement it in different methods.
	 *
	 * O(n) preprocessing time, O(n) space, O(1) query.
	 */

	int n;
	int blockSize;
	int blockNum;
	RMQ.Comparator c;

	private int[][] blocksRightMinimum;
	private int[][] blocksLeftMinimum;
	private final RMQPowerOf2Table xlogxTable;

	boolean preprocessed;

	RMQLinearAbstract() {
		xlogxTable = new RMQPowerOf2Table();
		preprocessed = false;
	}

	void preprocessRMQOuterBlocks(RMQ.Comparator c, int n) {
		blockSize = getBlockSize(n);
		blockNum = calcBlockNum(n, blockSize);

		this.n = n;
		this.c = c = n < blockNum * blockSize ? new PadderComparator(n, c) : c;

		blocksRightMinimum = new int[blockNum][blockSize - 1];
		blocksLeftMinimum = new int[blockNum][blockSize - 1];

		for (int b = 0; b < blockNum; b++) {
			int base = b * blockSize;

			int min = 0;
			for (int i = 0; i < blockSize - 1; i++) {
				if (c.compare(base + i + 1, base + min) < 0)
					min = i + 1;
				blocksLeftMinimum[b][i] = base + min;
			}

			min = blockSize - 1;
			for (int i = blockSize - 2; i >= 0; i--) {
				if (c.compare(base + i, base + min) < 0)
					min = i;
				blocksRightMinimum[b][i] = base + min;
			}
		}

		xlogxTable.preprocessRMQ((i, j) -> this.c.compare(blocksRightMinimum[i][0], blocksRightMinimum[j][0]),
				blockNum);

		preprocessed = true;
	}

	abstract int getBlockSize(int n);

	static int calcBlockNum(int n, int blockSize) {
		return (int) Math.ceil((double) n / blockSize);
	}

	@Override
	public int calcRMQ(int i, int j) {
		if (!preprocessed)
			throw new IllegalStateException("Preprocessing is required before query");
		if (i < 0 || j <= i || j > n)
			throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
		if (i + 1 == j)
			return i;
		j--;

		int blk0 = i / blockSize;
		int blk1 = j / blockSize;
		int innerI = i % blockSize;
		int innerJ = j % blockSize;

		if (blk0 != blk1) {
			int blk0min = innerI == blockSize - 1 ? blk0 * blockSize + innerI : blocksRightMinimum[blk0][innerI];
			int blk1min = innerJ == 0 ? blk1 * blockSize + innerJ : blocksLeftMinimum[blk1][innerJ - 1];
			int min = c.compare(blk0min, blk1min) < 0 ? blk0min : blk1min;

			if (blk0 + 1 != blk1) {
				int middleBlk = xlogxTable.calcRMQ(blk0 + 1, blk1);
				int middleMin = blocksRightMinimum[middleBlk][0];
				min = c.compare(min, middleMin) < 0 ? min : middleMin;
			}

			return min;
		} else {
			return calcRMQInnerBlock(blk0, innerI, innerJ);
		}

	}

	abstract int calcRMQInnerBlock(int block, int i, int j);

	private static class PadderComparator implements RMQ.Comparator {

		final int n;
		final RMQ.Comparator c;

		PadderComparator(int n, RMQ.Comparator c) {
			this.n = n;
			this.c = c;
		}

		@Override
		public int compare(int i, int j) {
			return i >= n ? i : c.compare(i, Math.min(j, n - 1));
		}

	}

}
