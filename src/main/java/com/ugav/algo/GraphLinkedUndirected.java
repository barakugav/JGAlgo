package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphLinkedUndirected extends GraphLinkedAbstract implements UGraph {

	private final Weights<Node> edges;

	public GraphLinkedUndirected() {
		this(0);
	}

	public GraphLinkedUndirected(int n) {
		/* We use 'edges' to maintain the current vertices in the graph */
		edges = new VerticesWeights.Builder(this, null).ofObjs(null);
		IDStrategy vIDStrategy = getVerticesIDStrategy();
		WeightsAbstract<Node> verticesSet = (WeightsAbstract<Node>) edges;
		verticesSet.forceAdd = true;
		for (int i = 0; i < n; i++) {
			int u = vIDStrategy.nextID(i);
			verticesSet.keyAdd(u);
		}
		addInternalVerticesWeight(edges, false);
	}

	@Override
	public IntSet vertices() {
		return ((WeightsAbstract<Node>) edges).keysSet();
	}

	@Override
	public EdgeIter edges(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItr(u, edges.get(u));
	}

	@Override
	public int addEdge(int u, int v) {
		if (u == v)
			throw new IllegalArgumentException("self edges are not supported");
		Node e = (Node) addEdgeNode(u, v), next;
		if ((next = edges.get(u)) != null) {
			e.nextSet(u, next);
			next.prevSet(u, e);
		}
		edges.set(u, e);
		if ((next = edges.get(v)) != null) {
			e.nextSet(v, next);
			next.prevSet(v, e);
		}
		edges.set(v, e);
		return e.id;
	}

	@Override
	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int edge) {
		Node n = (Node) getNode(edge);
		super.removeEdge(edge);
		removeEdge0(n, n.u);
		removeEdge0(n, n.v);
	}

	@Override
	public void removeEdgesAll(int u) {
		checkVertexIdx(u);
		for (Node p = edges.get(u), next; p != null; p = next) {
			// update u list
			next = p.next(u);
			p.nextSet(u, null);
			p.prevSet(u, null);

			// update v list
			int v = p.getEndpoint(u);
			removeEdge0(p, v);

			super.removeEdge(p.id);
		}
		edges.set(u, null);
	}

	void removeEdge0(Node e, int w) {
		Node next = e.next(w), prev = e.prev(w);
		if (prev == null) {
			edges.set(w, next);
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
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		for (IntIterator it = vertices().iterator(); it.hasNext();) {
			int u = it.nextInt();
			// TODO do some sort of 'addKey' instead of set, no need
			edges.set(u, null);
		}
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
