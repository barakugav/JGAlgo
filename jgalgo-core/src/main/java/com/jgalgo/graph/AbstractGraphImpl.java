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

import java.util.Objects;
import java.util.Set;

abstract class AbstractGraphImpl<V, E> extends AbstractGraph<V, E> {

	final IndexGraph indexGraph;

	AbstractGraphImpl(IndexGraph indexGraph) {
		this.indexGraph = Objects.requireNonNull(indexGraph);
	}

	@Override
	public IndexGraph indexGraph() {
		return indexGraph;
	}

	@Override
	public Set<String> verticesWeightsKeys() {
		return indexGraph.verticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		indexGraph.removeVerticesWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		indexGraph.addVerticesWeights(key, type, defVal);
		return verticesWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		indexGraph.addEdgesWeights(key, type, defVal);
		return edgesWeights(key);
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return indexGraph.edgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		indexGraph.removeEdgesWeights(key);
	}

	@Override
	public boolean isDirected() {
		return indexGraph.isDirected();
	}

	@Override
	public boolean isAllowSelfEdges() {
		return indexGraph.isAllowSelfEdges();
	}

	@Override
	public boolean isAllowParallelEdges() {
		return indexGraph.isAllowParallelEdges();
	}

}
