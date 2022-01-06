package com.ugav.algo;

public interface UnionFind {

	public int make();

	public int find(int x);

	public int union(int a, int b);

	public int size();

	public void clear();

}
