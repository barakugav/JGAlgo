package com.jgalgo.test;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntIterator;

class Utils {
	private Utils() {
	}

	/* syntax sugar to iterator for loops */
	static <E> Iterable<E> iterable(Iterator<E> it) {
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return it;
			}
		};
	}

	static IntIterator bitSetIterator(BitSet bitSet) {
		return new IntIterator() {

			int bit = bitSet.nextSetBit(0);

			@Override
			public boolean hasNext() {
				return bit != -1;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				int ret = bit;
				bit = bitSet.nextSetBit(bit + 1);
				return ret;
			}
		};
	}

}
