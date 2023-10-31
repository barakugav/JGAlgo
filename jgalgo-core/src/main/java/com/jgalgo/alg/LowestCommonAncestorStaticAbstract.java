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

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;

abstract class LowestCommonAncestorStaticAbstract implements LowestCommonAncestorStatic {

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> LowestCommonAncestorStatic.DataStructure<V, E> preProcessTree(Graph<V, E> tree, V root) {
		if (tree instanceof IndexGraph) {
			return (LowestCommonAncestorStatic.DataStructure<V, E>) preProcessTree((IndexGraph) tree,
					((Integer) root).intValue());

		} else if (tree instanceof IntGraph) {
			IndexGraph iGraph = tree.indexGraph();
			IndexIntIdMap viMap = ((IntGraph) tree).indexGraphVerticesMap();
			int iRoot = viMap.idToIndex(((Integer) root).intValue());
			LowestCommonAncestorStatic.IDataStructure indexResult = preProcessTree(iGraph, iRoot);
			return (LowestCommonAncestorStatic.DataStructure<V, E>) new IntDsFromIndexDs(indexResult, viMap);

		} else {
			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap<V> viMap = tree.indexGraphVerticesMap();
			int iRoot = viMap.idToIndex(root);
			LowestCommonAncestorStatic.IDataStructure indexResult = preProcessTree(iGraph, iRoot);
			return new ObjDsFromIndexDs<>(indexResult, viMap);
		}
	}

	abstract LowestCommonAncestorStatic.IDataStructure preProcessTree(IndexGraph tree, int root);

	private static class IntDsFromIndexDs implements LowestCommonAncestorStatic.IDataStructure {

		private final LowestCommonAncestorStatic.IDataStructure indexDs;
		private final IndexIntIdMap viMap;

		IntDsFromIndexDs(LowestCommonAncestorStatic.IDataStructure indexDs, IndexIntIdMap viMap) {
			this.indexDs = Objects.requireNonNull(indexDs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int findLca(int u, int v) {
			return viMap.indexToIdInt(indexDs.findLca(viMap.idToIndex(u), viMap.idToIndex(v)));
		}
	}

	private static class ObjDsFromIndexDs<V, E> implements LowestCommonAncestorStatic.DataStructure<V, E> {

		private final LowestCommonAncestorStatic.IDataStructure indexDs;
		private final IndexIdMap<V> viMap;

		ObjDsFromIndexDs(LowestCommonAncestorStatic.IDataStructure indexDs, IndexIdMap<V> viMap) {
			this.indexDs = Objects.requireNonNull(indexDs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public V findLca(V u, V v) {
			return viMap.indexToId(indexDs.findLca(viMap.idToIndex(u), viMap.idToIndex(v)));
		}
	}

}
