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
    public RMQ.Result preprocessRMQ(RMQ.Comparator c, int n) {
	if (n <= 0)
	    throw new IllegalArgumentException();
	Objects.requireNonNull(c);

	LookupTable table;
	if (n == 1)
	    return SingleElementResult.INSTANCE;
	if (n <= LookupTable8.LIMIT)
	    table = new LookupTable8(n);
	else if (n <= LookupTable128.LIMIT)
	    table = new LookupTable128(n);
	else if (n <= LookupTable32768.LIMIT)
	    table = new LookupTable32768(n);
	else
	    throw new IllegalArgumentException("N too big (" + n + ")");

	for (int i = 0; i < n - 1; i++)
	    table.set(indexOf(n, i, i + 1), c.compare(i, i + 1) < 0 ? i : i + 1);
	for (int i = 0; i < n - 2; i++) {
	    for (int j = i + 2; j < n; j++) {
		int m = table.get(indexOf(n, i, j - 1));
		table.set(indexOf(n, i, j), c.compare(m, j) < 0 ? m : j);
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

    private static abstract class LookupTable implements RMQ.Result {

	final int n;

	LookupTable(int n) {
	    this.n = n;
	}

	static int arrSize(int n) {
	    return n * (n - 1) / 2;
	}

	abstract int get(int idx);

	abstract void set(int idx, int x);
    }

    private static class LookupTable8 extends LookupTable {

	final byte arr[];

	private static final int LIMIT = 1 << ((Byte.SIZE - 1) / 2);

	LookupTable8(int n) {
	    super(n);
	    arr = new byte[arrSize(n)];
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException();
	    if (i + 1 == j)
		return i;

	    return arr[indexOf(n, i, j - 1)];
	}

	@Override
	int get(int idx) {
	    return arr[idx];
	}

	@Override
	void set(int idx, int x) {
	    arr[idx] = (byte) x;
	}

    }

    private static class LookupTable128 extends LookupTable {

	final short arr[];

	private static final int LIMIT = 1 << ((Short.SIZE - 1) / 2);

	LookupTable128(int n) {
	    super(n);
	    arr = new short[arrSize(n)];
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException();
	    if (i + 1 == j)
		return i;

	    return arr[indexOf(n, i, j - 1)];
	}

	@Override
	int get(int idx) {
	    return arr[idx];
	}

	@Override
	void set(int idx, int x) {
	    arr[idx] = (short) x;
	}

    }

    private static class LookupTable32768 extends LookupTable {

	final int arr[];

	private static final int LIMIT = 1 << ((Integer.SIZE - 1) / 2);

	LookupTable32768(int n) {
	    super(n);
	    arr = new int[arrSize(n)];
	}

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException();
	    if (i + 1 == j)
		return i;

	    return arr[indexOf(n, i, j - 1)];
	}

	@Override
	int get(int idx) {
	    return arr[idx];
	}

	@Override
	void set(int idx, int x) {
	    arr[idx] = x;
	}

    }

}