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

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

abstract class GraphBase implements Graph {

	@Override
	public EdgeSet getEdges(int source, int target) {
		return getCapabilities().directed() ? new EdgeSetSourceTargetDirected(source, target)
				: new EdgeSetSourceTargetUndirected(source, target);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');

		Set<Object> verticesWeightsKeys = getVerticesWeightKeys();
		Collection<Weights<?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (Object key : verticesWeightsKeys)
			verticesWeights.add(getVerticesWeights(key));

		Set<Object> edgesWeightsKeys = getEdgesWeightsKeys();
		Collection<Weights<?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
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
		for (int u : vertices()) {
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

	private abstract class EdgeSetAbstract extends AbstractIntSet implements EdgeSet {

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			removeEdge(edge);
			return true;
		}

		@Override
		public int size() {
			return Utils.size(this);
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

	}

	abstract class EdgeSetOutUndirected extends EdgeSetAbstract {

		final int source;

		EdgeSetOutUndirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge) || source == edgeTarget(edge);
		}

		@Override
		public void clear() {
			removeEdgesOutOf(source);
		}
	}

	abstract class EdgeSetInUndirected extends EdgeSetAbstract {

		final int target;

		EdgeSetInUndirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return target == edgeSource(edge) || target == edgeTarget(edge);
		}

		@Override
		public void clear() {
			removeEdgesInOf(target);
		}
	}

	abstract class EdgeSetOutDirected extends EdgeSetAbstract {

		final int source;

		EdgeSetOutDirected(int source) {
			this.source = source;
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge);
		}

		@Override
		public void clear() {
			removeEdgesOutOf(source);
		}
	}

	abstract class EdgeSetInDirected extends EdgeSetAbstract {

		final int target;

		EdgeSetInDirected(int target) {
			this.target = target;
		}

		@Override
		public boolean contains(int edge) {
			return target == edgeTarget(edge);
		}

		@Override
		public void clear() {
			removeEdgesInOf(target);
		}
	}

	private abstract class EdgeSetSourceTarget extends EdgeSetAbstract {

		final int source, target;

		EdgeSetSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void clear() {
			for (EdgeIter it = iterator(); it.hasNext();) {
				it.nextInt();
				it.remove();
			}
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterSourceTarget(source, target);
		}
	}

	private class EdgeSetSourceTargetUndirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetUndirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			int s = edgeSource(edge), t = edgeTarget(edge);
			return (source == s && target == t) || (source == t && target == s);
		}
	}

	private class EdgeSetSourceTargetDirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetDirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge) && target == edgeTarget(edge);
		}
	}

	private class EdgeIterSourceTarget implements EdgeIterImpl {

		private final int source, target;
		private final EdgeIter it;
		private int e = -1;

		EdgeIterSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
			it = edgesOut(source).iterator();
		}

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
	}

}
