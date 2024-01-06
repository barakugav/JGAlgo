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

import java.util.Iterator;
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.IterTools;

abstract class IsomorphismTesterAbstract implements IsomorphismTester {

	@Override
	public <V1, E1, V2, E2> boolean isIsomorphic(Graph<V1, E1> g1, Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return isIsomorphic((IndexGraph) g1, (IndexGraph) g2);
		} else {
			return isIsomorphic(g1.indexGraph(), g2.indexGraph());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V1, E1, V2, E2> Optional<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return (Optional) isomorphicMapping((IndexGraph) g1, (IndexGraph) g2);
		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			Optional<IsomorphismTester.IMapping> iMapping = isomorphicMapping(ig1, ig2);
			return iMapping.map(m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V1, E1, V2, E2> Iterator<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return (Iterator) isomorphicMappingsIter((IndexGraph) g1, (IndexGraph) g2);
		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			Iterator<IsomorphismTester.IMapping> iMappingsIter = isomorphicMappingsIter(ig1, ig2);
			return IterTools.map(iMappingsIter, m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	boolean isIsomorphic(IndexGraph g1, IndexGraph g2) {
		return isomorphicMapping(g1, g2).isPresent();
	}

	Optional<IsomorphismTester.IMapping> isomorphicMapping(IndexGraph g1, IndexGraph g2) {
		Iterator<IsomorphismTester.IMapping> iter = isomorphicMappingsIter(g1, g2);
		return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
	}

	abstract Iterator<IsomorphismTester.IMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2);

	static class MappingImpl implements IsomorphismTester.IMapping {

		private final int[] vertexMapping;
		private final int[] edgeMapping;
		private MappingImpl inverse;

		MappingImpl(int[] vertexMapping, int[] edgeMapping) {
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
		public IMapping inverse() {
			if (inverse == null) {
				final int n = vertexMapping.length;
				final int m = edgeMapping.length;
				int[] iVertexMapping = new int[n];
				int[] iEdgeMapping = new int[m];
				for (int v = 0; v < n; v++)
					iVertexMapping[vertexMapping[v]] = v;
				for (int e = 0; e < m; e++)
					iEdgeMapping[edgeMapping[e]] = e;
				inverse = new MappingImpl(iVertexMapping, iEdgeMapping);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

	static class ObjMappingFromIndexMapping<V1, E1, V2, E2> implements IsomorphismTester.Mapping<V1, E1, V2, E2> {

		private final IsomorphismTester.IMapping indexMapping;
		private final IndexIdMap<V1> v1Map;
		private final IndexIdMap<E1> e1Map;
		private final IndexIdMap<V2> v2Map;
		private final IndexIdMap<E2> e2Map;
		private ObjMappingFromIndexMapping<V2, E2, V1, E1> inverse;

		ObjMappingFromIndexMapping(IsomorphismTester.IMapping indexMapping, Graph<V1, E1> g1, Graph<V2, E2> g2) {
			this(indexMapping, g1.indexGraphVerticesMap(), g1.indexGraphEdgesMap(), g2.indexGraphVerticesMap(),
					g2.indexGraphEdgesMap());
		}

		private ObjMappingFromIndexMapping(IsomorphismTester.IMapping indexMapping, IndexIdMap<V1> v1Map,
				IndexIdMap<E1> e1Map, IndexIdMap<V2> v2Map, IndexIdMap<E2> e2Map) {
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
		public IsomorphismTester.Mapping<V2, E2, V1, E1> inverse() {
			if (inverse == null) {
				inverse = new ObjMappingFromIndexMapping<>(indexMapping.inverse(), v2Map, e2Map, v1Map, e1Map);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

	static class IntMappingFromIndexMapping implements IsomorphismTester.IMapping {

		private final IsomorphismTester.IMapping indexMapping;
		private final IndexIntIdMap v1Map;
		private final IndexIntIdMap e1Map;
		private final IndexIntIdMap v2Map;
		private final IndexIntIdMap e2Map;
		private IntMappingFromIndexMapping inverse;

		IntMappingFromIndexMapping(IsomorphismTester.IMapping indexMapping, IntGraph g1, IntGraph g2) {
			this(indexMapping, g1.indexGraphVerticesMap(), g1.indexGraphEdgesMap(), g2.indexGraphVerticesMap(),
					g2.indexGraphEdgesMap());
		}

		private IntMappingFromIndexMapping(IsomorphismTester.IMapping indexMapping, IndexIntIdMap v1Map,
				IndexIntIdMap e1Map, IndexIntIdMap v2Map, IndexIntIdMap e2Map) {
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
		public IsomorphismTester.IMapping inverse() {
			if (inverse == null) {
				inverse = new IntMappingFromIndexMapping(indexMapping.inverse(), v2Map, e2Map, v1Map, e1Map);
				inverse.inverse = this;
			}
			return inverse;
		}
	}

	@SuppressWarnings("unchecked")
	private static <V1, E1, V2, E2> IsomorphismTester.Mapping<V1, E1, V2, E2> mappingFromIndexMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismTester.IMapping indexMapping) {
		assert !(g1 instanceof IndexGraph && g2 instanceof IndexGraph);
		if (g1 instanceof IntGraph && g2 instanceof IntGraph) {
			return (IsomorphismTester.Mapping<V1, E1, V2, E2>) new IntMappingFromIndexMapping(indexMapping,
					(IntGraph) g1, (IntGraph) g2);
		} else {
			return new ObjMappingFromIndexMapping<>(indexMapping, g1, g2);
		}
	}

}
