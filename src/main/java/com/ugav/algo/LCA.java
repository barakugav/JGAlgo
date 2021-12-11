package com.ugav.algo;

public interface LCA {

	public <E> Result preprocessLCA(Graph<E> g, int r);

	public static interface Result {

		public int query(int u, int v);

	}

}
