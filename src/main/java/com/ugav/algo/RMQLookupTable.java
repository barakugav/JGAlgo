package com.ugav.algo;

import java.util.Objects;

public class RMQLookupTable implements RMQ {

    private RMQLookupTable() {
    }

    private static final RMQLookupTable INSTANCE = new RMQLookupTable();

    public static RMQLookupTable getInstace() {
	return INSTANCE;
    }

    @Override
    public RMQ.Result preprocessRMQ(RMQ.Comperator comperator, int n) {
	if (n <= 0)
	    throw new IllegalArgumentException();
	Objects.requireNonNull(comperator);

	if (n == 1)
	    return SingleElementResult.INSTANCE;

	LookupTable table = new LookupTable(n);

	for (int i = 0; i < n - 1; i++)
	    table.arr[indexOf(n, i, i + 1)] = comperator.compare(i, i + 1) < 0 ? i : i + 1;
	for (int i = 0; i < n - 2; i++) {
	    for (int j = i + 2; j < n; j++) {
		int m = table.arr[indexOf(n, i, j - 1)];
		table.arr[indexOf(n, i, j)] = comperator.compare(m, j) < 0 ? m : j;
	    }
	}

	return table;
    }

    private static int indexOf(int n, int i, int j) {
	return (2 * n - i - 1) * i / 2 + j - i - 1;
    }

    private static class SingleElementResult implements RMQ.Result {

	static SingleElementResult INSTANCE = new SingleElementResult();

	@Override
	public int query(int i, int j) {
	    if (i != 0 || j != 1)
		throw new IllegalArgumentException();
	    return 0;
	}

    }

    private static class LookupTable implements RMQ.Result {

	final int n;
	final int arr[];

	LookupTable(int n) {
	    this.n = n;
	    arr = new int[n * (n - 1) / 2];
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException();
	    if (i + 1 == j)
		return i;

	    return arr[indexOf(n, i, j - 1)];
	}

    }

}