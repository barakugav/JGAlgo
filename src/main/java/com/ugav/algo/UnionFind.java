package com.ugav.algo;

public interface UnionFind {

    <V> Element<V> make(V v);

    <V> Element<V> find(Element<V> e);

    <V> Element<V> union(Element<V> a, Element<V> b);

    static interface Element<V> {

	V get();

	void set(V v);

    }

}
