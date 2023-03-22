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
	private final Map<Object, GraphWeights<?>> eWeights = new Object2ObjectArrayMap<>();
	private final Map<Object, GraphWeights<?>> vWeights = new Object2ObjectArrayMap<>();
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
		int u = n++;
		for (GraphWeights<?> data : vWeights.values())
			data.keyAdd(u);
		return u;
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
		for (GraphWeights<?> data : eWeights.values())
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
		for (GraphWeights<?> data : eWeights.values())
			data.keyRemove(e);
		m--;
	}

	void edgeSwap(int e1, int e2) {
		for (GraphWeights<?> data : eWeights.values())
			data.keySwap(e1, e2);
		for (EdgeRenameListener listener : edgeRenameListeners)
			listener.edgeRename(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		vWeights.clear();
		n = 0;
	}

	@Override
	public void clearEdges() {
		for (GraphWeights<?> data : eWeights.values())
			data.clear();
		m = 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, GraphWeightsT extends GraphWeights<V>> GraphWeightsT verticesWeight(Object key) {
		return (GraphWeightsT) vWeights.get(key);
	}

	@Override
	public WeightsFactory verticesWeightsFactory() {
		return new WeightsFactory() {

			@Override
			public WeightsBuilderObj objs() {
				return new WeightsBuilderObj() {
					Object defVal = null;

					@SuppressWarnings("unchecked")
					@Override
					public <E> GraphWeights.Obj<E> build(Object key) {
						GraphWeights.Obj<E> weights = new GraphWeights.Obj<>(verticesNum());
						weights.setDefaultVal((E) defVal);
						return addVertexData(key, weights);
					}

					@Override
					public void setDefaultVal(Object defVal) {
						this.defVal = defVal;
					}
				};
			}

			@Override
			public WeightsBuilderInt ints() {
				return new WeightsBuilderInt() {
					int defVal = -1;

					@Override
					public GraphWeights.Int build(Object key) {
						GraphWeights.Int weights = new GraphWeights.Int(verticesNum());
						weights.setDefaultVal(defVal);
						return addVertexData(key, weights);
					}

					@Override
					public void setDefaultVal(int defVal) {
						this.defVal = defVal;
					}
				};
			}

			@Override
			public WeightsBuilderDouble doubles() {
				return new WeightsBuilderDouble() {
					double defVal = -1;

					@Override
					public GraphWeights.Double build(Object key) {
						GraphWeights.Double weights = new GraphWeights.Double(verticesNum());
						weights.setDefaultVal(defVal);
						return addVertexData(key, weights);
					}

					@Override
					public void setDefaultVal(double defVal) {
						this.defVal = defVal;
					}
				};
			}

		};
	}

	private <V, GraphWeightsT extends GraphWeights<V>> GraphWeightsT addVertexData(Object key, GraphWeightsT weights) {
		if (vWeights.containsKey(key))
			throw new IllegalArgumentException();
		int n = verticesNum();
		for (int u = 0; u < n; u++)
			weights.keyAdd(u);
		vWeights.put(key, weights);
		return weights;
	}

	@Override
	public Set<Object> getVerticesWeightKeys() {
		return Collections.unmodifiableSet(vWeights.keySet());
	}

	@Override
	public Collection<GraphWeights<?>> getVerticesWeights() {
		return Collections.unmodifiableCollection(vWeights.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, GraphWeightsT extends GraphWeights<E>> GraphWeightsT edgesWeight(Object key) {
		return (GraphWeightsT) eWeights.get(key);
	}

	@Override
	public WeightsFactory edgesWeightsFactory() {
		return new WeightsFactory() {

			@Override
			public WeightsBuilderObj objs() {
				return new WeightsBuilderObj() {
					Object defVal = null;

					@SuppressWarnings("unchecked")
					@Override
					public <E> GraphWeights.Obj<E> build(Object key) {
						GraphWeights.Obj<E> weights = new GraphWeights.Obj<>(edgesNum());
						weights.setDefaultVal((E) defVal);
						return addEdgeData(key, weights);
					}

					@Override
					public void setDefaultVal(Object defVal) {
						this.defVal = defVal;
					}
				};
			}

			@Override
			public WeightsBuilderInt ints() {
				return new WeightsBuilderInt() {
					int defVal = -1;

					@Override
					public GraphWeights.Int build(Object key) {
						GraphWeights.Int weights = new GraphWeights.Int(edgesNum());
						weights.setDefaultVal(defVal);
						return addEdgeData(key, weights);
					}

					@Override
					public void setDefaultVal(int defVal) {
						this.defVal = defVal;
					}
				};
			}

			@Override
			public WeightsBuilderDouble doubles() {
				return new WeightsBuilderDouble() {
					double defVal = -1;

					@Override
					public GraphWeights.Double build(Object key) {
						GraphWeights.Double weights = new GraphWeights.Double(edgesNum());
						weights.setDefaultVal(defVal);
						return addEdgeData(key, weights);
					}

					@Override
					public void setDefaultVal(double defVal) {
						this.defVal = defVal;
					}
				};
			}

		};
	}

	private <E, GraphWeightsT extends GraphWeights<E>> GraphWeightsT addEdgeData(Object key, GraphWeightsT weights) {
		if (eWeights.containsKey(key))
			throw new IllegalArgumentException();
		int m = edgesNum();
		for (int e = 0; e < m; e++)
			weights.keyAdd(e);
		eWeights.put(key, weights);
		return weights;
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return Collections.unmodifiableSet(eWeights.keySet());
	}

	@Override
	public Collection<GraphWeights<?>> getEdgesWeights() {
		return Collections.unmodifiableCollection(eWeights.values());
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

		Set<Object> vwKeys = getVerticesWeightKeys();
		if (!vwKeys.equals(o.getVerticesWeightKeys()))
			return false;
		for (Object weightKey : vwKeys)
			if (!verticesWeight(weightKey).equals(o.verticesWeight(weightKey)))
				return false;

		Set<Object> ewKeys = getEdgesWeightsKeys();
		if (!ewKeys.equals(o.getEdgesWeightsKeys()))
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

		Set<Object> ewKeys = g1.getEdgesWeightsKeys();
		assert ewKeys.equals(g2.getEdgesWeightsKeys());
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
		int h = 1;
		for (GraphWeights<?> vWeight : getVerticesWeights())
			h = h * 31 + vWeight.hashCode();

		int m = edgesNum();
		int[] es = new int[m];
		for (int e = 0; e < m; e++)
			es[e] = e;
		IntArrays.parallelQuickSort(es, createEdgeComparator(this, this));
		Collection<GraphWeights<?>> edgeWeights = getEdgesWeights();
		for (int eIdx = 0; eIdx < m; eIdx++) {
			int e = es[eIdx];
			for (GraphWeights<?> weight : edgeWeights)
				h = h * 31 + Objects.hashCode(weight.get(e));
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = verticesNum();
		Collection<GraphWeights<?>> weights = getEdgesWeights();

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
