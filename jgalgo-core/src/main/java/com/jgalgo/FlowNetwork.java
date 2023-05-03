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

package com.jgalgo;

/**
 * Flow on graph edges, with capacities and flows values.
 * <p>
 * A flow network on graph edges is defined as two functions: the capacity function \(C:E \rightarrow R\) and flow
 * function \( F:E \rightarrow R\). The capacity function define how many units of flow an edge can transfer from its
 * source to its target. The flow function is the number of units of flow that are currently transferred along the edge.
 * For each edge, the flow must be smaller or equal to its capacity.
 * <p>
 * Problems formulated using flow networks involve a source and a sink vertices. The source is a vertex from which the
 * flow is originated, and every flow going along its edges must reach the sink vertex using the edges of the graphs
 * while not violating the capacities of the network. For each vertex except the source and sink the sum of flow units
 * going along {@link Graph#edgesIn(int)} must be equal to the sum of flow units going along
 * {@link Graph#edgesOut(int)}.
 *
 * <pre> {@code
 * DiGraph g = ...;
 * FlowNetwork net = FlowNetwork.createAsEdgeWeight(g);
 * for (IntIterator edgeIter = g.edges().iterator(); edgeIter.hasNext();)
 *  f.setCapacity(edgeIter.nextInt(), 1);
 *
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * MaximumFlow maxFlowAlg = MaximumFlow.newBuilder().build();
 *
 * double totalFlow = maxFlowAlg.computeMaximumFlow(g, net, sourceVertex, targetVertex);
 * System.out.println("The maximum flow that can be pushed in the network is " + totalFlow);
 * for (IntIterator it = g.edges().iterator(); it.hasNext();) {
 * 	int e = it.nextInt();
 * 	double capacity = net.getCapacity(e);
 * 	double flow = net.getFlow(e);
 * 	System.out.println("flow on edge " + e + ": " + flow + "/" + capacity);
 * }
 * }</pre>
 *
 * @see    MaximumFlow
 * @author Barak Ugav
 */
public interface FlowNetwork {

    /**
     * Get the capacity of an edge.
     *
     * @param  edge                      an edge identifier in the graph
     * @return                           the capacity of the edge
     * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
     */
    double getCapacity(int edge);

    /**
     * Set the capacity of an edge.
     *
     * @param  edge                      an edge identifier in the graph
     * @param  capacity                  the new capacity of the edge
     * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
     */
    void setCapacity(int edge, double capacity);

    /**
     * Get the amount of flow units going along an edge.
     *
     * @param  edge                      an edge identifier in the graph
     * @return                           the amount of flow units going along an edge
     * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
     */
    double getFlow(int edge);

    /**
     * Set the amount of flow units going along an edge.
     *
     * @param  edge                      an edge identifier in the graph
     * @param  flow                      the new flow of the edge
     * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
     */
    void setFlow(int edge, double flow);

    /**
     * Create a flow network by adding edge weights using {@link Graph#addEdgesWeights}.
     * <p>
     * Unless {@link #setCapacity(int, double)} or {@link #setFlow(int, double)} are used, the capacity and flow of each
     * edge will be zero.
     *
     * @param  g a graph
     * @return   a flow network implemented as edge weights
     */
    static FlowNetwork createAsEdgeWeight(Graph g) {
        Weights.Double capacityWeights = g.addEdgesWeights(new Object(), double.class);
        Weights.Double flowWeights = g.addEdgesWeights(new Object(), double.class);
        return new FlowNetwork() {

            private static final double EPS = 0.0001;

            @Override
            public double getCapacity(int edge) {
                return capacityWeights.getDouble(edge);
            }

            @Override
            public void setCapacity(int edge, double capacity) {
                capacityWeights.set(edge, capacity);
            }

            @Override
            public double getFlow(int e) {
                return flowWeights.getDouble(e);
            }

            @Override
            public void setFlow(int edge, double flow) {
                double capacity = getCapacity(edge);
                if (flow > capacity + EPS)
                    throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
                flowWeights.set(edge, Math.min(flow, capacity));
            }
        };
    }

    /**
     * Flow on graph edges, with integer capacities and flows values.
     * <p>
     * Similar to the regular {@link FlowNetwork} interface, but with integer capacities and flows. Some algorithms that
     * work on flow networks are specifically for integers networks, or may performed faster if the capacities and flows
     * are integers.
     *
     * @author Barak Ugav
     */
    static interface Int extends FlowNetwork {

        /**
         * Get the integer capacity of an edge.
         *
         * @param  edge                      an edge identifier in the graph
         * @return                           the capacity of the edge
         * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
         */
        public int getCapacityInt(int edge);

        @Deprecated
        @Override
        default double getCapacity(int edge) {
            return getCapacityInt(edge);
        }

        /**
         * Set the integer capacity of an edge.
         *
         * @param  edge                      an edge identifier in the graph
         * @param  capacity                  the new capacity of the edge
         * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
         */
        public void setCapacity(int edge, int capacity);

        @Deprecated
        @Override
        default void setCapacity(int edge, double capacity) {
            setCapacity(edge, (int) capacity);
        }

        /**
         * Get the integer amount of flow units going along an edge.
         *
         * @param  edge                      an edge identifier in the graph
         * @return                           the amount of flow units going along an edge
         * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
         */
        public int getFlowInt(int edge);

        @Deprecated
        @Override
        default double getFlow(int edge) {
            return getFlowInt(edge);
        }

        /**
         * Set the integer amount of flow units going along an edge.
         *
         * @param  edge                      an edge identifier in the graph
         * @param  flow                      the new flow of the edge
         * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge identifier
         */
        public void setFlow(int edge, int flow);

        @Deprecated
        @Override
        default void setFlow(int edge, double flow) {
            setFlow(edge, (int) flow);
        }

        /**
         * Create an integer flow network by adding edge weights using {@link Graph#addEdgesWeights}.
         * <p>
         * Unless {@link #setCapacity(int, int)} or {@link #setFlow(int, int)} are used, the capacity and flow of each
         * edge will be zero.
         *
         * @param  g a graph
         * @return   a flow network implemented as edge weights
         */
        static FlowNetwork.Int createAsEdgeWeight(Graph g) {
            Weights.Int capacityWeights = g.addEdgesWeights(new Object(), int.class);
            Weights.Int flowWeights = g.addEdgesWeights(new Object(), int.class);
            return new FlowNetwork.Int() {

                @Override
                public int getCapacityInt(int edge) {
                    return capacityWeights.getInt(edge);
                }

                @Override
                public void setCapacity(int edge, int capacity) {
                    capacityWeights.set(edge, capacity);
                }

                @Override
                public int getFlowInt(int e) {
                    return flowWeights.getInt(e);
                }

                @Override
                public void setFlow(int edge, int flow) {
                    int capacity = getCapacityInt(edge);
                    if (flow > capacity)
                        throw new IllegalArgumentException("Illegal flow " + flow + " on edge " + edge);
                    flowWeights.set(edge, Math.min(flow, capacity));
                }
            };
        }

    }

}
