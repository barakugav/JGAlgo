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

class IndexGraphBuilderImpl implements IndexGraph.Builder {

	private boolean directed;
	private boolean selfEdges;
	private boolean parallelEdges;
	private int expectedVerticesNum;
	private int expectedEdgesNum;
	private final EnumSet<Graph.Builder.Hint> hints = EnumSet.noneOf(Graph.Builder.Hint.class);
	private String impl;

	IndexGraphBuilderImpl(boolean directed) {
		this.directed = directed;
	}

	IndexGraphBuilderImpl(IndexGraph g) {
		GraphCapabilities capabilities = g.getCapabilities();
		this.directed = capabilities.directed();
		this.selfEdges = capabilities.selfEdges();
		this.parallelEdges = capabilities.parallelEdges();
		impl = Graphs.getIndexGraphImpl(g);
	}

	@Override
	public IndexGraph build() {
		return chooseImpl().build(expectedVerticesNum, expectedEdgesNum);
	}

	@Override
	public IndexGraph buildCopyOf(IndexGraph g) {
		return chooseImpl().buildCopyOf(g);
	}

	private static interface Impl {

		IndexGraph build(int expectedVerticesNum, int expectedEdgesNum);

		IndexGraph buildCopyOf(IndexGraph graph);

	}

	private Impl chooseImpl() {
		Impl arrayImpl = directed ? new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphArrayDirected(graph);
			}
		} : new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphArrayUndirected(graph);
			}
		};
		Impl linkedImpl = directed ? new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphLinkedDirected(graph);
			}
		} : new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphLinkedUndirected(graph);
			}
		};
		Impl tableImpl = directed ? new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphTableDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphTableDirected(graph);
			}
		} : new Impl() {

			@Override
			public IndexGraph build(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphTableUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph buildCopyOf(IndexGraph graph) {
				return new GraphTableUndirected(graph);
			}
		};

		Impl impl;
		if (this.impl != null) {
			if ("GraphArray".equals(this.impl))
				impl = arrayImpl;
			else if ("GraphLinked".equals(this.impl))
				impl = linkedImpl;
			else if ("GraphTable".equals(this.impl))
				impl = tableImpl;
			else
				throw new IllegalArgumentException("unknown 'impl' value: " + this.impl);
		} else {
			if (hints.contains(Graph.Builder.Hint.FastEdgeLookup) && !selfEdges && !parallelEdges)
				impl = tableImpl;
			else if (hints.contains(Graph.Builder.Hint.FastEdgeLookup) && !selfEdges)
				impl = linkedImpl;
			else
				impl = arrayImpl;
		}
		return impl;
	}

	@Override
	public IndexGraph.Builder setDirected(boolean directed) {
		this.directed = directed;
		return this;
	}

	@Override
	public IndexGraph.Builder allowSelfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	@Override
	public IndexGraph.Builder allowParallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
		return this;
	}

	@Override
	public IndexGraph.Builder expectedVerticesNum(int expectedVerticesNum) {
		if (expectedVerticesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedVerticesNum);
		this.expectedVerticesNum = expectedVerticesNum;
		return this;
	}

	@Override
	public IndexGraph.Builder expectedEdgesNum(int expectedEdgesNum) {
		if (expectedEdgesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedEdgesNum);
		this.expectedEdgesNum = expectedEdgesNum;
		return this;
	}

	@Override
	public IndexGraph.Builder addHint(Graph.Builder.Hint hint) {
		hints.add(hint);
		return this;
	}

	@Override
	public IndexGraph.Builder removeHint(Graph.Builder.Hint hint) {
		hints.remove(hint);
		return this;
	}

	@Override
	public IndexGraph.Builder setOption(String key, Object value) {
		if ("impl".equals(key)) {
			impl = (String) value;
		} else {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
		return this;
	}

}
