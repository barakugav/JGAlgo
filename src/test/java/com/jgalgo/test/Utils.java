package com.jgalgo.test;

import java.util.Iterator;

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

}
