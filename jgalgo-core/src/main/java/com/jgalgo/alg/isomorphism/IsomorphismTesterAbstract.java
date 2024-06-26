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
package com.jgalgo.alg.isomorphism;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing isomorphism mapping between two graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class IsomorphismTesterAbstract implements IsomorphismTester {

	/**
	 * Default constructor.
	 */
	public IsomorphismTesterAbstract() {}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, boolean induced, BiPredicate<? super V1, ? super V2> vertexMatcher,
			BiPredicate<? super E1, ? super E2> edgeMatcher) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			IntMatcher vMatcher = asIntBiMatcher((BiPredicate<? super Integer, ? super Integer>) vertexMatcher);
			IntMatcher eMatcher = asIntBiMatcher((BiPredicate<? super Integer, ? super Integer>) edgeMatcher);
			return (Iterator) isomorphicMappingsIter((IndexGraph) g1, (IndexGraph) g2, induced, vMatcher, eMatcher);

		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			IntMatcher vMatcher = mapMatcher(vertexMatcher, g1.indexGraphVerticesMap(), g2.indexGraphVerticesMap());
			IntMatcher eMatcher = mapMatcher(edgeMatcher, g1.indexGraphEdgesMap(), g2.indexGraphEdgesMap());
			Iterator<IsomorphismIMapping> iMappingsIter = isomorphicMappingsIter(ig1, ig2, induced, vMatcher, eMatcher);
			return IterTools.map(iMappingsIter, m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	protected abstract Iterator<IsomorphismIMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2,
			boolean induced, IntMatcher vertexMatcher, IntMatcher edgeMatcher);

	private static IntMatcher asIntBiMatcher(BiPredicate<? super Integer, ? super Integer> matcher) {
		if (matcher == null)
			return null;
		if (matcher instanceof IntMatcher)
			return (IntMatcher) matcher;
		return (t, u) -> matcher.test(Integer.valueOf(t), Integer.valueOf(u));
	}

	private static <K1, K2> IntMatcher mapMatcher(BiPredicate<? super K1, ? super K2> matcher, IndexIdMap<K1> map1,
			IndexIdMap<K2> map2) {
		if (matcher == null)
			return null;
		if (matcher instanceof IntMatcher && map1 instanceof IndexIntIdMap && map2 instanceof IndexIntIdMap) {
			IntMatcher matcher0 = (IntMatcher) matcher;
			IndexIntIdMap map10 = (IndexIntIdMap) map1;
			IndexIntIdMap map20 = (IndexIntIdMap) map2;
			return (aIdx, bIdx) -> matcher0.isMatch(map10.indexToIdInt(aIdx), map20.indexToIdInt(bIdx));
		} else {
			return (aIdx, bIdx) -> matcher.test(map1.indexToId(aIdx), map2.indexToId(bIdx));
		}
	}

	/**
	 * Functional interface for predicting if two vertices or edges can be matched during isomorphism testing.
	 *
	 * @author Barak Ugav
	 */
	@FunctionalInterface
	protected static interface IntMatcher extends BiPredicate<Integer, Integer> {

		boolean isMatch(int t, int u);

		@Override
		default boolean test(Integer t, Integer u) {
			return isMatch(t.intValue(), u.intValue());
		}
	}

	/**
	 * A result of an isomorphism test between two index graphs.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexMapping implements IsomorphismIMapping {

		private final IndexGraph g1;
		private final IndexGraph g2;
		private final int[] vertexMapping;
		private final int[] edgeMapping;
		private IndexMapping inverse;
		private IntSet mappedVertices;
		private IntSet mappedEdges;

		/**
		 * Construct a new mapping for an isomorphism between two index graphs.
		 *
		 * @param g1            the source graph
		 * @param g2            the target graph
		 * @param vertexMapping an array representing the mapping of vertices from the source graph to the target graph,
		 *                          using an index to index mapping, or {@code -1} for an unmapped vertex. The provided
		 *                          array <b>is not copied</b> and should not be modified after the mapping is created
		 * @param edgeMapping   an array representing the mapping of edges from the source graph to the target graph,
		 *                          using an index to index mapping, or {@code -1} for an unmapped edge. The provided
		 *                          array <b>is not copied</b> and should not be modified after the mapping is created
		 */
		public IndexMapping(IndexGraph g1, IndexGraph g2, int[] vertexMapping, int[] edgeMapping) {
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
				for (int v1 : range(n1)) {
					int v2 = vertexMapping[v1];
					if (v2 >= 0) {
						assert iVertexMapping[v2] < 0;
						iVertexMapping[v2] = v1;
					}
				}
				for (int e1 : range(m1)) {
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
		public IntSet mappedVertices() {
			if (mappedVertices == null) {
				int[] mappedVerticesArr = range(g1.vertices().size()).filter(v1 -> vertexMapping[v1] >= 0).toArray();
				mappedVertices = ImmutableIntArraySet
						.newInstance(mappedVerticesArr,
								v -> 0 <= v && v < vertexMapping.length && vertexMapping[v] >= 0);
			}
			return mappedVertices;
		}

		@Override
		public IntSet mappedEdges() {
			if (mappedEdges == null) {
				int[] mappedEdgesArr = range(g1.edges().size()).filter(e1 -> edgeMapping[e1] >= 0).toArray();
				mappedEdges = ImmutableIntArraySet
						.newInstance(mappedEdgesArr, e -> 0 <= e && e < edgeMapping.length && edgeMapping[e] >= 0);
			}
			return mappedEdges;
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

	@SuppressWarnings("unchecked")
	private static <V1, E1, V2, E2> IsomorphismMapping<V1, E1, V2, E2> mappingFromIndexMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismIMapping indexMapping) {
		assert !(g1 instanceof IndexGraph && g2 instanceof IndexGraph);
		if (g1 instanceof IntGraph && g2 instanceof IntGraph) {
			return (IsomorphismMapping<V1, E1, V2, E2>) new IntMappingFromIndexMapping(indexMapping, (IntGraph) g1,
					(IntGraph) g2);
		} else {
			return new ObjMappingFromIndexMapping<>(indexMapping, g1, g2);
		}
	}

	private static class ObjMappingFromIndexMapping<V1, E1, V2, E2> implements IsomorphismMapping<V1, E1, V2, E2> {

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
		public Set<V1> mappedVertices() {
			return IndexIdMaps.indexToIdSet(indexMapping.mappedVertices(), v1Map);
		}

		@Override
		public Set<E1> mappedEdges() {
			return IndexIdMaps.indexToIdSet(indexMapping.mappedEdges(), e1Map);
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

	private static class IntMappingFromIndexMapping implements IsomorphismIMapping {

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
		public IntSet mappedVertices() {
			return IndexIdMaps.indexToIdSet(indexMapping.mappedVertices(), v1Map);
		}

		@Override
		public IntSet mappedEdges() {
			return IndexIdMaps.indexToIdSet(indexMapping.mappedEdges(), e1Map);
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
