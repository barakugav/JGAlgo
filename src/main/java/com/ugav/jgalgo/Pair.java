package com.ugav.jgalgo;

import java.util.Objects;

public class Pair<E1, E2> implements Comparable<Pair<E1, E2>> {

	public E1 e1;
	public E2 e2;

	public Pair() {
	}

	public Pair(E1 e1, E2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	public String toString() {
		return "<" + e1 + ", " + e2 + ">";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(e1) ^ ~Objects.hashCode(e2);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Pair))
			return false;

		Pair<?, ?> o = (Pair<?, ?>) other;
		return Objects.equals(e1, o.e1) && Objects.equals(e2, o.e2);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compareTo(Pair<E1, E2> o) {
		int c;
		return (c = ((Comparable) e1).compareTo(o.e1)) != 0 ? c : ((Comparable) e2).compareTo(o.e2);
	}

	public static <E1, E2> Pair<E1, E2> of(E1 e1, E2 e2) {
		return new Pair<>(e1, e2);
	}

}
