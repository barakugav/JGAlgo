package com.ugav.algo;

import java.util.Collection;

public interface SplitFind {

	public <V> Elm<V>[] make(Collection<V> elms);

	public <V> Elm<V> find(Elm<V> e);

	public <V> Pair<? extends Elm<V>, ? extends Elm<V>> split(Elm<V> e);

	public static interface Elm<V> {

		public V val();

		public void val(V v);

	}

}
