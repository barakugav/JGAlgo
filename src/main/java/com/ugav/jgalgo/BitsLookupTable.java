package com.ugav.jgalgo;

public class BitsLookupTable {

	private BitsLookupTable() {
	}

	public static class Count {

		private final int wordsize;
		private final byte[] bitCountTable;

		public Count(int wordsize) {
			if (!(0 < wordsize && wordsize < Integer.SIZE - 1))
				throw new IllegalArgumentException("unsupported word size: " + wordsize);
			this.wordsize = wordsize;
			bitCountTable = new byte[1 << wordsize];
		}

		public void init() {
			for (int highBit = 0; highBit < wordsize; highBit++) {
				for (int prevx = 0; prevx < 1 << highBit; prevx++) {
					int x = prevx | (1 << highBit);
					bitCountTable[x] = (byte) (bitCountTable[prevx] + 1);
				}
			}
		}

		public int bitCount(int x) {
			return bitCountTable[x];
		}

	}

	public static class Ith {

		private final int wordsize;
		private final Count count;
		private final byte[][] ithBitTable;

		public Ith(int wordsize, Count count) {
			if (!(0 < wordsize && wordsize < Integer.SIZE - 1))
				throw new IllegalArgumentException("unsupported word size: " + wordsize);
			if (count.wordsize < wordsize)
				throw new IllegalArgumentException();
			this.count = count;
			this.wordsize = wordsize;
			int halfwordsize = ((wordsize - 1) / 2 + 1);
			ithBitTable = new byte[1 << halfwordsize][halfwordsize];
		}

		public void init() {
			int halfwordsize = ((wordsize - 1) / 2 + 1);
			for (int highBit = 0; highBit < halfwordsize; highBit++) {
				for (int prevx = 0; prevx < 1 << highBit; prevx++) {
					int x = prevx | (1 << highBit);
					int xBitCount = count.bitCount(x);
					ithBitTable[x][xBitCount - 1] = (byte) (highBit);
					for (int i = xBitCount - 2; i >= 0; i--)
						ithBitTable[x][i] = ithBitTable[prevx][i];
				}
			}
		}

		public int ithBit(int x, int i) {

			/*
			 * the ithBitTable is of size [2^halfwordsize][halfwordsize] and we answer a
			 * query by 2 lookup tables. Using the easy [2^wordsize][wordsize] will results
			 * in O(n log n) time and size.
			 */

			if (i < 0 || i >= count.bitCount(x))
				throw new IndexOutOfBoundsException(Integer.toBinaryString(x) + "[" + i + "]");
			int halfwordsize = ((wordsize - 1) / 2 + 1);
			if (x < 1 << halfwordsize)
				return ithBitTable[x][i];

			int xlow = x & ((1 << halfwordsize) - 1);
			int xlowcount = count.bitCount(xlow);
			if (i < xlowcount)
				return ithBitTable[xlow][i];

			int xhigh = x >> halfwordsize;
			return halfwordsize + ithBitTable[xhigh][i - xlowcount];
		}

		public int numberOfTrailingZeros(int x) {
			return x == 0 ? Integer.SIZE : ithBit(x, 0);
		}

	}

}
