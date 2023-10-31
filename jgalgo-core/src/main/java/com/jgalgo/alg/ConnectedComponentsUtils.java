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

import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;

class ConnectedComponentsUtils {

	static abstract class AbstractStronglyConnectedComponentsAlgo implements StronglyConnectedComponentsAlgo {

		private final WeaklyConnectedComponentsAlgo weaklyConnectedComponentsAlgo =
				WeaklyConnectedComponentsAlgo.newInstance();

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexPartition<V, E> findStronglyConnectedComponents(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (VertexPartition<V, E>) findStronglyConnectedComponents((IndexGraph) g);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IVertexPartition indexResult = findStronglyConnectedComponents(iGraph);
				return (VertexPartition<V, E>) new VertexPartitions.IntPartitionFromIndexPartition((IntGraph) g,
						indexResult);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IVertexPartition indexResult = findStronglyConnectedComponents(iGraph);
				return new VertexPartitions.ObjPartitionFromIndexPartition<>(g, indexResult);
			}
		}

		IVertexPartition findStronglyConnectedComponents(IndexGraph g) {
			if (g.isDirected()) {
				return findStronglyConnectedComponentsDirected(g);
			} else {
				return (IVertexPartition) weaklyConnectedComponentsAlgo.findWeaklyConnectedComponents(g);
			}
		}

		@Override
		public <V, E> boolean isStronglyConnected(Graph<V, E> g) {
			return g instanceof IndexGraph ? isStronglyConnected((IndexGraph) g) : isStronglyConnected(g.indexGraph());
		}

		abstract IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g);

		abstract boolean isStronglyConnected(IndexGraph g);

	}

	static abstract class AbstractWeaklyConnectedComponentsAlgo implements WeaklyConnectedComponentsAlgo {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexPartition<V, E> findWeaklyConnectedComponents(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (VertexPartition<V, E>) findWeaklyConnectedComponents((IndexGraph) g);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IVertexPartition indexResult = findWeaklyConnectedComponents(iGraph);
				return (VertexPartition<V, E>) new VertexPartitions.IntPartitionFromIndexPartition((IntGraph) g,
						indexResult);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IVertexPartition indexResult = findWeaklyConnectedComponents(iGraph);
				return new VertexPartitions.ObjPartitionFromIndexPartition<>(g, indexResult);
			}
		}

		@Override
		public <V, E> boolean isWeaklyConnected(Graph<V, E> g) {
			return g instanceof IndexGraph ? isWeaklyConnected((IndexGraph) g) : isWeaklyConnected(g.indexGraph());
		}

		abstract IVertexPartition findWeaklyConnectedComponents(IndexGraph g);

		abstract boolean isWeaklyConnected(IndexGraph g);

	}

}
