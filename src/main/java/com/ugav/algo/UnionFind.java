package com.ugav.algo;

public interface UnionFind {

	public <V> Elm<V> make(V v);

	public <V> Elm<V> find(Elm<V> e);

	public <V> Elm<V> union(Elm<V> a, Elm<V> b);

	public static interface Elm<V> {

		public V get();

		public void set(V v);

	}

}
