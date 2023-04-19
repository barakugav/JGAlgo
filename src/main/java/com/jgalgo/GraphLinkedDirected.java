package com.jgalgo;

public class GraphLinkedDirected extends GraphLinkedAbstract implements DiGraph {

	private final DataContainer.Obj<Node> edgesIn;
	private final DataContainer.Obj<Node> edgesOut;

	public GraphLinkedDirected() {
		this(0);
	}

	public GraphLinkedDirected(int n) {
		super(n, Capabilities);

		edgesIn = new DataContainer.Obj<>(n, null);
		edgesOut = new DataContainer.Obj<>(n, null);

		addInternalVerticesDataContainer(edgesIn);
		addInternalVerticesDataContainer(edgesOut);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		for (Node p = edgesOut.get(v1); p != null; p = p.nextOut)
			p.u = v2;
		for (Node p = edgesIn.get(v1); p != null; p = p.nextIn)
			p.v = v2;
		for (Node p = edgesOut.get(v2); p != null; p = p.nextOut)
			p.u = v1;
		for (Node p = edgesIn.get(v2); p != null; p = p.nextIn)
			p.v = v1;
		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeIterOut(edgesOut.get(u));
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeIterIn(edgesIn.get(v));
	}

	@Override
	public int addEdge(int u, int v) {
		Node e = (Node) addEdgeNode(u, v);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Node e) {
		int u = e.u, v = e.v;
		Node next;
		next = edgesOut.get(u);
		if (next != null) {
			next.prevOut = e;
			e.nextOut = next;
		}
		edgesOut.set(u, e);
		next = edgesIn.get(v);
		if (next != null) {
			next.prevIn = e;
			e.nextIn = next;
		}
		edgesIn.set(v, e);
	}

	@Override
	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int edge) {
		removeEdge(getNode(edge));
	}

	@Override
	void removeEdge(GraphLinkedAbstract.Node node) {
		Node n = (Node) node;
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		super.removeEdge(node);
	}

	@Override
	public void removeEdgesOutOf(int u) {
		checkVertexIdx(u);
		for (Node p = edgesOut.get(u), next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInNode(p);
			super.removeEdge(p.id);
		}
		edgesOut.set(u, null);
	}

	@Override
	public void removeEdgesInOf(int v) {
		checkVertexIdx(v);
		for (Node p = edgesIn.get(v), next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutNode(p);
			super.removeEdge(p.id);
		}
		edgesIn.set(v, null);
	}

	private void removeEdgeOutNode(Node e) {
		Node next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut.set(e.u, next);
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
	}

	private void removeEdgeInNode(Node e) {
		Node next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn.set(e.v, next);
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
	}

	@Override
	public void reverseEdge(int e) {
		Node n = (Node) getNode(e);
		if (n.u == n.v)
			return;
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		int w = n.u;
		n.u = n.v;
		n.v = w;
		addEdgeToLists(n);
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		int n = vertices().size();
		for (int uIdx = 0; uIdx < n; uIdx++) {
			// TODO do some sort of 'addKey' instead of set, no need
			edgesOut.set(uIdx, null);
			edgesIn.set(uIdx, null);
		}
		super.clearEdges();
	}

	private abstract class EdgeIterImpl extends GraphLinkedAbstract.EdgeItr {

		EdgeIterImpl(Node p) {
			super(p);
		}

		@Override
		public int u() {
			return last.u;
		}

		@Override
		public int v() {
			return last.v;
		}

	}

	private class EdgeIterOut extends EdgeIterImpl {

		EdgeIterOut(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextOut;
		}

	}

	private class EdgeIterIn extends EdgeIterImpl {

		EdgeIterIn(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextIn;
		}

	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextOut;
		private Node nextIn;
		private Node prevOut;
		private Node prevIn;

		Node(int id, int u, int v) {
			super(id, u, v);
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
			return true;
		}

		@Override
		public boolean directed() {
			return true;
		}
	};

}
