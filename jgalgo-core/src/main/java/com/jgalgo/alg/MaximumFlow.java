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

import java.util.Collection;
import com.jgalgo.graph.Graph;

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
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * <pre> {@code
 * Graph<String, Integer> g = ...;
 * FlowNetwork<String, Integer> net = FlowNetwork.createAsEdgeWeight(g);
 * for (Integer e : g.edges())
 *  f.setCapacity(e, 1);
 *
 * String sourceVertex = ...;
 * String targetVertex = ...;
 * MaxFlow maxFlowAlg = MaximumFlow.newInstance();
 *
 * double totalFlow = maxFlowAlg.computeMaximumFlow(g, net, sourceVertex, targetVertex);
 * System.out.println("The maximum flow that can be pushed in the network is " + totalFlow);
 * for (Integer e : g.edges()) {
 * 	double capacity = net.getCapacity(e);
 * 	double flow = net.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + flow + "/" + capacity);
 * }
 * }</pre>
 *
 * @see    FlowNetwork
 * @see    <a href= "https://en.wikipedia.org/wiki/Maximum_flow_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface MaximumFlow {

	/**
	 * Calculate the maximum flow in a network between a source and a sink.
	 *
	 * <p>
	 * The function will set the edges flow by {@link FlowNetwork#setFlow(Object, double)}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  net                      network flow
	 * @param  source                   a source vertex
	 * @param  sink                     a sink vertex
	 * @return                          the maximum flow in the network from the source to the sink
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	<V, E> double computeMaximumFlow(Graph<V, E> g, FlowNetwork<V, E> net, V source, V sink);

	/**
	 * Calculate the maximum flow in a network between a set of sources and a set of sinks.
	 *
	 * <p>
	 * The function will set the edges flow by {@link FlowNetwork#setFlow(Object, double)}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  net                      network flow
	 * @param  sources                  a set of source vertices
	 * @param  sinks                    a set of sink vertices
	 * @return                          the maximum flow in the network from the sources to the sinks
	 * @throws IllegalArgumentException if a vertex is both a source and a sink, or if a vertex appear twice in the
	 *                                      source or sinks sets
	 */
	<V, E> double computeMaximumFlow(Graph<V, E> g, FlowNetwork<V, E> net, Collection<V> sources, Collection<V> sinks);

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
		return newBuilder().build();
	}

	/**
	 * Create a new maximum flow algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MaximumFlow} objects
	 */
	static MaximumFlow.Builder newBuilder() {
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
			public MaximumFlow.Builder setOption(String key, Object value) {
				switch (key) {
					case "impl":
						impl = (String) value;
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}
		};
	}

	/**
	 * A builder for {@link MaximumFlow} objects.
	 *
	 * @see    MaximumFlow#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for maximum flow computation.
		 *
		 * @return a new maximum flow algorithm
		 */
		MaximumFlow build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default MaximumFlow.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
