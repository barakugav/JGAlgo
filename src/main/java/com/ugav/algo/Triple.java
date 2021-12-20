package com.ugav.algo;

import java.util.Objects;

public class Triple<E1, E2, E3> implements Comparable<Triple<E1, E2, E3>> {

	public E1 e1;
	public E2 e2;
	public E3 e3;

	public Triple() {
	}

	public Triple(E1 e1, E2 e2, E3 e3) {
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
	}

	@Override
	public String toString() {
		return "<" + e1 + ", " + e2 + ", " + e3 + ">";
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(e1) * 961 + Objects.hashCode(e2) * 31 + Objects.hashCode(e3);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Triple))
			return false;

		Triple<?, ?, ?> o = (Triple<?, ?, ?>) other;
		return Objects.equals(e1, o.e1) && Objects.equals(e2, o.e2) && Objects.equals(e3, o.e3);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compareTo(Triple<E1, E2, E3> o) {
		int c;
		if ((c = ((Comparable) e1).compareTo(o.e1)) != 0)
			return c;
		if ((c = ((Comparable) e2).compareTo(o.e2)) != 0)
			return c;
		return ((Comparable) e2).compareTo(o.e2);
	}

}
