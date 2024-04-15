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
package com.jgalgo.alg.dag;

import static com.jgalgo.internal.util.Range.range;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Abstract class for computing a topological order in a DAG graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class TopologicalOrderAlgoAbstract implements TopologicalOrderAlgo {

	/**
	 * Default constructor.
	 */
	public TopologicalOrderAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Optional<TopologicalOrderAlgo.Result<V, E>> computeTopologicalSortingIfExist(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return computeTopologicalSortingIfExist((IndexGraph) g).map(res -> (TopologicalOrderAlgo.Result<V, E>) res);

		} else {
			IndexGraph iGraph = g.indexGraph();
			Optional<TopologicalOrderAlgo.IResult> indexResult = computeTopologicalSortingIfExist(iGraph);
			return indexResult.map(res -> resultFromIndexResult(g, res));
		}
	}

	protected abstract Optional<TopologicalOrderAlgo.IResult> computeTopologicalSortingIfExist(IndexGraph g);

	/**
	 * Result of the topological order algorithm for {@link IndexGraph}.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexResult implements TopologicalOrderAlgo.IResult {

		private final IntList orderedVertices;
		private int[] vertexOrderIndex;

		public IndexResult(int[] topolSort) {
			orderedVertices = Fastutil.list(topolSort);
		}

		@Override
		public IntList orderedVertices() {
			return orderedVertices;
		}

		@Override
		public int vertexOrderIndex(int vertex) {
			if (vertexOrderIndex == null) {
				vertexOrderIndex = new int[orderedVertices.size()];
				for (int i : range(orderedVertices.size()))
					vertexOrderIndex[orderedVertices.getInt(i)] = i;
			}
			if (!(0 <= vertex && vertex < vertexOrderIndex.length))
				throw NoSuchVertexException.ofIndex(vertex);
			return vertexOrderIndex[vertex];
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements TopologicalOrderAlgo.Result<V, E> {

		private final TopologicalOrderAlgo.IResult indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, TopologicalOrderAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public List<V> orderedVertices() {
			return IndexIdMaps.indexToIdList(indexRes.orderedVertices(), viMap);
		}

		@Override
		public int vertexOrderIndex(V vertex) {
			return indexRes.vertexOrderIndex(viMap.idToIndex(vertex));
		}
	}

	private static class IntResultFromIndexResult implements TopologicalOrderAlgo.IResult {

		private final TopologicalOrderAlgo.IResult indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, TopologicalOrderAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public IntList orderedVertices() {
			return IndexIdMaps.indexToIdList(indexRes.orderedVertices(), viMap);
		}

		@Override
		public int vertexOrderIndex(int vertex) {
			return indexRes.vertexOrderIndex(viMap.idToIndex(vertex));
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> TopologicalOrderAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			TopologicalOrderAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (TopologicalOrderAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
