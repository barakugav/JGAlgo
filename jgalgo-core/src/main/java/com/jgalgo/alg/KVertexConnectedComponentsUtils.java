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
package com.jgalgo.alg;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.IntSet;

class KVertexConnectedComponentsUtils {

	private KVertexConnectedComponentsUtils() {}

	abstract static class AbstractImpl implements KVertexConnectedComponentsAlgo {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> KVertexConnectedComponentsAlgo.Result<V, E> findKVertexConnectedComponents(Graph<V, E> g, int k) {
			if (g instanceof IndexGraph) {
				return (KVertexConnectedComponentsAlgo.Result<V, E>) findKVertexConnectedComponents((IndexGraph) g, k);
			} else {
				IndexGraph ig = g.indexGraph();
				KVertexConnectedComponentsAlgo.IResult indexRes = findKVertexConnectedComponents(ig, k);
				return resultFromIndexResult(g, indexRes);
			}
		}

		abstract KVertexConnectedComponentsAlgo.IResult findKVertexConnectedComponents(IndexGraph g, int k);
	}

	static class ResultImpl implements KVertexConnectedComponentsAlgo.IResult {

		private final IndexGraph g;
		private final List<IntSet> components;

		ResultImpl(IndexGraph g, List<IntSet> components) {
			this.g = g;
			this.components = components;
		}

		@Override
		public int componentsNum() {
			return components.size();
		}

		@Override
		public IntSet componentVertices(int compIndex) {
			return components.get(compIndex);
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public String toString() {
			return components.toString();
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements KVertexConnectedComponentsAlgo.Result<V, E> {

		private final Graph<V, E> g;
		private final KVertexConnectedComponentsAlgo.IResult indexRes;

		ObjResultFromIndexResult(Graph<V, E> g, KVertexConnectedComponentsAlgo.IResult indexRes) {
			this.g = g;
			this.indexRes = indexRes;
		}

		@Override
		public int componentsNum() {
			return indexRes.componentsNum();
		}

		@Override
		public Set<V> componentVertices(int compIndex) {
			return IndexIdMaps.indexToIdSet(indexRes.componentVertices(compIndex), g.indexGraphVerticesMap());
		}

		@Override
		public Graph<V, E> graph() {
			return g;
		}

		@Override
		public String toString() {
			return Range.of(componentsNum()).intStream().mapToObj(this::componentVertices).map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}

	private static class IntResultFromIndexResult implements KVertexConnectedComponentsAlgo.IResult {

		private final IntGraph g;
		private final KVertexConnectedComponentsAlgo.IResult indexRes;

		IntResultFromIndexResult(IntGraph g, KVertexConnectedComponentsAlgo.IResult indexRes) {
			this.g = g;
			this.indexRes = indexRes;
		}

		@Override
		public int componentsNum() {
			return indexRes.componentsNum();
		}

		@Override
		public IntSet componentVertices(int compIndex) {
			return IndexIdMaps.indexToIdSet(indexRes.componentVertices(compIndex), g.indexGraphVerticesMap());
		}

		@Override
		public IntGraph graph() {
			return g;
		}

		@Override
		public String toString() {
			return Range.of(componentsNum()).intStream().mapToObj(this::componentVertices).map(Object::toString)
					.collect(Collectors.joining(", ", "[", "]"));
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> KVertexConnectedComponentsAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			KVertexConnectedComponentsAlgo.IResult indexRes) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (KVertexConnectedComponentsAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexRes);
		} else {
			return new ObjResultFromIndexResult<>(g, indexRes);
		}
	}

}
