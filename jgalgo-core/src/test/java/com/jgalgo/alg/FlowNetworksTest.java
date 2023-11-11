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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntList;

public class FlowNetworksTest extends TestBase {

	@Test
	public void testIndexNetFromNet() {
		final long seed = 0xa425f113137a4ee3L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(200).parallelEdges(true)
				.selfEdges(true).cycles(true).connected(false).build();
		FlowNetwork<Integer, Integer> net = MaximumFlowTestUtils.randNetwork(g, seedGen.nextSeed());

		IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
		IFlowNetwork indexNet = FlowNetworks.indexNetFromNet(net, eiMap);

		for (Integer e : g.edges())
			assertEquals(net.getCapacity(e), indexNet.getCapacity(eiMap.idToIndex(e)));
		for (Integer e : g.edges())
			assertEquals(net.getFlow(e), indexNet.getFlow(eiMap.idToIndex(e)));

		for (Integer e : g.edges()) {
			if (rand.nextBoolean()) {
				net.setCapacity(e, rand.nextInt(100));
			} else {
				indexNet.setCapacity(eiMap.idToIndex(e), rand.nextInt(100));
			}
		}
		for (Integer e : g.edges())
			assertEquals(net.getCapacity(e), indexNet.getCapacity(eiMap.idToIndex(e)));

		for (Integer e : g.edges()) {
			if (rand.nextBoolean()) {
				net.setFlow(e, rand.nextDouble() * net.getCapacity(e));
			} else {
				indexNet.setFlow(eiMap.idToIndex(e), rand.nextDouble() * indexNet.getCapacity(eiMap.idToIndex(e)));
			}
		}
		for (Integer e : g.edges())
			assertEquals(net.getFlow(e), indexNet.getFlow(eiMap.idToIndex(e)));
	}

	@Test
	public void testIndexNetFromNetInt() {
		final long seed = 0x3593c82bada078bcL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());

		Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(100).m(200).parallelEdges(true)
				.selfEdges(true).cycles(true).connected(false).build();
		FlowNetworkInt<Integer, Integer> net = MaximumFlowTestUtils.randNetworkInt(g, seedGen.nextSeed());

		IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
		IFlowNetworkInt indexNet = (IFlowNetworkInt) FlowNetworks.indexNetFromNet(net, eiMap);

		for (Integer e : g.edges())
			assertEquals(net.getCapacityInt(e), indexNet.getCapacityInt(eiMap.idToIndex(e)));
		for (Integer e : g.edges())
			assertEquals(net.getFlowInt(e), indexNet.getFlowInt(eiMap.idToIndex(e)));

		for (Integer e : g.edges()) {
			switch (rand.nextInt(4)) {
				case 0:
					net.setCapacity(e, rand.nextInt(100));
					break;
				case 1:
					((FlowNetwork<Integer, Integer>) net).setCapacity(e, rand.nextInt(100));
					break;
				case 2:
					indexNet.setCapacity(eiMap.idToIndex(e), rand.nextInt(100));
					break;
				case 3:
					((IFlowNetwork) indexNet).setCapacity(eiMap.idToIndex(e), rand.nextInt(100));
					break;
				default:
					throw new AssertionError();
			}
		}
		for (Integer e : g.edges()) {
			assertEquals(net.getCapacityInt(e), indexNet.getCapacityInt(eiMap.idToIndex(e)));
			assertEquals(((FlowNetwork<Integer, Integer>) net).getCapacity(e),
					((IFlowNetwork) indexNet).getCapacity(eiMap.idToIndex(e)));
		}

