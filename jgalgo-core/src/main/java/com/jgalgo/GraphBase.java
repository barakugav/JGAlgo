/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBase implements Graph {

	final IDStrategyImpl.Continues verticesIDStrat;
	final IDStrategyImpl edgesIDStrat;

	GraphBase(IDStrategy.Continues verticesIDStrat, IDStrategy edgesIDStrat) {
		this.verticesIDStrat = (IDStrategyImpl.Continues) Objects.requireNonNull(verticesIDStrat);
		this.edgesIDStrat = (IDStrategyImpl) Objects.requireNonNull(edgesIDStrat);
	}

	@Override
	public final IntSet vertices() {
		return verticesIDStrat.idSet();
	}

	@Override
	public final IntSet edges() {
		return edgesIDStrat.idSet();
	}

	@Override
	public EdgeIter getEdges(int source, int target) {
		return new EdgeIterImpl() {
			EdgeIter it = edgesOut(source).iterator();
			int e = -1;

			@Override
			public boolean hasNext() {
				if (e != -1)
					return true;
				while (it.hasNext()) {
					int eNext = it.nextInt();
					if (it.target() == target) {
						e = eNext;
						return true;
					}
				}
				return false;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				int ret = e;
				e = -1;
				return ret;
			}

			@Override
			public int peekNext() {
				if (!hasNext())
					throw new NoSuchElementException();
				return e;
			}

			@Override
			public int source() {
				return source;
			}

			@Override
			public int target() {
				return target;
			}
		};
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIDStrat.clear();
	}

	@Override
	public void clearEdges() {
		edgesIDStrat.clear();
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
		return addVerticesWeights(key, type, null);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		WeightsT weights = WeightsImpl.newInstance((IDStrategyImpl) getVerticesIDStrategy(), type, defVal);
		addVerticesWeightsContainer(key, weights);
		return weights;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
		return addEdgesWeights(key, type, null);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsT weights = WeightsImpl.newInstance((IDStrategyImpl) getEdgesIDStrategy(), type, defVal);
		addEdgesWeightsContainer(key, weights);
		return weights;
	}

	abstract void addVerticesWeightsContainer(Object key, Weights<?> weights);

	abstract void addEdgesWeightsContainer(Object key, Weights<?> weights);

	@Override
	public IDStrategy.Continues getVerticesIDStrategy() {
		return verticesIDStrat;
	}

	@Override
	public IDStrategy getEdgesIDStrategy() {
		return edgesIDStrat;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = vertices().size();

		Set<Object> verticesWeightsKeys = getVerticesWeightKeys();
		Collection<Weights<?>> verticesWeights = new ArrayList<>(verticesWeightsKeys.size());
		for (Object key : verticesWeightsKeys)
			verticesWeights.add(getVerticesWeights(key));

		Set<Object> edgesWeightsKeys = getEdgesWeightsKeys();
		Collection<Weights<?>> edgesWeights = new ArrayList<>(edgesWeightsKeys.size());
		for (Object key : edgesWeightsKeys)
			edgesWeights.add(getEdgesWeights(key));

		ObjIntConsumer<Collection<Weights<?>>> appendWeights = (weights, key) -> {
			s.append('[');
			boolean firstData = true;
			for (Weights<?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.get(key));
			}
			s.append(']');
		};

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append('v').append(u);
			if (!verticesWeights.isEmpty())
				appendWeights.accept(verticesWeights, u);

			s.append(": [");
			boolean firstEdge = true;
			for (EdgeIter eit = edgesOut(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendWeights.accept(edgesWeights, e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	abstract class EdgeSetOutUndirected extends AbstractIntSet implements EdgeSet {

		final int source;

		EdgeSetOutUndirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge) || source == edgeTarget(edge);
		}

		@Override
		public int size() {
			int count = 0;
			for (IntIterator it = iterator(); it.hasNext(); it.nextInt())
				count++;
			return count;
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public void clear() {
			removeEdgesOutOf(source);
		}
	}

	abstract class EdgeSetInUndirected extends AbstractIntSet implements EdgeSet {

		final int target;

		EdgeSetInUndirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return target == edgeSource(edge) || target == edgeTarget(edge);
		}

		@Override
		public int size() {
			int count = 0;
			for (IntIterator it = iterator(); it.hasNext(); it.nextInt())
				count++;
			return count;
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public void clear() {
			removeEdgesInOf(target);
		}
	}

	abstract class EdgeSetOutDirected extends AbstractIntSet implements EdgeSet {

		final int source;

		EdgeSetOutDirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge);
		}

		@Override
		public int size() {
			int count = 0;
			for (IntIterator it = iterator(); it.hasNext(); it.nextInt())
				count++;
			return count;
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public void clear() {
			removeEdgesOutOf(source);
		}
	}

	abstract class EdgeSetInDirected extends AbstractIntSet implements EdgeSet {

		final int target;

		EdgeSetInDirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return target == edgeTarget(edge);
		}

		@Override
		public int size() {
			int count = 0;
			for (IntIterator it = iterator(); it.hasNext(); it.nextInt())
				count++;
			return count;
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public void clear() {
			removeEdgesInOf(target);
		}
	}

}
