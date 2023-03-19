package com.ugav.algo;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

abstract class GraphLinkedAbstract<E> extends GraphAbstract<E> implements Graph.Removeable<E> {

	private int n;
	private int m;
	private final Int2ObjectMap<Node<E>> edges;
	private int nextEdgeID = 1;
	private EdgeData<E> edgeData;

	GraphLinkedAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edges = new Int2ObjectOpenHashMap<>(n);
		edgeData = new EdgeDataMap.Obj<>(n);
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
		Node<E> node = edges.get(edge);
		if (node == null)
			throw new IndexOutOfBoundsException(edge);
		return node.u;
	}

	@Override
	public int getEdgeTarget(int edge) {
		Node<E> node = edges.get(edge);
		if (node == null)
			throw new IndexOutOfBoundsException(edge);
		return node.v;
	}

	@Override
	public int newVertex() {
		return n++;
	}

	Node<E> newEdgeNode(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);

		int id = nextEdgeID++;
		Node<E> n = allocNode(id, u, v);
		edges.put(id, n);
		return n;
	}

	Node<E> allocNode(int id, int u, int v) {
		return new Node<>(id, u, v);
	}

	Iterator<Node<E>> nodes() {
		return edges.values().iterator();
	}

	public Node<E> removeEdgeNode(int id) {
		Node<E> e = edges.remove(id);
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
	}

	@Override
	public void clearEdges() {
		edges.clear();
		edgeData.clear();
		m = 0;
	}

	@Override
	public EdgeData<E> edgeData() {
		return edgeData;
	}

	@Override
	public void setEdgesData(EdgeData<E> data) {
		edgeData = Objects.requireNonNull(data);
	}

	void checkVertexIdentifier(int v) {
		if (!(0 <= v && v < n))
			throw new IllegalArgumentException("Illegal vertex identifier");
	}

	abstract class EdgeItr implements Graph.EdgeIter<E> {

		private Node<E> next;
		Node<E> last;

		EdgeItr(Node<E> p) {
			this.next = p;
		}

		abstract Node<E> nextNode(Node<E> n);

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

		@Override
		public E data() {
			return edgeData().get(last.id);
		}

		@Override
		public void setData(E val) {
			edgeData().set(last.id, val);
		}

	}

	static class Node<E> {

		final int id;
		final int u, v;

		Node(int id, int u, int v) {
			this.id = id;
			this.u = u;
			this.v = v;
		}

	}

}
