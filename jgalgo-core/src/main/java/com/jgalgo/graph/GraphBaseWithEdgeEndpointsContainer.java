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
package com.jgalgo.graph;

import com.jgalgo.graph.EdgeEndpointsContainer.GraphWithEdgeEndpointsContainer;

abstract class GraphBaseWithEdgeEndpointsContainer extends GraphBaseMutable implements GraphWithEdgeEndpointsContainer {

	private long[] edgeEndpoints;
	private final DataContainer.Long edgeEndpointsContainer;

	GraphBaseWithEdgeEndpointsContainer(IndexGraphBase.Capabilities capabilities, int expectedVerticesNum,
			int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
		edgeEndpointsContainer =
				new DataContainer.Long(edges, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
		addInternalEdgesContainer(edgeEndpointsContainer);
	}

	GraphBaseWithEdgeEndpointsContainer(IndexGraphBase.Capabilities capabilities, IndexGraph g,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
		if (g instanceof GraphBaseWithEdgeEndpointsContainer) {
			GraphBaseWithEdgeEndpointsContainer g0 = (GraphBaseWithEdgeEndpointsContainer) g;
			edgeEndpointsContainer = g0.edgeEndpointsContainer.copy(edges, newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
		} else {

			final int m = edges.size();
			edgeEndpointsContainer =
					new DataContainer.Long(edges, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
			for (int e = 0; e < m; e++)
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}
	}

	GraphBaseWithEdgeEndpointsContainer(IndexGraphBase.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
		final int m = edges.size();
		edgeEndpointsContainer =
				new DataContainer.Long(edges, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
		addInternalEdgesContainer(edgeEndpointsContainer);

		for (int e = 0; e < m; e++)
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		setEndpoints(e, source, target);
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		clear(edgeEndpoints, edge, EdgeEndpointsContainer.DefVal);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		swapAndClear(edgeEndpoints, removedIdx, swappedIdx, EdgeEndpointsContainer.DefVal);
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	void reverseEdge0(int edge) {
		EdgeEndpointsContainer.reverseEdge(edgeEndpoints, edge);
	}

	@Override
	public void clearEdges() {
		edgeEndpointsContainer.clear();
		super.clearEdges();
	}

	@Override
	public long[] edgeEndpoints() {
		return edgeEndpoints;
	}

}
