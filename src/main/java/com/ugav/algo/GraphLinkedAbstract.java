package com.ugav.algo;

import java.util.Iterator;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

abstract class GraphLinkedAbstract extends GraphAbstract implements Graph.Removeable {

	private int n;
	private int m;
	private final Int2ObjectMap<Node> edges;
	private int nextEdgeID;

	GraphLinkedAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edges = new Int2ObjectOpenHashMap<>(n);
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public int edges() {
		return m;
	}

	@Override
	public IntIterator edgesIDs() {
		return edges.keySet().intIterator();
	}

	@Override
	public int getEdgeSource(int edge) {
		Node node = edges.get(edge);
		if (node == null)
			throw new IndexOutOfBoundsException(edge);
		return node.u;
	}

	@Override
	public int getEdgeTarget(int edge) {
		Node node = edges.get(edge);
		if (node == null)
			throw new IndexOutOfBoundsException(edge);
		return node.v;
	}

	@Override
	public int newVertex() {
		return n++;
	}

	Node newEdgeNode(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);

		int id = nextEdgeID++;
		Node n = allocNode(id, u, v);
		edges.put(id, n);
		m++;
		return n;
	}

	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	Iterator<Node> nodes() {
		return edges.values().iterator();
	}

	public Node removeEdgeNode(int id) {
		Node e = edges.remove(id);
		if (e == null)
			throw new IndexOutOfBoundsException(id);
		m--;
		return e;
	}

	void checkVertexIdx(int u) {
		if (u >= n)
			throw new IndexOutOfBoundsException(u);
	}

	@Override
	public void clear() {
		clearEdges();
		n = 0;
		super.clear();
	}

	@Override
	public void clearEdges() {
		edges.clear();
		m = 0;
		super.clearEdges();
	}

	void checkVertexIdentifier(int v) {
		if (!(0 <= v && v < n))
			throw new IllegalArgumentException("Illegal vertex identifier");
	}

	abstract class EdgeItr implements Graph.EdgeIter {

		private Node next;
		Node last;

		EdgeItr(Node p) {
			this.next = p;
		}

		abstract Node nextNode(Node n);

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			next = nextNode(last = next);
			return last.id;
		}

	}

	static class Node {

		final int id;
		final int u, v;

		Node(int id, int u, int v) {
			this.id = id;
			this.u = u;
			this.v = v;
		}

	}

}
