package com.ugav.algo;

import java.util.Objects;

public interface RMQ {

    public Result preprocessRMQ(Comparator c, int n);

    @FunctionalInterface
    public static interface Comparator {

	public int compare(int i, int j);

    }

    public static interface Result {

	public int query(int i, int j);

    }

    public static class ArrayComparator<E> implements Comparator {

	private final E[] arr;
	private final java.util.Comparator<? super E> c;

	public ArrayComparator(E[] arr, java.util.Comparator<? super E> c) {
	    this.arr = Objects.requireNonNull(arr);
	    this.c = Objects.requireNonNull(c);
	}

	@Override
	public int compare(int i, int j) {
	    return c.compare(arr[i], arr[j]);
	}

    }

    public static class IntArrayComparator implements Comparator {

	private final int[] arr;

	public IntArrayComparator(int[] arr) {
	    this.arr = Objects.requireNonNull(arr);
	}

	@Override
	public int compare(int i, int j) {
	    return Integer.compare(arr[i], arr[j]);
	}

    }

}
