package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;

public abstract class HeapAbstract<E> extends AbstractCollection<E> implements Heap<E> {

	protected final Comparator<? super E> c;

	public HeapAbstract(Comparator<? super E> c) {
		this.c = c != null ? c : Utils.getDefaultComparator();
	}

	@Override
	public boolean add(E e) {
		insert(e);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Object e : c)
			if (remove(e))
				modified = true;
		return modified;
	}

	@Override
	public E extractMin() {
		E min = findMin();
		remove(min);
		return min;
	}

	@Override
	public void meld(Heap<? extends E> h) {
		if (h == this)
			return;
		addAll(h);
		h.clear();
	}

}
