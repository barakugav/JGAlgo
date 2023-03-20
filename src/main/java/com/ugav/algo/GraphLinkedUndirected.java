package com.ugav.algo;

import java.util.Arrays;

public class GraphLinkedUndirected extends GraphLinkedAbstract implements Graph.Removeable.Undirected {

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
	public int newVertex() {
		int v = super.newVertex();
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
		Node e = (Node) newEdgeNode(u, v);
		e.nextSet(u, edges[u]);
		edges[u] = e;
		e.nextSet(v, edges[v]);
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

	void removeEdge0(Node e, int w) {
		// TODO add prev pointers
		for (Node prev = null, p = edges[w]; p != null; p = (prev = p).next(w)) {
			if (p == e) {
				if (prev == null)
					edges[w] = p.next(w);
				else
					prev.nextSet(w, p.next(w));
				p.nextSet(w, null);
				return;
			}
		}
		throw new IllegalArgumentException("edge not in graph: " + e);
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : Utils.iterable(nodes())) {
			Node p = (Node) p0;
			p.nextu = p.nextv = null;
		}
		Arrays.fill(edges, null);
		super.clearEdges();

	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextu, nextv;

		Node(int id, int u, int v) {
			super(id, u, v);
		}

		Node next(int w) {
			return w == u ? nextu : nextv;
		}

		void nextSet(int w, Node n) {
			if (w == u)
				nextu = n;
			else
				nextv = n;
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
