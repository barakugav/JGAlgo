package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;

public interface SplitFindMin extends SplitFind {

	public <K, V> Elm<K, V>[] make(Collection<K> keys, Collection<V> values, Comparator<? super K> c);

	public <K, V> Elm<K, V> find(Elm<K, V> e);

	public <K, V> Pair<? extends Elm<K, V>, ? extends Elm<K, V>> split(Elm<K, V> e);

	public <K, V> Elm<K, V> findMin(Elm<K, V> e);

	public <K, V> void decreaseKey(Elm<K, V> e, K newKey);

	public static interface Elm<K, V> extends SplitFind.Elm<V> {

		public K key();

	}

	@Override
	default <V> SplitFind.Elm<V>[] make(Collection<V> elms) {
		return make(elms, elms, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> SplitFind.Elm<V> find(SplitFind.Elm<V> e) {
		return find((Elm<?, V>) e);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> Pair<? extends SplitFind.Elm<V>, ? extends SplitFind.Elm<V>> split(SplitFind.Elm<V> e) {
		return split((Elm<?, V>) e);
	}

}
