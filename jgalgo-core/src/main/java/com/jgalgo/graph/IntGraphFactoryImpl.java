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
import java.util.Optional;
import java.util.function.Supplier;

class IntGraphFactoryImpl implements IntGraphFactory {

	final IndexGraphFactoryImpl indexFactory;
	Supplier<IdBuilderInt> vertexFactory = IdBuilderInt.defaultFactory();
	Supplier<IdBuilderInt> edgeFactory = IdBuilderInt.defaultFactory();

	IntGraphFactoryImpl(boolean directed) {
		this.indexFactory = new IndexGraphFactoryImpl(directed);
	}

	@Override
	public IntGraph newGraph() {
		return new IntGraphImpl(this);
	}

	@Override
	public IntGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		IndexGraph ig = indexFactory.newCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
		IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
		return new IntGraphImpl(this, ig, viMap, eiMap, Optional.empty(), Optional.empty());
	}

	@Override
	public IntGraph newImmutableCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		IndexGraphBuilder.ReIndexedGraph reIndexedGraph = indexFactory
				.immutableImpl()
				.newCopyOfWithReIndex(g.indexGraph(), true, true, copyVerticesWeights, copyEdgesWeights);
		IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();
		IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
		IndexGraph iGraph = reIndexedGraph.graph();
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
		return new IntGraphImpl(this, iGraph, viMap, eiMap, vReIndexing, eReIndexing);
	}

	@Override
	public IntGraphBuilder newBuilder() {
		return new IntGraphBuilderImpl(this);
	}

	@Override
	public IntGraphBuilder newBuilderCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		return new IntGraphBuilderImpl(this, g, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	public IntGraphFactory setDirected(boolean directed) {
		indexFactory.setDirected(directed);
		return this;
	}

	@Override
	public IntGraphFactory allowSelfEdges(boolean selfEdges) {
		indexFactory.allowSelfEdges(selfEdges);
		return this;
	}

	@Override
	public IntGraphFactory allowParallelEdges(boolean parallelEdges) {
		indexFactory.allowParallelEdges(parallelEdges);
		return this;
	}

	@Override
	public IntGraphFactory expectedVerticesNum(int expectedVerticesNum) {
		indexFactory.expectedVerticesNum(expectedVerticesNum);
		return this;
	}

	@Override
	public IntGraphFactory expectedEdgesNum(int expectedEdgesNum) {
		indexFactory.expectedEdgesNum(expectedEdgesNum);
		return this;
	}

	@Override
	public IntGraphFactory setVertexFactory(Supplier<? extends IdBuilder<Integer>> vertexFactory) {
		if (vertexFactory == null) {
			this.vertexFactory = null;
		} else {
			this.vertexFactory = () -> {
				IdBuilder<Integer> vertexBuilder = Objects.requireNonNull(vertexFactory.get());
				if (vertexBuilder instanceof IdBuilderInt) {
					return (IdBuilderInt) vertexBuilder;
				} else {
					return existingIds -> vertexBuilder.build(existingIds).intValue();
				}
			};
		}
		return this;
	}

	@Override
	public IntGraphFactory setEdgeFactory(Supplier<? extends IdBuilder<Integer>> edgeFactory) {
		if (edgeFactory == null) {
			this.edgeFactory = null;
		} else {
			this.edgeFactory = () -> {
				IdBuilder<Integer> edgeBuilder = Objects.requireNonNull(edgeFactory.get());
				if (edgeBuilder instanceof IdBuilderInt) {
					return (IdBuilderInt) edgeBuilder;
				} else {
					return existingIds -> edgeBuilder.build(existingIds).intValue();
				}
			};
		}
		return this;
	}

	@Override
	public IntGraphFactory addHint(GraphFactory.Hint hint) {
		indexFactory.addHint(hint);
		return this;
	}

	@Override
	public IntGraphFactory removeHint(GraphFactory.Hint hint) {
		indexFactory.removeHint(hint);
		return this;
	}

	@Override
	public IntGraphFactory setOption(String key, Object value) {
		indexFactory.setOption(key, value);
		return this;
	}
}
