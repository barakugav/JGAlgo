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
package com.jgalgo.alg.tree;

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;

/**
 * Abstract class for static LCA data structures.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class LowestCommonAncestorStaticAbstract implements LowestCommonAncestorStatic {

	/**
	 * Default constructor.
	 */
	public LowestCommonAncestorStaticAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> LowestCommonAncestorStatic.DataStructure<V, E> preProcessTree(Graph<V, E> tree, V root) {
		if (tree instanceof IndexGraph) {
			return (LowestCommonAncestorStatic.DataStructure<V, E>) preProcessTree((IndexGraph) tree,
					((Integer) root).intValue());

		} else {
			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap<V> viMap = tree.indexGraphVerticesMap();
			int iRoot = viMap.idToIndex(root);
			LowestCommonAncestorStatic.IDataStructure indexResult = preProcessTree(iGraph, iRoot);
			return dsFromIndexDs(tree, indexResult);
		}
	}

	protected abstract LowestCommonAncestorStatic.IDataStructure preProcessTree(IndexGraph tree, int root);

	private static class IntDsFromIndexDs implements LowestCommonAncestorStatic.IDataStructure {

		private final LowestCommonAncestorStatic.IDataStructure indexDs;
		private final IndexIntIdMap viMap;

		IntDsFromIndexDs(IntGraph g, LowestCommonAncestorStatic.IDataStructure indexDs) {
			this.indexDs = Objects.requireNonNull(indexDs);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public int findLca(int u, int v) {
			return viMap.indexToIdInt(indexDs.findLca(viMap.idToIndex(u), viMap.idToIndex(v)));
		}
	}

	private static class ObjDsFromIndexDs<V, E> implements LowestCommonAncestorStatic.DataStructure<V, E> {

		private final LowestCommonAncestorStatic.IDataStructure indexDs;
		private final IndexIdMap<V> viMap;

		ObjDsFromIndexDs(Graph<V, E> g, LowestCommonAncestorStatic.IDataStructure indexDs) {
			this.indexDs = Objects.requireNonNull(indexDs);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public V findLca(V u, V v) {
			return viMap.indexToId(indexDs.findLca(viMap.idToIndex(u), viMap.idToIndex(v)));
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> LowestCommonAncestorStatic.DataStructure<V, E> dsFromIndexDs(Graph<V, E> g,
			LowestCommonAncestorStatic.IDataStructure indexDs) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (LowestCommonAncestorStatic.DataStructure<V, E>) new IntDsFromIndexDs((IntGraph) g, indexDs);
		} else {
			return new ObjDsFromIndexDs<>(g, indexDs);
		}
	}

}
