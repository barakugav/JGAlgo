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
 * source to its target. The flow function define the number of units of flow that are currently transferred along each
 * edge. The capacity of any edge must be non negative, and the edge's flow must be smaller or equal to its capacity.
 * <p>
 * Problems formulated using flow networks involve a source and a sink vertices. The source is a vertex from which the
 * flow is originated, and every flow going along its edges must reach the sink vertex using the edges of the graphs
 * while not violating the capacities of the network. For each vertex except the source and sink the sum of flow units
 * going along {@link Graph#edgesIn(int)} must be equal to the sum of flow units going along
 * {@link Graph#edgesOut(int)}.
 * <p>
 * A flow is most intuitively defined on directed graphs, as the flow on an edge is transferred from one vertex to
 * another in some direction, but we can define and solve flow problem on undirected graphs as well. Technically, the
 * flows values returned by {@link #getFlow(int)} can either be positive or negative for undirected edges, with values
 * absolutely smaller than the capacity of the edge. A positive flow \(+f\) value assigned to edge {@code e} means a
 * flow directed from {@code edgeSource(e)} to {@code edgeTarget(e)} with \(f\) units of flow. A negative flow \(-f\)
 * value assigned to edge {@code e} means a flow directed from {@code edgeTarget(e)} to {@code edgeSource(e)} (opposite
 * direction) with \(|-f|\) units of flow (see {@link #getFlow(int)}).
 *
 * <pre> {@code
 * Graph g = ...;
 * FlowNetwork net = FlowNetwork.createAsEdgeWeight(g);
 * for (int e : g.edges())
 *  f.setCapacity(e, 1);
 *
 * int sourceVertex = ...;
 * int targetVertex = ...;
 * MaximumFlow maxFlowAlg = MaximumFlow.newBuilder().build();
 *
 * double totalFlow = maxFlowAlg.computeMaximumFlow(g, net, sourceVertex, targetVertex);
 * System.out.println("The maximum flow that can be pushed in the network is " + totalFlow);
 * for (int e : g.edges()) {
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
     * @throws IllegalArgumentException  if {@code capacity} is negative
     */
    void setCapacity(int edge, double capacity);

    /**
     * Get the amount of flow units going along an edge.
     * <p>
     * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of \(f\)
     * units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
     * <p>
     * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
     * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
     * {@code e}, for \(-cap(e) \leq -f &lt; 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)} to
     * {@code edgeSource(e)} (opposite direction).
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
        Weights.Double capacityWeights = g.addEdgesWeights(new Utils.Obj("capacity"), double.class);
        Weights.Double flowWeights = g.addEdgesWeights(new Utils.Obj("flow"), double.class);
        return new FlowNetwork() {

            private static final double EPS = 0.0001;

            @Override
            public double getCapacity(int edge) {
                return capacityWeights.getDouble(edge);
            }

            @Override
            public void setCapacity(int edge, double capacity) {
                if (capacity < 0)
                    throw new IllegalArgumentException("capacity can't be negative");
                capacityWeights.set(edge, capacity);
            }

            @Override
            public double getFlow(int edge) {
                return flowWeights.getDouble(edge);
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
         * @throws IllegalArgumentException  if {@code capacity} is negative
         */
        public void setCapacity(int edge, int capacity);

        @Deprecated
        @Override
        default void setCapacity(int edge, double capacity) {
            setCapacity(edge, (int) capacity);
        }

        /**
         * Get the integer amount of flow units going along an edge.
         * <p>
         * If the graph is directed, a flow of \(f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow of
         * \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}.
         * <p>
         * If the graph is undirected, a flow of \(+f\) units on {@code e}, for \(0 \leq f \leq cap(e)\), means a flow
         * of \(f\) units of flow from {@code edgeSource(e)} to {@code edgeTarget(e)}, while a flow of \(-f\) units on
         * {@code e}, for \(-cap(e) \leq -f &lt; 0\), means a flow of \(|-f|\) units of flow from {@code edgeTarget(e)}
         * to {@code edgeSource(e)} (opposite direction).
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
            Weights.Int capacityWeights = g.addEdgesWeights(new Utils.Obj("capacity"), int.class);
            Weights.Int flowWeights = g.addEdgesWeights(new Utils.Obj("flow"), int.class);
            return new FlowNetwork.Int() {

                @Override
                public int getCapacityInt(int edge) {
                    return capacityWeights.getInt(edge);
                }

                @Override
                public void setCapacity(int edge, int capacity) {
                    if (capacity < 0)
                        throw new IllegalArgumentException("capacity can't be negative");
                    capacityWeights.set(edge, capacity);
                }

                @Override
                public int getFlowInt(int edge) {
                    return flowWeights.getInt(edge);
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
