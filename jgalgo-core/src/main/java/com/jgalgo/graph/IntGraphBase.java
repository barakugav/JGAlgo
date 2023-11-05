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

package com.jgalgo.graph;

import java.util.Collection;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

abstract class IntGraphBase extends GraphBase<Integer, Integer> implements IntGraph {

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof IntGraph))
			return super.equals(other);
		IntGraph o = (IntGraph) other;

		if (isDirected() != o.isDirected())
			return false;
		if (!vertices().equals(o.vertices()))
			return false;
		if (!edges().equals(o.edges()))
			return false;
		if (isDirected()) {
			for (int e : edges())
				if (edgeSource(e) != o.edgeSource(e) || edgeTarget(e) != o.edgeTarget(e))
					return false;
		} else {
			for (int e : edges()) {
				int s1 = edgeSource(e), t1 = edgeTarget(e);
				int s2 = o.edgeSource(e), t2 = o.edgeTarget(e);
				if (!(s1 == s2 && t1 == t2) && !(s1 == t2 && t1 == s2))
					return false;
			}
		}

		if (!getVerticesWeightsKeys().equals(o.getVerticesWeightsKeys()))
			return false;
		for (String key : getVerticesWeightsKeys()) {
			IWeights<?> w1 = getVerticesIWeights(key), w2 = o.getVerticesIWeights(key);
			if (!WeightsImpl.isEqual(vertices(), w1, w2))
				return false;
		}
		if (!getEdgesWeightsKeys().equals(o.getEdgesWeightsKeys()))
			return false;
		for (String key : getEdgesWeightsKeys()) {
			IWeights<?> w1 = getEdgesIWeights(key), w2 = o.getEdgesIWeights(key);
			if (!WeightsImpl.isEqual(edges(), w1, w2))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int h = Boolean.hashCode(isDirected());
		h += vertices().hashCode();
		h += edges().hashCode();
		if (isDirected()) {
			for (int e : edges())
				h += edgeSource(e) + 31 * edgeTarget(e);
		} else {
			for (int e : edges())
				h += edgeSource(e) + edgeTarget(e);
		}
		for (String key : getVerticesWeightsKeys())
			h += WeightsImpl.hashCode(vertices(), getVerticesIWeights(key));
		for (String key : getEdgesWeightsKeys())
			h += WeightsImpl.hashCode(edges(), getEdgesIWeights(key));
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');

		Set<String> verticesWeightsKeys = getVerticesWeightsKeys();
		Collection<IWeights<?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add(getVerticesIWeights(key));

		Set<String> edgesWeightsKeys = getEdgesWeightsKeys();
		Collection<IWeights<?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add(getEdgesIWeights(key));

		ObjIntConsumer<Collection<IWeights<?>>> appendWeights = (weights, elm) -> {
			s.append('[');
			boolean firstData = true;
			for (IWeights<?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(elm));
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
			for (IEdgeIter eit = outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
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

	abstract class EdgeSetAbstract extends AbstractIntSet implements IEdgeSet {

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			removeEdge(edge);
			return true;
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
			removeOutEdgesOf(source);
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
			removeInEdgesOf(target);
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
			removeOutEdgesOf(source);
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
			removeInEdgesOf(target);
		}
	}

}
