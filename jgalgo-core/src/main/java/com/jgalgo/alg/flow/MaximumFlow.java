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

import java.util.Collection;
import com.jgalgo.alg.AlgorithmBuilderBase;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;

/**
 * Calculate the maximum flow in a flow network.
 *
 * <p>
 * A maximum flow is firstly a valid flow, namely for each vertex except the source and sink the sum of flow units going
 * along {@link Graph#inEdges(Object)} must be equal to the sum of flow units going along
 * {@link Graph#outEdges(Object)}. In addition, a maximum flow maximize the number of flow units originated at the
 * source and reaching the sink, which is equivalent to the sum of flows going out(in) of the source(sink) subtracted by
 * the sum of flows going in(out) to the source(sink).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #builder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * WeightsDouble<Integer> capacities = g.addEdgesWeights("capacity", double.class);
 * for (Integer e : g.edges())
 *  capacities.set(e, 1);
 *
 * String sourceVertex = ...;
 * String targetVertex = ...;
 * MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
 * Flow<String, Integer> flow = maxFlowAlg.computeMaximumFlow(g, capacities, sourceVertex, targetVertex);
 *
 * System.out.println("The maximum flow that can be pushed in the network is " + flow.getSupply(sourceVertex));
 * for (Integer e : g.edges()) {
 * 	double capacity = capacities.get(e);
 * 	double f = flow.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + f + "/" + capacity);
 * }
 * }</pre>
 *
 * @see    Flow
 * @see    <a href= "https://en.wikipedia.org/wiki/Maximum_flow_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumFlow {

	/**
	 * Calculate the maximum flow in a network between a source and a sink.
	 *
	 * <p>
	 * Some algorithm might run faster for integer capacities, and {@link WeightFunctionInt} can be passed as
	 * {@code capacity}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity} to avoid
	 * boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  capacity                 a capacity edge weight function
	 * @param  source                   a source vertex
	 * @param  sink                     a sink vertex
	 * @return                          the flows computed for each edge
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	<V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, V source, V sink);

	/**
	 * Calculate the maximum flow in a network between a set of sources and a set of sinks.
	 *
	 * <p>
	 * Some algorithm might run faster for integer capacities, and {@link WeightFunctionInt} can be passed as
	 * {@code capacity}.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code capacity} to avoid
	 * boxing/unboxing. If {@code g} is an {@link IntGraph}, the returned object is {@link IFlow}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  capacity                 a capacity edge weight function
	 * @param  sources                  a set of source vertices
	 * @param  sinks                    a set of sink vertices
	 * @return                          the flows computed for each edge
	 * @throws IllegalArgumentException if a vertex is both a source and a sink, or if a vertex appear twice in the
	 *                                      source or sinks sets
	 */
	<V, E> Flow<V, E> computeMaximumFlow(Graph<V, E> g, WeightFunction<E> capacity, Collection<V> sources,
			Collection<V> sinks);

	/**
	 * Create a new maximum flow algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MaximumFlow} object. The {@link MaximumFlow.Builder}
	 * might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MaximumFlow}
	 */
	static MaximumFlow newInstance() {
		return builder().build();
	}

	/**
	 * Create a new maximum flow algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MaximumFlow} objects
	 */
	static MaximumFlow.Builder builder() {
		return new MaximumFlow.Builder() {
			String impl;

			@Override
			public MaximumFlow build() {
				if (impl != null) {
					switch (impl) {
						case "edmonds-karp":
							return new MaximumFlowEdmondsKarp();
						case "dinic":
							return new MaximumFlowDinic();
						case "dinic-dynamic-trees":
							return new MaximumFlowDinicDynamicTrees();
						case "push-relabel-fifo":
							return MaximumFlowPushRelabel.newInstanceFifo();
						case "push-relabel-highest-first":
							return MaximumFlowPushRelabel.newInstanceHighestFirst();
						case "push-relabel-partial-augment":
							return MaximumFlowPushRelabel.newInstancePartialAugment();
						case "push-relabel-lowest-first":
							return MaximumFlowPushRelabel.newInstanceLowestFirst();
						case "push-relabel-move-to-front":
							return MaximumFlowPushRelabel.newInstanceMoveToFront();
						case "push-relabel-fifo-dynamic-trees":
							return new MaximumFlowPushRelabelDynamicTrees();
						default:
							throw new IllegalArgumentException("unknown 'impl' value: " + impl);
					}
				}
				return MaximumFlowPushRelabel.newInstanceHighestFirst();
			}

			@Override
			public void setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						MaximumFlow.Builder.super.setOption(key, value);
				}
			}
		};
	}

	/**
	 * A builder for {@link MaximumFlow} objects.
	 *
	 * @see    MaximumFlow#builder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for maximum flow computation.
		 *
		 * @return a new maximum flow algorithm
		 */
		MaximumFlow build();
	}

}
