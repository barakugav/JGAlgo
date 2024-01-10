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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;

class IsomorphismTesters {

	private IsomorphismTesters() {}

	static class IndexMapping implements IsomorphismIMapping {

		private final IndexGraph g1;
		private final IndexGraph g2;
		private final int[] vertexMapping;
		private final int[] edgeMapping;
		private IndexMapping inverse;

		IndexMapping(IndexGraph g1, IndexGraph g2, int[] vertexMapping, int[] edgeMapping) {
			this.g1 = g1;
			this.g2 = g2;
			this.vertexMapping = vertexMapping;
			this.edgeMapping = edgeMapping;
		}

		@Override
		public int mapVertex(int vertex) {
			if (!g1.vertices().contains(vertex))
				throw NoSuchVertexException.ofIndex(vertex);
			return vertexMapping[vertex];
		}

		@Override
		public int mapEdge(int edge) {
			if (!g1.edges().contains(edge))
				throw NoSuchEdgeException.ofIndex(edge);
			return edgeMapping[edge];
		}

		@Override
		public IsomorphismIMapping inverse() {
			if (inverse == null) {
				final int n1 = g1.vertices().size();
				final int m1 = g1.edges().size();
				final int n2 = g2.vertices().size();
				final int m2 = g2.edges().size();
				int[] iVertexMapping = new int[n2];
				int[] iEdgeMapping = new int[m2];
				Arrays.fill(iVertexMapping, -1);
				Arrays.fill(iEdgeMapping, -1);
				for (int v1 = 0; v1 < n1; v1++) {
					int v2 = vertexMapping[v1];
					if (v2 >= 0) {
						assert iVertexMapping[v2] < 0;
						iVertexMapping[v2] = v1;
					}
				}
				for (int e1 = 0; e1 < m1; e1++) {
					int e2 = edgeMapping[e1];
					if (e2 >= 0) {
						assert iEdgeMapping[e2] < 0;
						iEdgeMapping[e2] = e1;
					}
				}
				inverse = new IndexMapping(g2, g1, iVertexMapping, iEdgeMapping);
				inverse.inverse = this;
			}
			return inverse;
		}

		@Override
		public IntGraph sourceGraph() {
			return g1;
		}

		@Override
		public IntGraph targetGraph() {
			return g2;
		}

		@Override
		public String toString() {
			return range(g1.vertices().size()).mapToObj(v1 -> {
				int v2 = vertexMapping[v1];
				String v2Str = v2 < 0 ? "null" : String.valueOf(v2);
				return "" + v1 + ":" + v2Str;
			}).collect(Collectors.joining(", ", "{", "}"));
		}
	}

	static class ObjMappingFromIndexMapping<V1, E1, V2, E2> implements IsomorphismMapping<V1, E1, V2, E2> {

		private final IsomorphismIMapping indexMapping;
		private final Graph<V1, E1> g1;
		private final Graph<V2, E2> g2;
		private final IndexIdMap<V1> v1Map;
		private final IndexIdMap<E1> e1Map;
		private final IndexIdMap<V2> v2Map;
		private final IndexIdMap<E2> e2Map;
		private ObjMappingFromIndexMapping<V2, E2, V1, E1> inverse;

		ObjMappingFromIndexMapping(IsomorphismIMapping indexMapping, Graph<V1, E1> g1, Graph<V2, E2> g2) {
			this.indexMapping = indexMapping;
			this.g1 = g1;
			this.g2 = g2;
			this.v1Map = g1.indexGraphVerticesMap();
			this.e1Map = g1.indexGraphEdgesMap();
			this.v2Map = g2.indexGraphVerticesMap();
			this.e2Map = g2.indexGraphEdgesMap();
		}

		@Override
		public V2 mapVertex(V1 vertex) {
			int v1 = v1Map.idToIndex(vertex);
			int v2 = indexMapping.mapVertex(v1);
			return v2 < 0 ? null : v2Map.indexToId(v2);

		}

		@Override
		public E2 mapEdge(E1 edge) {
			int e1 = e1Map.idToIndex(edge);
			int e2 = indexMapping.mapEdge(e1);
			return e2 < 0 ? null : e2Map.indexToId(e2);
		}

		@Override
		public IsomorphismMapping<V2, E2, V1, E1> inverse() {
			if (inverse == null) {
				inverse = new ObjMappingFromIndexMapping<>(indexMapping.inverse(), g2, g1);
				inverse.inverse = this;
			}
			return inverse;
		}

		@Override
		public Graph<V1, E1> sourceGraph() {
			return g1;
		}

		@Override
		public Graph<V2, E2> targetGraph() {
			return g2;
		}

		@Override
		public String toString() {
			return range(g1.indexGraph().vertices().size()).mapToObj(v1Idx -> {
				V1 v1 = v1Map.indexToId(v1Idx);
				int v2Idx = indexMapping.mapVertex(v1Idx);
				V2 v2 = v2Idx < 0 ? null : v2Map.indexToId(v2Idx);
				return "" + v1 + ":" + v2;
			}).collect(Collectors.joining(", ", "{", "}"));
		}
	}

	static class IntMappingFromIndexMapping implements IsomorphismIMapping {

		private final IsomorphismIMapping indexMapping;
		private final IntGraph g1;
		private final IntGraph g2;
		private final IndexIntIdMap v1Map;
		private final IndexIntIdMap e1Map;
		private final IndexIntIdMap v2Map;
		private final IndexIntIdMap e2Map;
		private IntMappingFromIndexMapping inverse;

		IntMappingFromIndexMapping(IsomorphismIMapping indexMapping, IntGraph g1, IntGraph g2) {
			this.indexMapping = indexMapping;
			this.g1 = g1;
			this.g2 = g2;
			this.v1Map = g1.indexGraphVerticesMap();
			this.e1Map = g1.indexGraphEdgesMap();
			this.v2Map = g2.indexGraphVerticesMap();
			this.e2Map = g2.indexGraphEdgesMap();
		}

		@Override
		public int mapVertex(int vertex) {
			int v1 = v1Map.idToIndex(vertex);
			int v2 = indexMapping.mapVertex(v1);
			return v2 < 0 ? -1 : v2Map.indexToIdInt(v2);

		}

		@Override
		public int mapEdge(int edge) {
			int e1 = e1Map.idToIndex(edge);
			int e2 = indexMapping.mapEdge(e1);
			return e2 < 0 ? -1 : e2Map.indexToIdInt(e2);
		}

		@Override
		public IsomorphismIMapping inverse() {
			if (inverse == null) {
				inverse = new IntMappingFromIndexMapping(indexMapping.inverse(), g2, g1);
				inverse.inverse = this;
			}
			return inverse;
		}

		@Override
		public IntGraph sourceGraph() {
			return g1;
		}

		@Override
		public IntGraph targetGraph() {
			return g2;
		}

		@Override
		public String toString() {
			return range(g1.indexGraph().vertices().size()).mapToObj(v1Idx -> {
				int v1 = v1Map.indexToIdInt(v1Idx);
				int v2Idx = indexMapping.mapVertex(v1Idx);
				String v2 = v2Idx < 0 ? "null" : String.valueOf(v2Map.indexToIdInt(v2Idx));
				return "" + v1 + ":" + v2;
			}).collect(Collectors.joining(", ", "{", "}"));
		}
	}

}
