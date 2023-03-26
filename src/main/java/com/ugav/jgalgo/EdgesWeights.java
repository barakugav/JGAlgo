package com.ugav.jgalgo;

public class EdgesWeights {

	private EdgesWeights() {
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
		int m = g.edges().size();
		container.ensureCapacity(m);
		for (int eIdx = 0; eIdx < m; eIdx++)
			container.add(eIdx);
		return ((GraphBase) g).addEdgesWeights(key, weights);
	}

	private static class Builder {
		private final Graph g;
		private final boolean isContinues;

		Builder(Graph g) {
			this.g = g;
			isContinues = g.getEdgesIDStrategy() instanceof IDStrategy.Continues;
		}

		<E> Weights<E> ofObjs(E defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Obj<>(g.edges().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Obj<>(g.edges().size(), defVal, g.getEdgesIDStrategy());
			}
		}

		Weights.Int ofInts(int defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Int(g.edges().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Int(g.edges().size(), defVal, g.getEdgesIDStrategy());
			}
		}

		Weights.Long ofLongs(long defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Long(g.edges().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Long(g.edges().size(), defVal, g.getEdgesIDStrategy());
			}
		}

		Weights.Double ofDoubles(double defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Double(g.edges().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Double(g.edges().size(), defVal, g.getEdgesIDStrategy());
			}
		}

		Weights.Bool ofBools(boolean defVal) {
			if (isContinues) {
				return new WeightsImpl.Direct.Bool(g.edges().size(), defVal);
			} else {
				return new WeightsImpl.Mapped.Bool(g.edges().size(), defVal, g.getEdgesIDStrategy());
			}
		}
	}

}
