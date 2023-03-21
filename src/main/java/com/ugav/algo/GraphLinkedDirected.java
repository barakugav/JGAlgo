package com.ugav.algo;

import java.util.Arrays;

public class GraphLinkedDirected extends GraphLinkedAbstract implements Graph.Directed {

	private Node[] edgesIn;
	private Node[] edgesOut;

	private static final Node[] EmptyNodeArr = new Node[0];

	public GraphLinkedDirected() {
		this(0);
	}

	public GraphLinkedDirected(int n) {
		super(n);
		edgesIn = n != 0 ? new Node[n] : EmptyNodeArr;
		edgesOut = n != 0 ? new Node[n] : EmptyNodeArr;
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (v >= edgesIn.length) {
			edgesIn = Arrays.copyOf(edgesIn, Math.max(edgesIn.length * 2, 2));
			edgesOut = Arrays.copyOf(edgesOut, Math.max(edgesOut.length * 2, 2));
		}
		return v;
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItrOut(edgesOut[u]);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeVertexItrIn(edgesIn[v]);
	}

	@Override
	public int addEdge(int u, int v) {
		Node e = (Node) addEdgeNode(u, v), next;
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

		return e.id;
	}

	@Override
	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int e) {
		Node n = (Node) removeEdgeNode(e);
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
	}

	@Override
	public void removeEdgesOut(int u) {
		checkVertexIdx(u);
		for (Node p = edgesOut[u], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInNode(p);
			removeEdgeNode(p.id);
		}
		edgesOut[u] = null;
	}

	@Override
	public void removeEdgesIn(int v) {
		checkVertexIdx(v);
		for (Node p = edgesIn[v], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutNode(p);
			removeEdgeNode(p.id);
		}
		edgesIn[v] = null;
	}

	private void removeEdgeOutNode(Node e) {
		Node next = e.nextOut, prev = e.prevOut;
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

	private void removeEdgeInNode(Node e) {
		Node next = e.nextIn, prev = e.prevIn;
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

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : Utils.iterable(nodes())) {
			Node p = (Node) p0;
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		Arrays.fill(edgesOut, null);
		Arrays.fill(edgesIn, null);
		super.clearEdges();
	}

	private abstract class EdgeVertexItr extends GraphLinkedAbstract.EdgeItr {

		EdgeVertexItr(Node p) {
			super(p);
		}

		@Override
		public int u() {
			return last.u;
		}

		@Override
		public int v() {
			return last.u;
		}

	}

	private class EdgeVertexItrOut extends EdgeVertexItr {

		EdgeVertexItrOut(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextOut;
		}

	}

	private class EdgeVertexItrIn extends EdgeVertexItr {

		EdgeVertexItrIn(Node p) {
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

}
