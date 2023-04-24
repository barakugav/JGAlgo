package com.jgalgo;

import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Static RMQ for sequences for which the different between any pair of
 * consecutive elements is <span>&#177;</span>{@code 1}.
 * <p>
 * The algorithm divide the sequence into blocks of size {@code O((log n) / 2)},
 * and then create all possible lookup tables of the sub sequences of the
 * smaller blocks. Because the different between any pair of consecutive
 * elements, the number of such blocks is {@code O(} <span>&#8730;</span>
 * {@code n)} and the total space and preprocessing time of all these blocks is
 * {@code O(n)}.
 * <p>
 * To answer on queries which does not fall in the same block, the minimum of
 * each block is stored, and {@link RMQStaticPowerOf2Table} is used on the
 * {@code O(n / log n)} elements, which is linear in total.
 * <p>
 * This algorithm is used for the static implementation of the lowest common
 * ancestor algorithm, see {@link LCAStaticRMQ}.
 * <p>
 * Based on 'Fast Algorithms for Finding Nearest Common Ancestors' by D. Harel,
 * R. Tarjan (1984).
 *
 * @author Barak Ugav
 */
public class RMQStaticPlusMinusOne extends RMQStaticLinearAbstract {

	/**
	 * Construct a new static RMQ algorithm object.
	 */
	public RMQStaticPlusMinusOne() {
	}

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
					int[] demoBlock = calcDemoBlock(k);
					return innerRMQ.preProcessSequence(RMQStaticComparator.ofIntArray(demoBlock), demoBlock.length);
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
			return block * blockSize + interBlocksDs[block].findMinimumInRange(i, j);
		}

	}

}
