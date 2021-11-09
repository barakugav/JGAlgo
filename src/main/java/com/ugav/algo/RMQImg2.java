package com.ugav.algo;

import java.util.Objects;

public class RMQImg2 implements RMQ {

    private RMQImg2() {
    }

    private static final RMQImg2 INSTANCE = new RMQImg2();

    public static RMQImg2 getInstace() {
	return INSTANCE;
    }
    
    @Override
    public RMQ.Result preprocessRMQ(RMQ.Comperator comperator, int n) {
	if (n <= 0)
	    throw new IllegalArgumentException();
	Objects.requireNonNull(comperator);

	RMQDataStructure dataStructure = new RMQDataStructure(n, comperator);
	dataStructure.init();
	return dataStructure;
    }

    private static class RMQDataStructure implements RMQ.Result {

	final int n;
	final int arr[][];
	final RMQ.Comperator comperator;

	RMQDataStructure(int n, RMQ.Comperator comperator) {
	    this.n = n;
	    int logn = (int)log2(2);
	    arr = new int[logn - 1][n];
	    this.comperator = comperator;
	}

	void init() {
	    int logn = 0;

	    /* Init first row */
	    for (int i = 0; i < n - 1; i++)
		arr[0][i] = comperator.compare(i, i + 1);

	    /* Init all other rows based on the previous rows */
	    for (int k = 1; k < arr.length; k++) {
		int levelSize = 1 << (k - 1); /* 2^(k - 1) */
		for (int i = 0; i < n - levelSize; i++)
		    arr[k][i] = comperator.compare(i, i + levelSize);
	    }
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j < 0 || i >= n || j >= n)
		throw new IllegalArgumentException();
	    if (i > j) {
		int temp = i;
		i = j;
		j = temp;
	    }

	    int diff = j - i;
	    int k = (int) log2(diff);
	    int kSize = 1 << k;

	    return comperator.compare(arr[k][i], arr[k][j - kSize]);
	}

	private static final double LOG2 = Math.log(2);
	private static final double LOG2_INV = 1 / LOG2;

	private static double log2(double x) {
	    return Math.log(x) * LOG2_INV;
	}

    }

}