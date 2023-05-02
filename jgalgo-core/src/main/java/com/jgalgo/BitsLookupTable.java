package com.jgalgo;

/**
 * Lookup tables for bit operations.
 * <p>
 * Some algorithms which use the <a href="https://en.wikipedia.org/wiki/Random-access_machine">RAM model</a> perform
 * some bit operations such as popcount ({@link Integer#bitCount(int)}) or ctz
 * ({@link Integer#numberOfTrailingZeros(int)}) and assume these operations can be implemented in \(O(1)\). Although the
 * standard {@link Integer} implementation of these function is good in practice, its not implemented in 'real'
 * \(O(1)\), rather its implemented in \(O(\textit{wordsize})\) or \(O(\log \textit{wordsize})\). its possible to
 * implemented these operations in true \(O(1)\) time by constructing tables of size \(2^\textit{wordsize}\), which is
 * usually linear in the input size, and this is what this class purpose is.
 * <p>
 * The use of this class should be used with benchmarks, as its expected to use non negligible amount of memory and gain
 * little if any performance increase.
 *
 * @author Barak Ugav
 */
public class BitsLookupTable {

	private BitsLookupTable() {}

	/**
	 * Lookup table for bitCount (popcount) operation.
	 * <p>
	 * Lookup table that implement bitCount operation in \(O(1)\) time for any wordsize (maximum number of bits needed
	 * to represent an integer) using space \(O(2^\textit{wordsize})\) which is usually linear in the input size.
	 *
	 * <pre> {@code
	 * final int n = ...;
	 * int[] numbers = new int[] {0, 1, 2, ..., n};
	 *
	 * // wordsize = ceil(log2(n))
	 * final int wordsize = (int) Math.ceil((Math.log(n) / Math.log(2)));
	 * BitsLookupTable.Count table = new BitsLookupTable.Count(wordsize);
	 *
	 * for (int x : numbers) {
	 *     assert Integer.bitCount(x) == table.bitCount(x);
	 *     System.out.println("The number of 1 bits in " + x + " is " + table.bitCount(x));
	 * }
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class Count {

		private final int wordsize;
		private final byte[] bitCountTable;

		/**
		 * Construct a new lookup table for bitCount operation for integer words of some maximum size.
		 *
		 * @param wordsize maximum number of bits needed to represent an integer
		 */
		public Count(int wordsize) {
			if (!(0 < wordsize && wordsize < Integer.SIZE - 1))
				throw new IllegalArgumentException("unsupported word size: " + wordsize);
			this.wordsize = wordsize;
			bitCountTable = new byte[1 << wordsize];

			/* init table */
			for (int highBit = 0; highBit < wordsize; highBit++) {
				for (int prevx = 0; prevx < 1 << highBit; prevx++) {
					int x = prevx | (1 << highBit);
					bitCountTable[x] = (byte) (bitCountTable[prevx] + 1);
				}
			}
		}

		/**
		 * Get the number of 1 bits in an integer.
		 * <p>
		 * This function is equivalent to {@link Integer#bitCount(int)}, but its implemented in 'true' \(O(1)\) and
		 * therefore faster (in theory!).
		 *
		 * @param  x an integer
		 * @return   the number of 1 bits in the given integer
		 */
		public int bitCount(int x) {
			return bitCountTable[x];
		}

	}

	/**
	 * Lookup table for getting the i-th bit in an integer.
	 * <p>
	 * Define a list \(S(x)\) as ordered list containing all indices of 1 bits of an integer number \(x\), namely given
	 * an integer \(x = 0b \; b_{31} b_{30} ... b_0\) where \(b_i\) is the \(i\)-th bit of \(x\), define \(S(x)\) as the
	 * ordered list \(S = (i \mid b_i = 1)\), for example \(x = 9 = 0b \; 1001, S(x) = (0, 3)\). The \(i\)-th bit of a
	 * number \(x\) is \(S(x)[i]\). This function does not have a standard implementation in the {@link Integer} class,
	 * but could implemented easily in \(O(\textit{wordsize})\) time.
	 * <p>
	 * This class construct a lookup table to answer an i-th bit query in \(O(1)\) time using a table of size
	 * \(O(2^\textit{wordsize})\) space which is usually linear in the input size.
	 *
	 * <pre> {@code
	 * final int n = ...;
	 * int[] numbers = new int[] {0, 1, 2, ..., n};
	 *
	 * // wordsize = ceil(log2(n))
	 * final int wordsize = (int) Math.ceil((Math.log(n) / Math.log(2)));
	 * BitsLookupTable.Count bitCountTable = new BitsLookupTable.Count(wordsize);
	 * BitsLookupTable.Ith table = new BitsLookupTable.Ith(wordsize, bitCountTable);
	 *
	 * for (int x : numbers) {
	 *     int numberOfOneBits = bitCountTable.bitCount(x);
	 *     for (int i = 0; i < numberOfOneBits; i++)
	 *         System.out.println("The " + i + "-th bit of " + x + " is " + table.ithBit(x, i));
	 * }
	 * }</pre>
	 *
	 * @author Barak Ugav
	 */
	public static class Ith {

		private final int wordsize;
		private final BitsLookupTable.Count count;
		private final byte[][] ithBitTable;

		/**
		 * Construct a new lookup table for i-th bit operation for integer words of some maximum size.
		 *
		 * @param wordsize maximum number of bits needed to represent an integer
		 * @param count    a lookup table for the bitCount (popcount) operation
		 */
		public Ith(int wordsize, BitsLookupTable.Count count) {
			if (!(0 < wordsize && wordsize < Integer.SIZE - 1))
				throw new IllegalArgumentException("unsupported word size: " + wordsize);
			if (count.wordsize < wordsize)
				throw new IllegalArgumentException();
			this.count = count;
			this.wordsize = wordsize;
			int halfwordsize = ((wordsize - 1) / 2 + 1);
			ithBitTable = new byte[1 << halfwordsize][halfwordsize];

			/* init table */
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

		/**
		 * Get the index of i-th one bit of an integer.
		 * <p>
		 * Define a list \(S(x)\) as ordered list containing all indices of 1 bits, for example \(x = 9 = 0b1001, S(x) =
		 * (0 ,3)\). The \(i\)-th bit of \(x\) is defined as \(S(x)[i]\).
		 *
		 * @param  x                         an integer number
		 * @param  i                         index of a one bit in range {@code [0, bitCount(x))}.
		 * @return                           the index of the i-th one bit on the given integer
		 * @throws IndexOutOfBoundsException if {@code i < 0} or {@code i >= bitCount(x)}.
		 */
		public int ithBit(int x, int i) {

			/*
			 * the ithBitTable is of size [2^halfwordsize][halfwordsize] and we answer a query by 2 lookup tables. Using
			 * the easy [2^wordsize][wordsize] will results in \(O(n \log n)\) time and size.
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

		/**
		 * Get the number of trailing zeros of an integer.
		 *
		 * @param  x an integer
		 * @return   the number of trailing zeros of an integer.
		 * @see      Integer#numberOfTrailingZeros(int)
		 */
		public int numberOfTrailingZeros(int x) {
			return x == 0 ? Integer.SIZE : ithBit(x, 0);
		}

	}

}
