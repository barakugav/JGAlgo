package com.ugav.algo;

public class UnionFindImpl implements UnionFind {

    UnionFindImpl() {
    }

    private static final UnionFindImpl INSTANCE = new UnionFindImpl();

    public static UnionFindImpl getInstance() {
	return INSTANCE;
    }

    @Override
    public <V> Element<V> make(V v) {
	return new Element<>(v);
    }

    @Override
    public <V> Element<V> find(UnionFind.Element<V> e0) {
	Element<V> e = (Element<V>) e0;

	/* Find root */
	Element<V> r;
	for (r = e; r.parent != null; r = r.parent)
	    ;

	/* path compression */
	for (; e != r;) {
	    Element<V> next = e.parent;
	    e.parent = r;
	    e = next;
	}

	return r;
    }

    @Override
    public <V> Element<V> union(UnionFind.Element<V> a0, UnionFind.Element<V> b0) {
	Element<V> a = find(a0);
	Element<V> b = find(b0);
	if (a == b)
	    return a;

	if (a.rank < b.rank) {
	    Element<V> t = a;
	    a = b;
	    b = t;
	} else if (a.rank == b.rank)
	    a.rank++;

	b.parent = a;
	return a;
    }

    static class Element<V> implements UnionFind.Element<V> {

	V v;
	Element<V> parent;
	byte rank;

	Element(V v) {
	    this.v = v;
	    parent = null;
	    rank = 0;
	}

	@Override
	public V get() {
	    return v;
	}

	@Override
	public void set(V v) {
	    this.v = v;
	}

    }

}
