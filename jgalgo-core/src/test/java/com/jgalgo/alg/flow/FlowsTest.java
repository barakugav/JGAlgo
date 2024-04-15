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
package com.jgalgo.alg.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

public class FlowsTest extends TestBase {

	@SuppressWarnings("boxing")
	@Test
	public void testFlowAndCostSumDirected() {
		foreachBoolConfig(intGraph -> {
			Graph<Integer, Integer> g = intGraph ? IntGraph.newDirected() : Graph.newDirected();
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addEdge(0, 2, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(2, 0, 2);

			double[] flows = new double[g.edges().size()];
			IFlow flow0 = new Flows.IndexFlow(g.indexGraph(), flows);
			Flow<Integer, Integer> flow = Flows.flowFromIndexFlow(g, flow0);

			WeightsDouble<Integer> costDouble = Weights.createExternalEdgesWeights(g, double.class);
			WeightsInt<Integer> costInt = Weights.createExternalEdgesWeights(g, int.class);
			for (Integer e : g.edges()) {
				costDouble.set(e, e.intValue() + 1);
				costInt.set(e, e.intValue() + 1);
			}

			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();

			assertEquals(0, flow.getSupply(0));
			assertEquals(0, flow.getTotalCost(costInt));
			assertEquals(0, flow.getTotalCost(costDouble));

			flows[viMap.idToIndex(0)] = 1;
			assertEquals(1, flow.getSupply(0));
			assertEquals(1, flow.getTotalCost(costInt));
			assertEquals(1, flow.getTotalCost(costDouble));

			flows[viMap.idToIndex(1)] = 1;
			assertEquals(1, flow.getSupply(0));
			assertEquals(2, flow.getSupplySubset(IntList.of(0, 1)));
			assertEquals(3, flow.getTotalCost(costInt));
			assertEquals(3, flow.getTotalCost(costDouble));
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void testFlowAndCostSumUndirected() {
		foreachBoolConfig(intGraph -> {
			GraphFactory<Integer, Integer> factory =
					intGraph ? IntGraphFactory.undirected() : GraphFactory.undirected();
			Graph<Integer, Integer> g = factory.allowParallelEdges().newGraph();
			g.addVertex(0);
			g.addVertex(1);
			g.addVertex(2);
			g.addEdge(0, 2, 0);
			g.addEdge(1, 2, 1);
			g.addEdge(2, 0, 2);

			double[] flows = new double[g.edges().size()];
			IFlow flow0 = new Flows.IndexFlow(g.indexGraph(), flows);
			Flow<Integer, Integer> flow = Flows.flowFromIndexFlow(g, flow0);

			WeightsDouble<Integer> costDouble = Weights.createExternalEdgesWeights(g, double.class);
			WeightsInt<Integer> costInt = Weights.createExternalEdgesWeights(g, int.class);
			for (Integer e : g.edges()) {
				costDouble.set(e, e.intValue() + 1);
				costInt.set(e, e.intValue() + 1);
			}

			IndexIdMap<Integer> viMap = g.indexGraphVerticesMap();

			assertEquals(0, flow.getSupply(0));
			assertEquals(0, flow.getTotalCost(costInt));
			assertEquals(0, flow.getTotalCost(costDouble));

			flows[viMap.idToIndex(0)] = 1;
			assertEquals(1, flow.getSupply(0));
			assertEquals(1, flow.getTotalCost(costInt));
			assertEquals(1, flow.getTotalCost(costDouble));

			flows[viMap.idToIndex(1)] = 1;
			assertEquals(1, flow.getSupply(0));
			assertEquals(2, flow.getSupplySubset(IntList.of(0, 1)));
			assertEquals(3, flow.getTotalCost(costInt));
			assertEquals(3, flow.getTotalCost(costDouble));
		});
	}

}
