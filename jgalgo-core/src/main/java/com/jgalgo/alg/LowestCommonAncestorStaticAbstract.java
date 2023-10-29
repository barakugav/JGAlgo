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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;

abstract class LowestCommonAncestorStaticAbstract implements LowestCommonAncestorStatic {

	@Override
	public LowestCommonAncestorStatic.DataStructure preProcessTree(IntGraph tree, int root) {
		if (tree instanceof IndexGraph)
			return preProcessTree((IndexGraph) tree, root);

		IndexGraph iGraph = tree.indexGraph();
		IndexIntIdMap viMap = tree.indexGraphVerticesMap();

		int iRoot = viMap.idToIndex(root);
		LowestCommonAncestorStatic.DataStructure indexResult = preProcessTree(iGraph, iRoot);
		return new DSFromIndexDS(indexResult, viMap);
	}

	abstract LowestCommonAncestorStatic.DataStructure preProcessTree(IndexGraph tree, int root);

	private static class DSFromIndexDS implements LowestCommonAncestorStatic.DataStructure {

		private final LowestCommonAncestorStatic.DataStructure ds;
		private final IndexIntIdMap viMap;

		DSFromIndexDS(LowestCommonAncestorStatic.DataStructure ds, IndexIntIdMap viMap) {
			this.ds = Objects.requireNonNull(ds);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int findLowestCommonAncestor(int u, int v) {
			return viMap.indexToIdInt(ds.findLowestCommonAncestor(viMap.idToIndex(u), viMap.idToIndex(v)));
		}

	}

}
