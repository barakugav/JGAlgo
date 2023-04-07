package com.jgalgo;

public class GraphLinkedUndirected extends GraphLinkedAbstract implements UGraph {

	private final DataContainer.Obj<Node> edges;

	public GraphLinkedUndirected() {
		this(0);
	}

	public GraphLinkedUndirected(int n) {
		super(n, Capabilities);
		edges = new DataContainer.Obj<>(n, null);
		addInternalVerticesDataContainer(edges);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int tempV = -2;
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(v1);
			if (p.u == v1)
				p.u = tempV;
			if (p.v == v1)
				p.v = tempV;
		}
		for (Node p = edges.get(v2), next; p != null; p = next) {
			next = p.next(v2);
			if (p.u == v2)
				p.u = v1;
			if (p.v == v2)
				p.v = v1;
		}
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(tempV);
			if (p.u == tempV)
				p.u = v2;
			if (p.v == tempV)
				p.v = v2;
		}

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int u) {
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
		int n = vertices().size();
		for (int uIdx = 0; uIdx < n; uIdx++) {
			// TODO do some sort of 'addKey' instead of set, no need
			edges.set(uIdx, null);
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

	private static final GraphCapabilities Capabilities = new GraphCapabilities() {
		@Override
		public boolean vertexAdd() {
			return true;
		}

		@Override
		public boolean vertexRemove() {
			return true;
		}

		@Override
		public boolean edgeAdd() {
			return true;
		}

		@Override
		public boolean edgeRemove() {
			return true;
		}

		@Override
		public boolean parallelEdges() {
			return true;
		}

		@Override
		public boolean selfEdges() {
			return false;
		}

		@Override
		public boolean directed() {
			return false;
		}
	};

}
