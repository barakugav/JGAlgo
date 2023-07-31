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

import java.util.EnumSet;
import java.util.List;

class IndexGraphFactoryImpl implements IndexGraphFactory {

	private boolean directed;
	private boolean selfEdges;
	private boolean parallelEdges;
	private int expectedVerticesNum;
	private int expectedEdgesNum;
	private final EnumSet<GraphFactory.Hint> hints = EnumSet.noneOf(GraphFactory.Hint.class);
	private String impl;

	IndexGraphFactoryImpl(boolean directed) {
		this.directed = directed;
	}

	IndexGraphFactoryImpl(IndexGraph g) {
		GraphCapabilities capabilities = g.getCapabilities();
		this.directed = capabilities.directed();
		this.selfEdges = capabilities.selfEdges();
		this.parallelEdges = capabilities.parallelEdges();
		impl = Graphs.getIndexGraphImpl(g);
	}

	@Override
	public IndexGraph newGraph() {
		return chooseImpl().newGraph(expectedVerticesNum, expectedEdgesNum);
	}

	@Override
	public IndexGraph newCopyOf(IndexGraph g, boolean copyWeights) {
		setDirected(g.getCapabilities().directed());
		return chooseImpl().newCopyOf(g, copyWeights);
	}

	private static interface Impl {

		IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum);

		IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights);

	}

	private Impl chooseImpl() {
		Impl arrayImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphArrayDirected(graph, copyWeights);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphArrayUndirected(graph, copyWeights);
			}
		};
		Impl linkedImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphLinkedDirected(graph, copyWeights);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphLinkedUndirected(graph, copyWeights);
			}
		};
		Impl hashtableImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphHashmapDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphHashmapDirected(graph, copyWeights);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphHashmapUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphHashmapUndirected(graph, copyWeights);
			}
		};
		Impl matrixImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphMatrixDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphMatrixDirected(graph, copyWeights);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphMatrixUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyWeights) {
				return new GraphMatrixUndirected(graph, copyWeights);
			}
		};

		if (impl != null) {
			switch (impl) {
				case "array":
					return arrayImpl;
				case "linked-list":
					return linkedImpl;
				case "hashtable":
					return hashtableImpl;
				case "matrix":
					return matrixImpl;
				default:
					throw new IllegalArgumentException("unknown 'impl' value: " + impl);
			}
		} else {
			if (hints.contains(GraphFactory.Hint.FastEdgeLookup) && !parallelEdges)
				return hashtableImpl;

			if (hints.containsAll(List.of(GraphFactory.Hint.FastEdgeLookup, GraphFactory.Hint.DenseGraph)) && !selfEdges
					&& !parallelEdges)
				return matrixImpl;

			if (hints.contains(GraphFactory.Hint.FastEdgeRemoval) && !selfEdges)
				return linkedImpl;

			return arrayImpl;
		}
	}

	@Override
	public IndexGraphFactory setDirected(boolean directed) {
		this.directed = directed;
		return this;
	}

	@Override
	public IndexGraphFactory allowSelfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	@Override
	public IndexGraphFactory allowParallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
		return this;
	}

	@Override
	public IndexGraphFactory expectedVerticesNum(int expectedVerticesNum) {
		if (expectedVerticesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedVerticesNum);
		this.expectedVerticesNum = expectedVerticesNum;
		return this;
	}

	@Override
	public IndexGraphFactory expectedEdgesNum(int expectedEdgesNum) {
		if (expectedEdgesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedEdgesNum);
		this.expectedEdgesNum = expectedEdgesNum;
		return this;
	}

	@Override
	public IndexGraphFactory addHint(GraphFactory.Hint hint) {
		hints.add(hint);
		return this;
	}

	@Override
	public IndexGraphFactory removeHint(GraphFactory.Hint hint) {
		hints.remove(hint);
		return this;
	}

	@Override
	public IndexGraphFactory setOption(String key, Object value) {
		switch (key) {
			case "impl":
				impl = (String) value;
				break;
			default:
				throw new IllegalArgumentException("unknown option key: " + key);
		}
		return this;
	}

}
