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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;

class IsomorphismTesters {

	private IsomorphismTesters() {}

	static class IndexMapping implements IsomorphismIMapping {

		private final int[] vertexMapping;
		private final int[] edgeMapping;
		private IndexMapping inverse;

		IndexMapping(int[] vertexMapping, int[] edgeMapping) {
			this.vertexMapping = vertexMapping;
			this.edgeMapping = edgeMapping;

			// final int n = vertexMapping.length;
			// final int m = edgeMapping.length;
			// assert range(n).allMatch(v -> 0 <= vertexMapping[v] && vertexMapping[v] < n);
			// assert range(m).allMatch(e -> 0 <= edgeMapping[e] && edgeMapping[e] < m);
			// assert range(n).map(v -> vertexMapping[v]).distinct().count() == n;
			// assert range(m).map(e -> edgeMapping[e]).distinct().count() == m;
		}

		@Override
		public int mapVertex(int vertex) {
			if (!(0 <= vertex && vertex < vertexMapping.length))
				throw NoSuchVertexException.ofIndex(vertex);
			return vertexMapping[vertex];
		}

		@Override
		public int mapEdge(int edge) {
			if (!(0 <= edge && edge < edgeMapping.length))
				throw NoSuchEdgeException.ofIndex(edge);
			return edgeMapping[edge];
		}

		@Override
		public IsomorphismIMapping inverse() {
			if (inverse == null) {
				final int n = vertexMapping.length;
				final int m = edgeMapping.length;
				int[] iVertexMapping = new int[n];
				int[] iEdgeMapping = new int[m];
				for (int v = 0; v < n; v++)
					iVertexMapping[vertexMapping[v]] = v;
				for (int e = 0; e < m; e++)
					iEdgeMapping[edgeMapping[e]] = e;
				inverse = new IndexMapping(iVertexMapping, iEdgeMapping);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

	static class ObjMappingFromIndexMapping<V1, E1, V2, E2> implements IsomorphismMapping<V1, E1, V2, E2> {

		private final IsomorphismIMapping indexMapping;
		private final IndexIdMap<V1> v1Map;
		private final IndexIdMap<E1> e1Map;
		private final IndexIdMap<V2> v2Map;
		private final IndexIdMap<E2> e2Map;
		private ObjMappingFromIndexMapping<V2, E2, V1, E1> inverse;

		ObjMappingFromIndexMapping(IsomorphismIMapping indexMapping, Graph<V1, E1> g1, Graph<V2, E2> g2) {
			this(indexMapping, g1.indexGraphVerticesMap(), g1.indexGraphEdgesMap(), g2.indexGraphVerticesMap(),
					g2.indexGraphEdgesMap());
		}

		private ObjMappingFromIndexMapping(IsomorphismIMapping indexMapping, IndexIdMap<V1> v1Map, IndexIdMap<E1> e1Map,
				IndexIdMap<V2> v2Map, IndexIdMap<E2> e2Map) {
			this.indexMapping = indexMapping;
			this.v1Map = v1Map;
			this.e1Map = e1Map;
			this.v2Map = v2Map;
			this.e2Map = e2Map;
		}

		@Override
		public V2 mapVertex(V1 vertex) {
			int v1 = v1Map.idToIndex(vertex);
			int v2 = indexMapping.mapVertex(v1);
			return v2Map.indexToId(v2);

		}

		@Override
		public E2 mapEdge(E1 edge) {
			int e1 = e1Map.idToIndex(edge);
			int e2 = indexMapping.mapEdge(e1);
			return e2Map.indexToId(e2);
		}

		@Override
		public IsomorphismMapping<V2, E2, V1, E1> inverse() {
			if (inverse == null) {
				inverse = new ObjMappingFromIndexMapping<>(indexMapping.inverse(), v2Map, e2Map, v1Map, e1Map);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

	static class IntMappingFromIndexMapping implements IsomorphismIMapping {

		private final IsomorphismIMapping indexMapping;
		private final IndexIntIdMap v1Map;
		private final IndexIntIdMap e1Map;
		private final IndexIntIdMap v2Map;
		private final IndexIntIdMap e2Map;
		private IntMappingFromIndexMapping inverse;

		IntMappingFromIndexMapping(IsomorphismIMapping indexMapping, IntGraph g1, IntGraph g2) {
			this(indexMapping, g1.indexGraphVerticesMap(), g1.indexGraphEdgesMap(), g2.indexGraphVerticesMap(),
					g2.indexGraphEdgesMap());
		}

		private IntMappingFromIndexMapping(IsomorphismIMapping indexMapping, IndexIntIdMap v1Map, IndexIntIdMap e1Map,
				IndexIntIdMap v2Map, IndexIntIdMap e2Map) {
			this.indexMapping = indexMapping;
			this.v1Map = v1Map;
			this.e1Map = e1Map;
			this.v2Map = v2Map;
			this.e2Map = e2Map;
		}

		@Override
		public int mapVertex(int vertex) {
			int v1 = v1Map.idToIndex(vertex);
			int v2 = indexMapping.mapVertex(v1);
			return v2Map.indexToIdInt(v2);

		}

		@Override
		public int mapEdge(int edge) {
			int e1 = e1Map.idToIndex(edge);
			int e2 = indexMapping.mapEdge(e1);
			return e2Map.indexToIdInt(e2);
		}

		@Override
		public IsomorphismIMapping inverse() {
			if (inverse == null) {
				inverse = new IntMappingFromIndexMapping(indexMapping.inverse(), v2Map, e2Map, v1Map, e1Map);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

}
