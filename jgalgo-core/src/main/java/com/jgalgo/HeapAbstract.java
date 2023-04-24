package com.jgalgo;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Comparator;

abstract class HeapAbstract<E> extends AbstractCollection<E> implements Heap<E> {

	final Comparator<? super E> c;

	HeapAbstract(Comparator<? super E> c) {
		this.c = c;
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
	public void meld(Heap<? extends E> h) {
		if (h == this)
			return;
		addAll(h);
		h.clear();
	}

	@Override
	public Comparator<? super E> comparator() {
		return c;
	}

	int compare(E e1, E e2) {
		return c == null ? Utils.cmpDefault(e1, e2) : c.compare(e1, e2);
	}

}
