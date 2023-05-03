package com.jgalgo;

import java.util.NoSuchElementException;

interface EdgeIterImpl extends EdgeIter, Utils.IterPeekable.Int {

	static final EdgeIterImpl Empty = new EdgeIterImpl() {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public int nextInt() {
			throw new NoSuchElementException();
		}

		@Override
		public int peekNext() {
			throw new NoSuchElementException();
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
