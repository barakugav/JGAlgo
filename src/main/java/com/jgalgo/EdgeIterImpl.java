package com.jgalgo;

import java.util.NoSuchElementException;

class EdgeIterImpl {

	public static final EdgeIter Empty = new EdgeIter() {

		@Override
		public int nextInt() {
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public int u() {
			throw new NoSuchElementException();
		}

		@Override
		public int v() {
			throw new NoSuchElementException();
		}
	};

}
