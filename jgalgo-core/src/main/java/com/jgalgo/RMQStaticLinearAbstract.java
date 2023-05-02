package com.jgalgo;

abstract class RMQStaticLinearAbstract implements RMQStatic {

	/*
	 * This implementation divides the elements sequence into blocks, for each block
	 * calculate the minimum in the block and the minimum within the block from each
	 * index to the borders of the block. In addition, we use the \(O(x \log x)\)
	 * implementation on the minimum values from each block (which we have less than
	 * n).
	 *
	 * To answer a query, if the two indices are not in the same block, we check the
	 * minimum from i to the end of the block, from j to the end of the block, and
	 * the minimum along all the blocks between them. If the two elements are not in
	 * the same block we have no implementation, and the implementations that
	 * extends this class will implement it in different methods.
	 *
	 * \(O(n)\) pre processing time, \(O(n)\) space, \(O(1)\) query.
	 */

	private final RMQStaticPowerOf2Table xlogxTable;

	RMQStaticLinearAbstract() {
		xlogxTable = new RMQStaticPowerOf2Table();
	}

	abstract class DS implements RMQStatic.DataStructure {

		int n;
		int blockSize;
		int blockNum;
		RMQStaticComparator c;

		private int[][] blocksRightMinimum;
		private int[][] blocksLeftMinimum;
		private RMQStatic.DataStructure xlogxTableDS;

		void preProcessRMQOuterBlocks(RMQStaticComparator c, int n) {
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

			xlogxTableDS = xlogxTable.preProcessSequence(
					(i, j) -> this.c.compare(blocksRightMinimum[i][0], blocksRightMinimum[j][0]),
					blockNum);
		}

		abstract int getBlockSize(int n);

		int calcBlockNum(int n, int blockSize) {
			return (int) Math.ceil((double) n / blockSize);
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
				int blk0min = innerI == blockSize - 1 ? blk0 * blockSize + innerI : blocksRightMinimum[blk0][innerI];
				int blk1min = innerJ == 0 ? blk1 * blockSize + innerJ : blocksLeftMinimum[blk1][innerJ - 1];
				int min = c.compare(blk0min, blk1min) < 0 ? blk0min : blk1min;

				if (blk0 + 1 != blk1) {
					int middleBlk = xlogxTableDS.findMinimumInRange(blk0 + 1, blk1 - 1);
					int middleMin = blocksRightMinimum[middleBlk][0];
					min = c.compare(min, middleMin) < 0 ? min : middleMin;
				}

				return min;
			} else {
				return calcRMQInnerBlock(blk0, innerI, innerJ);
			}
		}

		abstract int calcRMQInnerBlock(int block, int i, int j);
	}

	private static class PadderComparator implements RMQStaticComparator {

		final int n;
		final RMQStaticComparator c;

		PadderComparator(int n, RMQStaticComparator c) {
			this.n = n;
			this.c = c;
		}

		@Override
		public int compare(int i, int j) {
			return i >= n ? i : c.compare(i, Math.min(j, n - 1));
		}

	}

}
