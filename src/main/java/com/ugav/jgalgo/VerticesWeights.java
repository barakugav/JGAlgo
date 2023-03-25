package com.ugav.jgalgo;

public class VerticesWeights {

	private VerticesWeights() {
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key) {
		return ofObjs(g, key, null);
	}

	public static <E> Weights<E> ofObjs(Graph g, Object key, E defVal) {
		return addWeights(g, key, new Builder(g).ofObjs(defVal));
	}

	public static Weights.Int ofInts(Graph g, Object key) {
		return ofInts(g, key, -1);
	}

	public static Weights.Int ofInts(Graph g, Object key, int defVal) {
		return addWeights(g, key, new Builder(g).ofInts(defVal));
	}

	public static Weights.Long ofLongs(Graph g, Object key) {
		return ofLongs(g, key, -1);
	}

	public static Weights.Long ofLongs(Graph g, Object key, long defVal) {
		return addWeights(g, key, new Builder(g).ofLongs(defVal));
	}

	public static Weights.Double ofDoubles(Graph g, Object key) {
		return ofDoubles(g, key, -1);
	}

	public static Weights.Double ofDoubles(Graph g, Object key, double defVal) {
		return addWeights(g, key, new Builder(g).ofDoubles(defVal));
	}

	public static Weights.Bool ofBools(Graph g, Object key) {
		return ofBools(g, key, false);
	}

	public static Weights.Bool ofBools(Graph g, Object key, boolean defVal) {
		return addWeights(g, key, new Builder(g).ofBools(defVal));
	}

	@SuppressWarnings("unchecked")
	private static <E, WeightsT extends Weights<E>> WeightsT addWeights(Graph g, Object key, WeightsT weights) {
		DataContainer<E> container = ((WeightsImpl<E>) weights).container;
		int n = g.vertices().size();
		container.ensureCapacity(n);
		for (int uIdx = 0; uIdx < n; uIdx++)
			container.add(uIdx);
		return ((GraphAbstract) g).addVerticesWeights(key, weights);
	}

	/**
	 * The default vertices weight key of the bipartite property.
	 * <p>
	 * A bipartite graph is a graph in which the vertices are partitioned into two
	 * sets V1,V2 and there are no edges between two vertices u,v if they are both
	 * in V1 or both in V2. Some algorithms expect a bipartite graph as an input,
	 * and the partition V1,V2 is expected to be a vertex boolean weight keyed by
	 * {@link #DefaultBipartiteWeightKey}. To use a different key, the algorithms
	 * expose a {@code setBipartiteVerticesWeightKey(Object)} function.
	 */
	public static final Object DefaultBipartiteWeightKey = new Object() {
		@Override
		public String toString() {
			return "DefaultBipartiteVerticesWeightKey";
		}
	};

	private static class Builder {
		private final Graph g;
		private final boolean isContinues;

		Builder(Graph g) {
			this.g = g;
			isContinues = g.getEdgesIDStrategy() instanceof IDStrategy.Continues;
		}

		<E> Weights<E> ofObjs(E defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Obj<>(g.vertices().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Obj<>(g.vertices().size(), defVal, g.getVerticesIDStrategy());
			}
		}

		Weights.Int ofInts(int defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Int(g.vertices().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Int(g.vertices().size(), defVal, g.getVerticesIDStrategy());
			}
		}

		Weights.Long ofLongs(long defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Long(g.vertices().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Long(g.vertices().size(), defVal, g.getVerticesIDStrategy());
			}
		}

		Weights.Double ofDoubles(double defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Double(g.vertices().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Double(g.vertices().size(), defVal, g.getVerticesIDStrategy());
			}
		}

		Weights.Bool ofBools(boolean defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Bool(g.vertices().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Bool(g.vertices().size(), defVal, g.getVerticesIDStrategy());
			}
		}
	}

}
