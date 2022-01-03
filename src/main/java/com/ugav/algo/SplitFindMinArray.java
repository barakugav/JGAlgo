package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class SplitFindMinArray implements SplitFindMin {

	private SplitFindMinArray() {
	}

	private static final SplitFindMinArray INSTANCE = new SplitFindMinArray();

	public static SplitFindMinArray getInstace() {
		return INSTANCE;
	}

	@Override
	public <K, V> Element<K, V>[] make(Collection<K> keys, Collection<V> values, Comparator<? super K> c) {
		if (keys.size() != values.size())
			throw new IllegalArgumentException();

		@SuppressWarnings("unchecked")
		Elm<K, V>[] elms = new Elm[keys.size()];
		int i = 0;
		Iterator<V> e = values.iterator();
		for (Iterator<K> k = keys.iterator(); k.hasNext();)
			elms[i++] = new Elm<>(k.next(), e.next());
		c = c != null ? c : Utils.getDefaultComparator();

		Block<K, V> head = null, prev = null;
		for (int size = elms.length; size > 0;) {
			int blkSize = 1 << Integer.numberOfTrailingZeros(size);
			int from = prev == null ? 0 : prev.to;
			int to = from + blkSize;
			Block<K, V> blk = new Block<>(elms, from, to, c);
			if (head == null) {
				head = blk;
			} else {
				prev.next = blk;
				blk.prev = prev;
			}
			prev = blk;
			size -= blkSize;
		}
		@SuppressWarnings("unused")
		Sequence<K, V> seq = new Sequence<>(elms, head, c);

		@SuppressWarnings("unchecked")
		Element<K, V>[] res = new Element[elms.length];
		for (i = 0; i < elms.length; i++)
			res[i] = elms[i];
		return res;
	}

	@Override
	public <K, V> Element<K, V> find(Element<K, V> e) {
		Elm<K, V> elm = (Elm<K, V>) e;
		Sequence<K, V> seq = elm.block.seq;
		return seq.elms[seq.head.from];
	}

	@Override
	public <K, V> Pair<? extends Element<K, V>, ? extends Element<K, V>> split(Element<K, V> e) {
		Elm<K, V> elm = (Elm<K, V>) e;
		Block<K, V> blk = elm.block;
		Sequence<K, V> seq = blk.seq;
		Elm<K, V>[] elms = seq.elms;

		int elmIdx;
		for (elmIdx = blk.from; elmIdx < blk.to; elmIdx++)
			if (elms[elmIdx] == e)
				break;
		if (elmIdx >= blk.to)
			throw new InternalError();
		if (blk == seq.head && elmIdx == 0)
			return Pair.valueOf(null, elms[seq.head.from]);

		Block<K, V> head = seq.head, tail = blk.prev;
		if (tail != null)
			tail.next = null;
		for (int size = elmIdx - blk.from; size > 0;) {
			int blkSize = 1 << (31 - Integer.numberOfLeadingZeros(size));
			int from = tail == null ? blk.from : tail.to;
			int to = from + blkSize;
			Block<K, V> newBlk = new Block<>(elms, from, to, seq.c);
			if (tail != null) {
				tail.next = newBlk;
				newBlk.prev = tail;
			} else
				head = newBlk;
			tail = newBlk;
			size -= blkSize;
		}
		Sequence<K, V> seq1 = new Sequence<>(elms, head, seq.c);

		head = blk.next;
		if (head != null)
			head.prev = null;
		for (int size = blk.to - elmIdx; size > 0;) {
			int blkSize = 1 << (31 - Integer.numberOfLeadingZeros(size));
			int to = head == null ? blk.to : head.from;
			int from = to - blkSize;
			Block<K, V> newBlk = new Block<>(elms, from, to, seq.c);
			if (head != null) {
				head.prev = newBlk;
				newBlk.next = head;
			}
			head = newBlk;
			size -= blkSize;
		}
		Sequence<K, V> seq2 = new Sequence<>(elms, head, seq.c);

		blk.clear();
		seq.clear();

		return Pair.valueOf(elms[seq1.head.from], elms[seq2.head.from]);
	}

	@Override
	public <K, V> Element<K, V> findMin(Element<K, V> e) {
		Elm<K, V> elm = (Elm<K, V>) e;
		return elm.block.seq.min;
	}

	@Override
	public <K, V> void decreaseKey(Element<K, V> e, K newKey) {
		Elm<K, V> elm = (Elm<K, V>) e;
		Block<K, V> blk = elm.block;
		Sequence<K, V> seq = blk.seq;
		elm.key = newKey;

		if (elm == blk.min || seq.c.compare(newKey, blk.min.key) < 0) {
			blk.min = elm;
			if (seq.c.compare(newKey, seq.min.key) < 0)
				seq.min = elm;
		}
	}

	private static class Sequence<K, V> {

		Elm<K, V>[] elms;
		Block<K, V> head;
		Elm<K, V> min;
		final Comparator<? super K> c;

		Sequence(Elm<K, V>[] elms, Block<K, V> head, Comparator<? super K> c) {
			this.elms = elms;
			this.head = head;
			this.c = c;

			Elm<K, V> min = null;
			for (Block<K, V> blk = head; blk != null; blk = blk.next) {
				blk.seq = this;
				if (min == null || c.compare(min.key, blk.min.key) > 0)
					min = blk.min;
			}
			this.min = min;
		}

		void clear() {
			this.elms = null;
			head = null;
			min = null;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append('[');
			for (Block<K, V> blk = head; blk != null; blk = blk.next) {
				if (blk != head)
					s.append(", ");
				s.append(blk);
			}
			s.append(']');
			return s.toString();
		}

	}

	private static class Block<K, V> {

		Sequence<K, V> seq;
		final int from;
		final int to;
		Block<K, V> next;
		Block<K, V> prev;
		Elm<K, V> min;

		Block(Elm<K, V>[] elms, int from, int to, Comparator<? super K> c) {
			this.from = from;
			this.to = to;

			Elm<K, V> min = null;
			for (int i = from; i < to; i++) {
				Elm<K, V> elm = elms[i];
				elm.block = this;
				if (min == null || c.compare(min.key, elm.key) > 0)
					min = elm;
			}
			this.min = min;
		}

		void clear() {
			next = prev = null;
			min = null;
			seq = null;
		}

		@Override
		public String toString() {
			return "[" + from + ", " + to + "]";
		}

	}

	private static class Elm<K, V> implements SplitFindMin.Element<K, V> {

		K key;
		V val;

		Block<K, V> block;

		Elm(K k, V v) {
			key = k;
			val = v;
		}

		@Override
		public V val() {
			return val;
		}

		@Override
		public void val(V v) {
			val = v;
		}

		@Override
		public K key() {
			return key;
		}

		@Override
		public String toString() {
			return "<" + key + ", " + val + ">";
		}

	}

}
