package com.ugav.algo;

public interface UnionFind {

	public <V> Element<V> make(V v);

	public <V> Element<V> find(Element<V> e);

	public <V> Element<V> union(Element<V> a, Element<V> b);

	public static interface Element<V> {

		public V get();

		public void set(V v);

	}

}
