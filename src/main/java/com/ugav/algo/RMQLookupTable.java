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

	LookupTable dataStructure = new LookupTable(n);
	dataStructure.init(comperator);
	return dataStructure;
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
	    if (n < 1)
		throw new IllegalArgumentException();
	    this.n = n;

	    int arrSize = n * (n - 1) / 2;
	    arr = new int[arrSize];
	}

	int indexOf(int i, int j) {
	    return (2 * n - i - 1) * i / 2 + j - i - 1;
	}

	void init(RMQ.Comperator comperator) {
	    for (int i = 0; i < n - 1; i++)
		arr[indexOf(i, i + 1)] = comperator.compare(i, i + 1) < 0 ? i : i + 1;
	    for (int i = 0; i < n - 2; i++) {
		for (int j = i + 2; j < n; j++) {
		    int m = arr[indexOf(i, j - 1)];
		    arr[indexOf(i, j)] = comperator.compare(m, j) < 0 ? m : j;
		}
	    }
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException();
	    if (i + 1 == j)
		return i;

	    return arr[indexOf(i, j - 1)];
	}

    }

}