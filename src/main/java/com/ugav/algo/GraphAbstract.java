package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class GraphAbstract implements Graph {

	private int n, m;
	private final Map<Object, GraphWeights<?>> edgeData = new Object2ObjectArrayMap<>();
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
	public int addVertex() {
		return n++;
	}

	@Override
	public int edgesNum() {
		return m;
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = m++;
		for (GraphWeights<?> data : edgeData.values())
			data.keyAdd(e);
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
		for (GraphWeights<?> data : edgeData.values())
			data.keyRemove(e);
		m--;
	}

	void edgeSwap(int e1, int e2) {
		for (GraphWeights<?> data : edgeData.values())
			data.keySwap(e1, e2);
		for (EdgeRenameListener listener : edgeRenameListeners)
			listener.edgeRename(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		n = 0;
	}

	@Override
	public void clearEdges() {
		for (GraphWeights<?> data : edgeData.values())
			data.clear();
		m = 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, GraphWeightsT extends GraphWeights<E>> GraphWeightsT edgesWeight(Object key) {
		return (GraphWeightsT) edgeData.get(key);
	}

	@Override
	public <E> GraphWeights<E> newEdgeWeight(Object key) {
		return addEdgeData(key, new GraphWeights.Obj<>(edgesNum()));
	}

	@Override
	public GraphWeights.Int newEdgeWeightInt(Object key) {
		return addEdgeData(key, new GraphWeights.Int(edgesNum()));
	}

	@Override
	public GraphWeights.Double newEdgeWeightDouble(Object key) {
		return addEdgeData(key, new GraphWeights.Double(edgesNum()));
	}

	private <E, GraphWeightsT extends GraphWeights<E>> GraphWeightsT addEdgeData(Object key, GraphWeightsT weights) {
		if (edgeData.containsKey(key))
			throw new IllegalArgumentException();
		int m = edgesNum();
		for (int e = 0; e < m; e++)
			weights.keyAdd(e);
		edgeData.put(key, weights);
		return weights;
	}

	@Override
	public Set<Object> getEdgeWeightKeys() {
		return Collections.unmodifiableSet(edgeData.keySet());
	}

	@Override
	public Collection<GraphWeights<?>> getEdgeWeights() {
		return Collections.unmodifiableCollection(edgeData.values());
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

		Set<Object> ewKeys = getEdgeWeightKeys();
		if (!ewKeys.equals(o.getEdgeWeightKeys()))
			return false;
		List<Object> ewKeysObj = new ArrayList<>(0);
		for (Object weightKey : ewKeys) {
			GraphWeights<?> ew1 = edgesWeight(weightKey);
			GraphWeights<?> ew2 = o.edgesWeight(weightKey);
			if (ew1 instanceof GraphWeights.Int) {
				if (!(ew2 instanceof GraphWeights.Int))
					return false;
				continue;
			}
			if (ew1 instanceof GraphWeights.Double) {
				if (!(ew2 instanceof GraphWeights.Double))
					return false;
				continue;
			}
			if (ew1 instanceof GraphWeights.Obj<?> ew1Obj) {
				if (!(ew2 instanceof GraphWeights.Obj<?>))
					return false;
				if (ew1Obj.isComparable() && ((GraphWeights.Obj<?>) ew2).isComparable()) {
					continue;
				}
			}
			/* else */
			ewKeysObj.add(weightKey);
		}

		int m = edgesNum();
		int[] es1 = new int[m];
		int[] es2 = new int[m];
		for (int e = 0; e < m; e++)
			es1[e] = es2[e] = e;

		IntArrays.parallelQuickSort(es1, createEdgeComparator(this, this));
		IntArrays.parallelQuickSort(es2, createEdgeComparator(o, o));

		List<GraphWeights<?>> ewObj1 = new ArrayList<>(0);
		List<GraphWeights<?>> ewObj2 = new ArrayList<>(0);
		for (Object weightKey : ewKeysObj) {
			ewObj1.add(edgesWeight(weightKey));
			ewObj2.add(o.edgesWeight(weightKey));
		}
		IntComparator cmp = createEdgeComparator(this, o);
		for (int e = 0; e < m; e++) {
			int e1 = es1[e], e2 = es2[e];
			if (cmp.compare(e1, e2) != 0)
				return false;
			for (int i = 0; i < ewObj1.size(); i++)
				if (!Objects.equals(ewObj1.get(i).get(e1), ewObj2.get(i).get(e2)))
					return false;
		}
		return true;
	}

	private static IntComparator createEdgeComparator(Graph g1, Graph g2) {
		boolean directed = g1 instanceof DiGraph;
		assert directed != (g2 instanceof DiGraph);

		Set<Object> ewKeys = g1.getEdgeWeightKeys();
		assert ewKeys.equals(g2.getEdgeWeightKeys());
		List<Object> ewKeysInt = new ArrayList<>(0);
		List<Object> ewKeysDouble = new ArrayList<>(0);
		List<Object> ewKeysComparable = new ArrayList<>(0);
		for (Object weightKey : ewKeys) {
			GraphWeights<?> ew1 = g1.edgesWeight(weightKey);
			GraphWeights<?> ew2 = g2.edgesWeight(weightKey);
			if (ew1 instanceof GraphWeights.Int) {
				assert ew2 instanceof GraphWeights.Int;
				ewKeysInt.add(weightKey);

			} else if (ew1 instanceof GraphWeights.Double) {
				assert ew2 instanceof GraphWeights.Double;
				ewKeysDouble.add(weightKey);

			} else if (ew1 instanceof GraphWeights.Obj<?> ew1Obj) {
				assert ew2 instanceof GraphWeights.Obj<?>;
				if (ew1Obj.isComparable()) {
					assert ((GraphWeights.Obj<?>) ew2).isComparable();
					ewKeysComparable.add(weightKey);
				}
			}
		}

		List<GraphWeights.Int> ewInt1 = new ArrayList<>(0);
		List<GraphWeights.Int> ewInt2 = new ArrayList<>(0);
		List<GraphWeights.Double> ewDouble1 = new ArrayList<>(0);
		List<GraphWeights.Double> ewDouble2 = new ArrayList<>(0);
		List<GraphWeights.Obj<Comparable<?>>> ewComparable1 = new ArrayList<>(0);
		List<GraphWeights.Obj<Comparable<?>>> ewComparable2 = new ArrayList<>(0);
		for (Object weightKey : ewKeysInt) {
			ewInt1.add(g1.edgesWeight(weightKey));
			ewInt2.add(g2.edgesWeight(weightKey));
		}
		for (Object weightKey : ewKeysDouble) {
			ewDouble1.add(g1.edgesWeight(weightKey));
			ewDouble2.add(g2.edgesWeight(weightKey));
		}
		for (Object weightKey : ewKeysComparable) {
			ewComparable1.add(g1.edgesWeight(weightKey));
			ewComparable2.add(g2.edgesWeight(weightKey));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		IntComparator dataCmp = (e1, e2) -> {
			int c;
			for (int i = 0; i < ewInt1.size(); i++)
				if ((c = Integer.compare(ewInt1.get(i).getInt(e1), ewInt2.get(i).getInt(e2))) != 0)
					return c;
			for (int i = 0; i < ewDouble1.size(); i++)
				if ((c = Double.compare(ewDouble1.get(i).getDouble(e1), ewDouble2.get(i).getDouble(e2))) != 0)
					return c;
			for (int i = 0; i < ewComparable1.size(); i++) {
				Comparable o1 = ewComparable1.get(i).get(e1);
				Comparable o2 = ewComparable2.get(i).get(e2);
				if ((c = o1.compareTo(o2)) != 0)
					return c;
			}
			return 0;
		};

		if (directed) {
			return (e1, e2) -> {
				int c;
				int u1 = g1.edgeSource(e1), u2 = g2.edgeSource(e2);
				if ((c = Integer.compare(u1, u2)) != 0)
					return c;
				int v1 = g1.edgeTarget(e1), v2 = g2.edgeTarget(e2);
				if ((c = Integer.compare(v1, v2)) != 0)
					return c;
				return dataCmp.compare(e1, e2);
			};
		} else {
			return (e1, e2) -> {
				int u1 = g1.edgeSource(e1), u2 = g2.edgeSource(e2);
				int v1 = g1.edgeTarget(e1), v2 = g2.edgeTarget(e2);
				if (u1 > v1) {
					int temp = u1;
					u1 = v1;
					v1 = temp;
				}
				if (u2 > v2) {
					int temp = u2;
					u2 = v2;
					v2 = temp;
				}
				int c;
				if ((c = Integer.compare(u1, u2)) != 0)
					return c;
				if ((c = Integer.compare(v1, v2)) != 0)
					return c;
				return dataCmp.compare(e1, e2);
			};
		}
	}

	@Override
	public int hashCode() {
		int m = edgesNum();
		int[] es = new int[m];
		for (int e = 0; e < m; e++)
			es[e] = e;
		IntArrays.parallelQuickSort(es, createEdgeComparator(this, this));

		Collection<GraphWeights<?>> weights = getEdgeWeights();

		int h = 1 + verticesNum();
		for (int eIdx = 0; eIdx < m; eIdx++) {
			int e = es[eIdx];
			for (GraphWeights<?> weight : weights)
				h = h * 31 + Objects.hashCode(weight.get(e));
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = verticesNum();
		Collection<GraphWeights<?>> weights = getEdgeWeights();

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append("<v" + u + ">->[");

			boolean firstEdge = true;
			for (EdgeIter eit = edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append("(" + u + ", " + v + ")");
				if (!weights.isEmpty()) {
					s.append('[');
					boolean firstData = true;
					for (GraphWeights<?> weight : weights) {
						if (firstData) {
							firstData = false;
						} else {
							s.append(", ");
						}
						s.append(String.valueOf(weight.get(e)));
					}
					s.append(']');
				}
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
