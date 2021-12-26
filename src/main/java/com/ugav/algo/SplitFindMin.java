package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;

public interface SplitFindMin extends SplitFind {

	public <K, V> Element<K, V>[] make(Collection<K> keys, Collection<V> values, Comparator<? super K> c);

	public <K, V> Element<K, V> find(Element<K, V> e);

	public <K, V> Pair<? extends Element<K, V>, ? extends Element<K, V>> split(Element<K, V> e);

	public <K, V> Element<K, V> findMin(Element<K, V> e);

	public <K, V> void decreaseKey(Element<K, V> e, K newKey);

	public static interface Element<K, V> extends SplitFind.Element<V> {

		public K key();

	}

	@Override
	default <V> SplitFind.Element<V>[] make(Collection<V> elms) {
		return make(elms, elms, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> SplitFind.Element<V> find(SplitFind.Element<V> e) {
		return find((Element<?, V>) e);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V> Pair<? extends SplitFind.Element<V>, ? extends SplitFind.Element<V>> split(SplitFind.Element<V> e) {
		return split((Element<?, V>) e);
	}

}