		for (Integer e : g.edges()) {
			switch (rand.nextInt(4)) {
				case 0:
					net.setFlow(e, (int) (rand.nextDouble() * net.getCapacityInt(e)));
					break;
				case 1:
					((FlowNetwork<Integer, Integer>) net).setFlow(e, (int) (rand.nextDouble() * net.getCapacityInt(e)));
					break;
				case 2:
					indexNet.setFlow(eiMap.idToIndex(e),
							(int) (rand.nextDouble() * indexNet.getCapacityInt(eiMap.idToIndex(e))));
					break;
				case 3:
					((IFlowNetwork) indexNet).setFlow(eiMap.idToIndex(e),
							(int) (rand.nextDouble() * indexNet.getCapacityInt(eiMap.idToIndex(e))));
					break;
				default:
					throw new AssertionError();
			}
		}
		for (Integer e : g.edges())
			assertEquals(net.getFlowInt(e), indexNet.getFlowInt(eiMap.idToIndex(e)));
	}

	@SuppressWarnings("boxing")
	@Test
	public void testFlowAndCostSumDirected() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean intWeights : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = intGraph ? IntGraph.newDirected() : Graph.newDirected();
				g.addVertex(0);
				g.addVertex(1);
				g.addVertex(2);
				g.addEdge(0, 2, 0);
				g.addEdge(1, 2, 1);
				g.addEdge(2, 0, 2);

				FlowNetwork<Integer, Integer> net =
						intWeights ? FlowNetworkInt.createFromEdgeWeights(g) : FlowNetwork.createFromEdgeWeights(g);
				for (Integer e : g.edges())
					net.setCapacity(e, 1);

				WeightsDouble<Integer> costDouble = Weights.createExternalEdgesWeights(g, double.class);
				WeightsInt<Integer> costInt = Weights.createExternalEdgesWeights(g, int.class);
				for (Integer e : g.edges()) {
					costDouble.set(e, e.intValue() + 1);
					costInt.set(e, e.intValue() + 1);
				}

				assertEquals(0, net.getFlowSum(g, 0));
				assertEquals(0, net.getCostSum(g.edges(), costInt));
				assertEquals(0, net.getCostSum(g.edges(), costDouble));

				net.setFlow(0, 1);
				assertEquals(1, net.getFlowSum(g, 0));
				assertEquals(1, net.getCostSum(g.edges(), costInt));
				assertEquals(1, net.getCostSum(g.edges(), costDouble));

				net.setFlow(1, 1);
				assertEquals(1, net.getFlowSum(g, 0));
				assertEquals(2, net.getFlowSum(g, IntList.of(0, 1)));
				assertEquals(3, net.getCostSum(g.edges(), costInt));
				assertEquals(3, net.getCostSum(g.edges(), costDouble));

			}
		}
	}

	@SuppressWarnings("boxing")
	@Test
	public void testFlowAndCostSumUndirected() {
		for (boolean intGraph : BooleanList.of(false, true)) {
			for (boolean intWeights : BooleanList.of(false, true)) {
				Graph<Integer, Integer> g = intGraph ? IntGraph.newUndirected() : Graph.newUndirected();
				g.addVertex(0);
				g.addVertex(1);
				g.addVertex(2);
				g.addEdge(0, 2, 0);
				g.addEdge(1, 2, 1);
				g.addEdge(2, 0, 2);

				FlowNetwork<Integer, Integer> net =
						intWeights ? FlowNetworkInt.createFromEdgeWeights(g) : FlowNetwork.createFromEdgeWeights(g);
				for (Integer e : g.edges())
					net.setCapacity(e, 1);

				WeightsDouble<Integer> costDouble = Weights.createExternalEdgesWeights(g, double.class);
				WeightsInt<Integer> costInt = Weights.createExternalEdgesWeights(g, int.class);
				for (Integer e : g.edges()) {
					costDouble.set(e, e.intValue() + 1);
					costInt.set(e, e.intValue() + 1);
				}

				assertEquals(0, net.getFlowSum(g, 0));
				assertEquals(0, net.getCostSum(g.edges(), costInt));
				assertEquals(0, net.getCostSum(g.edges(), costDouble));

				net.setFlow(0, 1);
				assertEquals(1, net.getFlowSum(g, 0));
				assertEquals(1, net.getCostSum(g.edges(), costInt));
				assertEquals(1, net.getCostSum(g.edges(), costDouble));

				net.setFlow(1, 1);
				assertEquals(1, net.getFlowSum(g, 0));
				assertEquals(2, net.getFlowSum(g, IntList.of(0, 1)));
				assertEquals(3, net.getCostSum(g.edges(), costInt));
				assertEquals(3, net.getCostSum(g.edges(), costDouble));

			}
		}
	}

}
