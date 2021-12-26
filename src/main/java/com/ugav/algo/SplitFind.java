package com.ugav.algo;

import java.util.Collection;

public interface SplitFind {

	public <V> Element<V>[] make(Collection<V> elms);

	public <V> Element<V> find(Element<V> e);

	public <V> Pair<? extends Element<V>, ? extends Element<V>> split(Element<V> e);

	public static interface Element<V> {

		public V val();

		public void val(V v);

	}

}
