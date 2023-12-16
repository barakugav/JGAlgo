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

class GraphFactoryImpl<V, E> implements GraphFactory<V, E> {

	final IndexGraphFactoryImpl indexFactory;
	IdBuilder<V> vertexBuilder;
	IdBuilder<E> edgeBuilder;

	GraphFactoryImpl(boolean directed) {
		this.indexFactory = new IndexGraphFactoryImpl(directed);
	}

	@Override
	public Graph<V, E> newGraph() {
		return new GraphImpl<>(this);
	}

	@Override
	public Graph<V, E> newCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return new GraphImpl<>(this, g, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	public GraphBuilder<V, E> newBuilder() {
		return new GraphBuilderImpl<>(this);
	}

	@Override
	public GraphBuilder<V, E> newBuilderCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return new GraphBuilderImpl<>(this, g, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	public GraphFactory<V, E> allowSelfEdges(boolean selfEdges) {
		indexFactory.allowSelfEdges(selfEdges);
		return this;
	}

	@Override
	public GraphFactory<V, E> allowParallelEdges(boolean parallelEdges) {
		indexFactory.allowParallelEdges(parallelEdges);
		return this;
	}

	@Override
	public GraphFactory<V, E> expectedVerticesNum(int expectedVerticesNum) {
		indexFactory.expectedVerticesNum(expectedVerticesNum);
		return this;
	}

	@Override
	public GraphFactory<V, E> expectedEdgesNum(int expectedEdgesNum) {
		indexFactory.expectedEdgesNum(expectedEdgesNum);
		return this;
	}

	@Override
	public GraphFactory<V, E> setVertexBuilder(IdBuilder<V> vertexBuilder) {
		this.vertexBuilder = vertexBuilder;
		return this;
	}

	@Override
	public GraphFactory<V, E> setEdgeBuilder(IdBuilder<E> edgeBuilder) {
		this.edgeBuilder = edgeBuilder;
		return this;
	}

	@Override
	public GraphFactory<V, E> addHint(GraphFactory.Hint hint) {
		indexFactory.addHint(hint);
		return this;
	}

	@Override
	public GraphFactory<V, E> removeHint(GraphFactory.Hint hint) {
		indexFactory.removeHint(hint);
		return this;
	}

	@Override
	public GraphFactory<V, E> setOption(String key, Object value) {
		indexFactory.setOption(key, value);
		return this;
	}
}
