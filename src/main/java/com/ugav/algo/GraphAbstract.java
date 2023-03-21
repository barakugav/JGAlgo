package com.ugav.algo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class GraphAbstract implements Graph {

	private int n, m;
	private final Map<Object, EdgesWeight<?>> edgeData = new Object2ObjectArrayMap<>();
	private final List<EdgeRenameListener> edgeRenameListeners = new CopyOnWriteArrayList<>();

	public GraphAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
	}

	@Override
	public int verticesNum() {
		return n;
	}

	@Override
	public int edgesNum() {
		return m;
	}

	@Override
	public int addVertex() {
		return n++;
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = m++;
		for (EdgesWeight<?> data : edgeData.values())
			data.edgeAdd(e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		checkEdgeIdx(e);
		int lastEdge = edgesNum() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		for (EdgesWeight<?> data : edgeData.values())
			data.edgeRemove(e);
		m--;
	}

	void edgeSwap(int e1, int e2) {
		for (EdgesWeight<?> data : edgeData.values())
			data.edgeSwap(e1, e2);
		for (EdgeRenameListener listener : edgeRenameListeners)
			listener.edgeRename(e1, e2);
	}

	@Override
	public void addEdgeRenameListener(EdgeRenameListener listener) {
		edgeRenameListeners.add(Objects.requireNonNull(listener));
	}

	@Override
	public void removeEdgeRenameListener(EdgeRenameListener listener) {
		edgeRenameListeners.remove(listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, EdgeDataT extends EdgesWeight<E>> EdgeDataT edgesWeight(Object key) {
		return (EdgeDataT) edgeData.get(key);
	}

	@Override
	public <E> EdgesWeight<E> newEdgeWeight(Object key) {
		return addEdgeData(key, new EdgesWeight.Obj<>(edgesNum()));
	}

	@Override
	public EdgesWeight.Int newEdgeWeightInt(Object key) {
		return addEdgeData(key, new EdgesWeight.Int(edgesNum()));
	}

	@Override
	public EdgesWeight.Double newEdgeWeightDouble(Object key) {
		return addEdgeData(key, new EdgesWeight.Double(edgesNum()));
	}

	private <E, T extends EdgesWeight<E>> T addEdgeData(Object key, T data) {
		if (edgeData.containsKey(key))
			throw new IllegalArgumentException();
		int m = edgesNum();
		for (int e = 0; e < m; e++)
			data.edgeAdd(e);
		edgeData.put(key, data);
		return data;
	}

	@Override
	public Collection<Object> getEdgeWeightKeys() {
		return Collections.unmodifiableCollection(edgeData.keySet());
	}

	@Override
	public void clear() {
		clearEdges();
		n = 0;
	}

	@Override
	public void clearEdges() {
		for (EdgesWeight<?> data : edgeData.values())
			data.clear();
		m = 0;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Graph))
			return false;
		Graph o = (Graph) other;

		if ((this instanceof DiGraph) != (o instanceof DiGraph))
			return false;
		if (verticesNum() != o.verticesNum() || edgesNum() != o.edgesNum())
			return false;
		int m = edgesNum();
		int[] es1 = new int[m];
		int[] es2 = new int[m];
		for (int e = 0; e < m; e++)
			es1[e] = es2[e] = e;

		final boolean directed = this instanceof DiGraph;
		BiFunction<Graph, Graph, IntComparator> cmpFactory = (g1, g2) -> {
			System.out.println();
			return null; // TODO
//			EdgeData<?> eData1 = g1.edgeData(), eData2 = g2.edgeData();
//			// TODO bug, not a full order
//			IntComparator dataCmp = (e1, e2) -> {
//				Object d1 = eData1.get(e1);
//				Object d2 = eData2.get(e2);
//				if (Objects.equals(d1, d2))
//					return 0;
//				int i1 = System.identityHashCode(d1);
//				int i2 = System.identityHashCode(d2);
//				int c = Integer.compare(i1, i2);
//				return c != 0 ? c : -1; // TODO bug, identityHashCode is not unique
//			};
//
//			if (directed) {
//				return (e1, e2) -> {
//					int c;
//					int u1 = g1.getEdgeSource(e1), u2 = g2.getEdgeSource(e2);
//					if ((c = Integer.compare(u1, u2)) != 0)
//						return c;
//					int v1 = g1.getEdgeTarget(e1), v2 = g2.getEdgeTarget(e2);
//					if ((c = Integer.compare(v1, v2)) != 0)
//						return c;
//					return dataCmp.compare(e1, e2);
//				};
//			} else {
//				return (e1, e2) -> {
//					int c;
//					int u1 = g1.getEdgeSource(e1), u2 = g2.getEdgeSource(e2);
//					int v1 = g1.getEdgeTarget(e1), v2 = g2.getEdgeTarget(e2);
//					if (u1 > v1) {
//						int temp = u1;
//						u1 = v1;
//						v1 = temp;
//					}
//					if (u2 > v2) {
//						int temp = u2;
//						u2 = v2;
//						v2 = temp;
//					}
//					if ((c = Integer.compare(u1, u2)) != 0)
//						return c;
//					if ((c = Integer.compare(v1, v2)) != 0)
//						return c;
//					return dataCmp.compare(e1, e2);
//				};
//			}
		};

		IntArrays.parallelQuickSort(es1, cmpFactory.apply(this, this));
		IntArrays.parallelQuickSort(es2, cmpFactory.apply(o, o));

		IntComparator cmp = cmpFactory.apply(this, o);
		for (int i = 0; i < m; i++)
			if (cmp.compare(es1[i], es2[i]) != 0)
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int h = 1, n = verticesNum();
//		EdgeData<E> edgeData = edgeData();
//		if (this instanceof Graph.Directed<?> g) {
//			for (int u = 0; u < n; u++) {
//				int uh = 1;
//				for (EdgeIter<?> eit = g.edgesOut(u); eit.hasNext();) {
//					int e = eit.nextInt();
//					int v = eit.v();
//					int eh = v * 31 + Objects.hashCode(edgeData.get(e));
//					uh = uh * 31 + eh;
//				}
//				h = h * 31 + uh;
//			}
//		} else {
//			for (int u = 0; u < n; u++) {
//				int uh = 1;
//				for (EdgeIter<?> eit = edges(u); eit.hasNext();) {
//					int e = eit.nextInt();
//					int v = eit.v();
//					if (u > v)
//						continue;
//					int eh = v * 31 + Objects.hashCode(edgeData.get(e));
//					uh = uh * 31 + eh;
//				}
//				h = h * 31 + uh;
//			}
//
//		} TODO
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = verticesNum();
//		EdgeData<E> edgeData = edgeData(); TODO

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex)
				firstVertex = false;
			else
				s.append(", ");
			s.append("<v" + u + ">->[");

			boolean firstEdge = true;
			for (EdgeIter eit = edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
//				Object d = edgeData.get(e);
				Object d = null;
				s.append("(" + u + ", " + v + ")" + (d != null ? "[" + d + "]" : ""));
			}
			s.append("]");
		}
		s.append('}');
		return s.toString();
	}

	void checkVertexIdx(int u) {
		if (u >= n)
			throw new IndexOutOfBoundsException(u);
	}

	void checkEdgeIdx(int e) {
		if (e >= m)
			throw new IndexOutOfBoundsException(e);
	}

}
