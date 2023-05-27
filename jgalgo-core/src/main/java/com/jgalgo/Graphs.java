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

import java.util.Collections;
import java.util.Set;
import com.jgalgo.GraphsUtils.GraphCapabilitiesBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

/**
 * Static methods class for graphs.
 *
 * @author Barak Ugav
 */
public class Graphs {
	private Graphs() {}

	private abstract static class EmptyGraph implements Graph {

		private final IDStrategy.Continues verticesIDStrat = new IDStrategy.ContinuesEmpty();
		private final IDStrategy.Continues edgesIDStrat = new IDStrategy.ContinuesEmpty();

		@Override
		public IntSet vertices() {
			return IntSets.emptySet();
		}

		@Override
		public IntSet edges() {
			return IntSets.emptySet();
		}

		@Override
		public int addVertex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVertex(int vertex) {
			throw new IndexOutOfBoundsException(vertex);
		}

		@Override
		public EdgeIter edgesOut(int source) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public EdgeIter edgesIn(int target) {
			throw new IndexOutOfBoundsException(target);
		}

		@Override
		public EdgeIter getEdges(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public int addEdge(int source, int target) {
			throw new IndexOutOfBoundsException(source);
		}

		@Override
		public void removeEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public void reverseEdge(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeSource(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			throw new IndexOutOfBoundsException(edge);
		}

		@Override
		public void clear() {}

		@Override
		public void clearEdges() {}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
			return null;
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type,
				V defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeVerticesWeights(Object key) {}

		@Override
		public Set<Object> getVerticesWeightKeys() {
			return Collections.emptySet();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
			return null;
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeEdgesWeights(Object key) {}

		@Override
		public Set<Object> getEdgesWeightsKeys() {
			return Collections.emptySet();
		}

		@Override
		public IDStrategy.Continues getVerticesIDStrategy() {
			return verticesIDStrat;
		}

		@Override
		public IDStrategy getEdgesIDStrategy() {
			return edgesIDStrat;
		}

		@Override
		public Graph copy() {
			return this;
		}
	}

	private static class EmptyGraphUndirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newUndirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

	}

	private static class EmptyGraphDirected extends EmptyGraph {

		private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newDirected().vertexAdd(false)
				.vertexRemove(false).edgeAdd(false).edgeRemove(false).parallelEdges(false).selfEdges(false).build();

		@Override
		public GraphCapabilities getCapabilities() {
			return Capabilities;
		}

	}

	/**
	 * An undirected graphs with no vertices and no edges
	 */
	public static final Graph EmptyGraphUndirected = new EmptyGraphUndirected();

	/**
	 * A directed graphs with no vertices and no edges
	 */
	public static final Graph EmptyGraphDirected = new EmptyGraphDirected();

}
