package com.jgalgo;

import java.util.Comparator;
import java.util.Objects;

class RMQComparatorArray {

	static class ObjDefaultCmp<E> implements RMQComparator {

		private final E[] arr;

		ObjDefaultCmp(E[] arr) {
			this.arr = Objects.requireNonNull(arr);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(int i, int j) {
			return ((Comparable) arr[i]).compareTo(arr[j]);
		}
	}

	static class ObjCustomCmp<E> implements RMQComparator {

		private final E[] arr;
		private final Comparator<? super E> c;

		ObjCustomCmp(E[] arr, Comparator<? super E> c) {
			this.arr = Objects.requireNonNull(arr);
			this.c = Objects.requireNonNull(c);
		}

		@Override
		public int compare(int i, int j) {
			return c.compare(arr[i], arr[j]);
		}
	}

	static class Int implements RMQComparator {

		private final int[] arr;

		Int(int[] arr) {
			this.arr = Objects.requireNonNull(arr);
		}

		@Override
		public int compare(int i, int j) {
			return Integer.compare(arr[i], arr[j]);
		}
	}

}
