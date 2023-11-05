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
import java.util.function.BiConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

abstract class GraphBase<V, E> implements Graph<V, E> {

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Graph))
			return false;
		@SuppressWarnings("unchecked")
		Graph<V, E> o = (Graph<V, E>) other;

		if (isDirected() != o.isDirected())
			return false;
		if (!vertices().equals(o.vertices()))
			return false;
		if (!edges().equals(o.edges()))
			return false;
		if (isDirected()) {
			for (E e : edges())
				if (!edgeSource(e).equals(o.edgeSource(e)) || !edgeTarget(e).equals(o.edgeTarget(e)))
					return false;
		} else {
			for (E e : edges()) {
				V s1 = edgeSource(e), t1 = edgeTarget(e);
				V s2 = o.edgeSource(e), t2 = o.edgeTarget(e);
				if (!(s1.equals(s2) && t1.equals(t2)) && !(s1.equals(t2) && t1.equals(s2)))
					return false;
			}
		}

		if (!getVerticesWeightsKeys().equals(o.getVerticesWeightsKeys()))
			return false;
		for (String key : getVerticesWeightsKeys()) {
			Weights<V, ?> w1 = getVerticesWeights(key), w2 = o.getVerticesWeights(key);
			if (!WeightsImpl.isEqual(vertices(), w1, w2))
				return false;
		}
		if (!getEdgesWeightsKeys().equals(o.getEdgesWeightsKeys()))
			return false;
		for (String key : getEdgesWeightsKeys()) {
			Weights<E, ?> w1 = getEdgesWeights(key), w2 = o.getEdgesWeights(key);
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
			for (E e : edges())
				h += edgeSource(e).hashCode() + 31 * edgeTarget(e).hashCode();
		} else {
			for (E e : edges())
				h += edgeSource(e).hashCode() + edgeTarget(e).hashCode();
		}
		for (String key : getVerticesWeightsKeys())
			h += WeightsImpl.hashCode(vertices(), getVerticesWeights(key));
		for (String key : getEdgesWeightsKeys())
			h += WeightsImpl.hashCode(edges(), getEdgesWeights(key));
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');

		Set<String> verticesWeightsKeys = getVerticesWeightsKeys();
		Collection<Weights<V, ?>> verticesWeights = new ObjectArrayList<>(verticesWeightsKeys.size());
		for (String key : verticesWeightsKeys)
			verticesWeights.add(getVerticesWeights(key));

		Set<String> edgesWeightsKeys = getEdgesWeightsKeys();
		Collection<Weights<E, ?>> edgesWeights = new ObjectArrayList<>(edgesWeightsKeys.size());
		for (String key : edgesWeightsKeys)
			edgesWeights.add(getEdgesWeights(key));

		BiConsumer<Collection<Weights<V, ?>>, V> appendVertexWeights = (weights, vertex) -> {
			s.append('[');
			boolean firstData = true;
			for (Weights<V, ?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(vertex));
			}
			s.append(']');
		};
		BiConsumer<Collection<Weights<E, ?>>, E> appendEdgeWeights = (weights, edge) -> {
			s.append('[');
			boolean firstData = true;
			for (Weights<E, ?> weight : weights) {
				if (firstData) {
					firstData = false;
				} else {
					s.append(", ");
				}
				s.append(weight.getAsObj(edge));
			}
			s.append(']');
		};

		boolean firstVertex = true;
		for (V u : vertices()) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append('v').append(u);
			if (!verticesWeights.isEmpty())
				appendVertexWeights.accept(verticesWeights, u);

			s.append(": [");
			boolean firstEdge = true;
			for (EdgeIter<V, E> eit = outEdges(u).iterator(); eit.hasNext();) {
				E e = eit.next();
				V v = eit.target();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v);
				if (!edgesWeights.isEmpty()) {
					s.append(", ");
					appendEdgeWeights.accept(edgesWeights, e);
				}
				s.append(')');
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

}
