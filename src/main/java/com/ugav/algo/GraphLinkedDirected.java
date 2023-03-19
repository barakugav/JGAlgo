package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class GraphLinkedDirected<E> implements Graph.Removeable.Directed<E> {
//	public class GraphLinkedDirected<E> extends GraphLinkedAbstractOld<E> implements Graph.Directed<E> {

	private int n, m;
	private Node<E>[] edgesIn;
	private Node<E>[] edgesOut;
	private final Int2ObjectMap<Node<E>> edges;
	private EdgeData<E> edgeData;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodeArr = new Node[0];

	public GraphLinkedDirected() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinkedDirected(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edgesIn = n != 0 ? new Node[n] : EmptyNodeArr;
		edgesOut = n != 0 ? new Node[n] : EmptyNodeArr;
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
		int v = n++;
		if (v >= edgesIn.length) {
			edgesIn = Arrays.copyOf(edgesIn, edgesIn.length * 2);
			edgesOut = Arrays.copyOf(edgesOut, edgesOut.length * 2);
		}
		return v;
	}

	@Override
	public EdgeIter<E> edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItrOut(edgesOut[u]);
	}

	@Override
	public EdgeIter<E> edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeVertexItrIn(edgesIn[v]);
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);

		int id = m++;
		Node<E> e = new Node<>(id, u, v), next;
		next = edgesOut[u];
		if (next != null) {
			next.prevOut = e;
			e.nextOut = next;
		}
		edgesOut[u] = e;
		next = edgesIn[v];
		if (next != null) {
			next.prevIn = e;
			e.nextIn = next;
		}
		edgesIn[v] = e;

		edges.put(id, e);
		return id;
	}

	@Override
	public void removeEdge(int id) {
		Node<E> e = edges.remove(id);
		if (e == null)
			throw new IndexOutOfBoundsException(id);
		removeEdgeOut(e);
		removeEdgeIn(e);
		m--;
	}

	@Override
	public void removeEdgesOut(int u) {
		int count = 0;
		for (Node<E> p = edgesOut[u], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeIn(p);
			edges.remove(p.id);
			count++;
		}
		edgesOut[u] = null;
		m -= count;
	}

	@Override
	public void removeEdgesIn(int v) {
		int count = 0;
		for (Node<E> p = edgesIn[v], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOut(p);
			count++;
		}
		edgesIn[v] = null;
		m -= count;
	}

	private void removeEdgeOut(Node<E> e) {
		Node<E> next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut[e.u] = next;
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
	}

	private void removeEdgeIn(Node<E> e) {
		Node<E> next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn[e.v] = next;
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
	}

	private void checkVertexIdx(int u) {
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
		for (Node<E> p : edges.values())
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		edges.clear();
		Arrays.fill(edgesOut, null);
		Arrays.fill(edgesIn, null);
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

	private abstract class EdgeVertexItr implements EdgeIter<E> {

		private Node<E> next, last;

		EdgeVertexItr(Node<E> p) {
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
		public int u() {
			return last.u;
		}

		@Override
		public int v() {
			return last.u;
		}

		@Override
		public E data() {
			return edgeData.get(last.id);
		}

		@Override
		public void setData(E val) {
			edgeData.set(last.id, val);
		}

	}

	private class EdgeVertexItrOut extends EdgeVertexItr {

		EdgeVertexItrOut(Node<E> p) {
			super(p);
		}

		@Override
		Node<E> nextNode(Node<E> n) {
			return n.nextOut;
		}

	}

	private class EdgeVertexItrIn extends EdgeVertexItr {

		EdgeVertexItrIn(Node<E> p) {
			super(p);
		}

		@Override
		Node<E> nextNode(Node<E> n) {
			return n.nextIn;
		}

	}

	private static class Node<E> {

		private final int id;
		private final int u, v;
		private Node<E> nextOut;
		private Node<E> nextIn;
		private Node<E> prevOut;
		private Node<E> prevIn;

		private Node(int id, int u, int v) {
			this.id = id;
			this.u = u;
			this.v = v;
		}

	}

}
