package com.ugav.algo;

import java.util.Iterator;

interface IteratorInt { // TODO remove, use fastutil

	boolean hasNext();

	int next();

	default Iterator<Integer> asIteratorObj() {
		return new Iterator<>() {

			@Override
			public boolean hasNext() {
				return IteratorInt.this.hasNext();
			}

			@Override
			public Integer next() {
				return Integer.valueOf(IteratorInt.this.next());
			}
		};
	}

}
