package com.ugav.algo;

import java.util.Arrays;

public class GraphLinkedUndirected extends GraphLinkedAbstract implements UGraph {

	private Node[] edges;

	private static final Node[] EmptyNodeArr = new Node[0];

	public GraphLinkedUndirected() {
		this(0);
	}

	public GraphLinkedUndirected(int n) {
		super(n);
		edges = n != 0 ? new Node[n] : EmptyNodeArr;
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		if (v >= edges.length)
			edges = Arrays.copyOf(edges, Math.max(edges.length * 2, 2));
		return v;
	}

	@Override
	public EdgeIter edges(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItr(u, edges[u]);
	}

	@Override
	public int addEdge(int u, int v) {
		if (u == v)
			throw new IllegalArgumentException("self edges are not supported");
		Node e = (Node) addEdgeNode(u, v), next;
		if ((next = edges[u]) != null) {
			e.nextSet(u, next);
			next.prevSet(u, e);
		}
		edges[u] = e;
		if ((next = edges[v]) != null) {
			e.nextSet(v, next);
			next.prevSet(v, e);
		}
		edges[v] = e;
		return e.id;
	}

	@Override
	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int id) {
		Node e = (Node) removeEdgeNode(id);
		removeEdge0(e, e.u);
		removeEdge0(e, e.v);
	}

	@Override
	public void removeEdgesAll(int u) {
		checkVertexIdx(u);
		for (Node p = edges[u], next; p != null; p = next) {
			// update u list
			next = p.next(u);
			p.nextSet(u, null);
			p.prevSet(u, null);

			// update v list
			int v = p.getEndpoint(u);
			removeEdge0(p, v);

			removeEdgeNode(p.id);
		}
		edges[u] = null;
	}

	void removeEdge0(Node e, int w) {
		Node next = e.next(w), prev = e.prev(w);
		if (prev == null) {
			edges[w] = next;
		} else {
			prev.nextSet(w, next);
			e.prevSet(w, null);
		}
		if (next != null) {
			next.prevSet(w, prev);
			e.nextSet(w, null);
		}
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : Utils.iterable(nodes())) {
			Node p = (Node) p0;
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		Arrays.fill(edges, null);
		super.clearEdges();

	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextu, nextv;
		private Node prevu, prevv;

		Node(int id, int u, int v) {
			super(id, u, v);
		}

		Node next(int w) {
			assert w == u || w == v;
			return w == u ? nextu : nextv;
		}

		void nextSet(int w, Node n) {
			assert w == u || w == v;
			if (w == u)
				nextu = n;
			else
				nextv = n;
		}

		Node prev(int w) {
			assert w == u || w == v;
			return w == u ? prevu : prevv;
		}

		void prevSet(int w, Node n) {
			assert w == u || w == v;
			if (w == u)
				prevu = n;
			else
				prevv = n;
		}

		int getEndpoint(int w) {
			assert w == u || w == v;
			return w == u ? v : u;
		}

	}

	private class EdgeVertexItr extends GraphLinkedAbstract.EdgeItr {

		private final int u;

		EdgeVertexItr(int u, Node p) {
			super(p);
			this.u = u;
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).next(u);
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			int u0 = last.u, v0 = last.v;
			return u == u0 ? v0 : u0;
		}

	}

}
