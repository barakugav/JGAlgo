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

import java.util.function.Supplier;
import it.unimi.dsi.fastutil.ints.IntSet;

class IntGraphFactoryImpl implements IntGraphFactory {

	final IndexGraphFactoryImpl indexFactory;
	IdBuilderInt vertexBuilder = DefaultIdBuilder.get();
	IdBuilderInt edgeBuilder = DefaultIdBuilder.get();

	IntGraphFactoryImpl(boolean directed) {
		this.indexFactory = new IndexGraphFactoryImpl(directed);
	}

	@Override
	public IntGraph newGraph() {
		return new IntGraphImpl(this);
	}

	@Override
	public IntGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		return new IntGraphImpl(this, g, copyVerticesWeights, copyEdgesWeights);
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
	public IntGraphFactory setVertexBuilder(IdBuilder<Integer> vertexBuilder) {
		if (vertexBuilder == null) {
			this.vertexBuilder = null;
		} else if (vertexBuilder instanceof IdBuilderInt) {
			this.vertexBuilder = (IdBuilderInt) vertexBuilder;
		} else {
			this.vertexBuilder = existingIds -> vertexBuilder.build(existingIds).intValue();
		}
		return this;
	}

	@Override
	public IntGraphFactory setEdgeBuilder(IdBuilder<Integer> edgeBuilder) {
		if (edgeBuilder == null) {
			this.edgeBuilder = null;
		} else if (edgeBuilder instanceof IdBuilderInt) {
			this.edgeBuilder = (IdBuilderInt) edgeBuilder;
		} else {
			this.edgeBuilder = existingIds -> edgeBuilder.build(existingIds).intValue();
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

	private static final Supplier<IdBuilderInt> DefaultIdBuilder = () -> {
		return new IdBuilderInt() {
			private int counter;

			@Override
			public int build(IntSet ids) {
				for (;;) {
					int id = ++counter;
					if (!ids.contains(id))
						return id;
				}
			}
		};
	};

}
