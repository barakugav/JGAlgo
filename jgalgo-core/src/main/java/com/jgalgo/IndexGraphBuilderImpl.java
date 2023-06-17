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

import java.util.EnumSet;
import java.util.function.BiFunction;

class IndexGraphBuilderImpl implements IndexGraph.Builder {

	private boolean directed;
	private int expectedVerticesNum;
	private int expectedEdgesNum;
	private final EnumSet<Graph.Builder.Hint> hints = EnumSet.noneOf(Graph.Builder.Hint.class);
	private String impl;

	IndexGraphBuilderImpl(boolean directed) {
		this.directed = directed;
	}

	@Override
	public IndexGraph build() {
		BiFunction<Integer, Integer, ? extends GraphBaseIndex> baseBuilderArray =
				directed ? GraphArrayDirected::new : GraphArrayUndirected::new;
		BiFunction<Integer, Integer, ? extends GraphBaseIndex> baseBuilderLinked =
				directed ? GraphLinkedDirected::new : GraphLinkedUndirected::new;
		BiFunction<Integer, Integer, ? extends GraphBaseIndex> baseBuilderTable =
				directed ? GraphTableDirected::new : GraphTableUndirected::new;

		BiFunction<Integer, Integer, ? extends GraphBaseIndex> baseBuilder;
		if (impl != null) {
			if ("GraphArray".equals(impl))
				baseBuilder = baseBuilderArray;
			else if ("GraphLinked".equals(impl))
				baseBuilder = baseBuilderLinked;
			else if ("GraphTable".equals(impl))
				baseBuilder = baseBuilderTable;
			else
				throw new IllegalArgumentException("unknown 'impl' value: " + impl);
		} else {
			if (hints.contains(Graph.Builder.Hint.FastEdgeLookup))
				baseBuilder = baseBuilderTable;
			else if (hints.contains(Graph.Builder.Hint.FastEdgeLookup))
				baseBuilder = baseBuilderLinked;
			else
				baseBuilder = baseBuilderArray;
		}
		return baseBuilder.apply(Integer.valueOf(expectedVerticesNum), Integer.valueOf(expectedEdgesNum));
	}

	@Override
	public IndexGraph.Builder setDirected(boolean directed) {
		this.directed = directed;
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
