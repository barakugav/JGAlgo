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

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The Edmonds-Karp algorithm for maximum flow.
 * <p>
 * The most known implementation that solve the maximum flow problem. It does so by finding augmenting paths from the
 * source to the sink in the residual network, and saturating at least one edge in each path. This is a specification
 * Ford–Fulkerson method, which chooses the shortest augmenting path in each iteration. It runs in \(O(m^2 n)\) time and
 * linear space.
 * <p>
 * Based on the paper 'Theoretical improvements in algorithmic efficiency for network flow problems' by Jack Edmonds and
 * Richard M Karp.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MaximumFlowEdmondsKarp extends MaximumFlowAbstract.WithoutResidualGraph {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowEdmondsKarp() {}

	@Override
	double computeMaximumFlow(IndexGraph g, IFlowNetwork net, int source, int sink) {
		if (net instanceof IFlowNetworkInt) {
			return new WorkerInt(g, (IFlowNetworkInt) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, source, sink).computeMaxFlow();
		}
	}

	private abstract class Worker extends MaximumFlowAbstract.WithoutResidualGraph.Worker {

		Worker(IndexGraph gOrig, IFlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		void computeMaxFlow0() {
			final int n = g.vertices().size();
			int[] backtrack = new int[n];
			Bitmap visited = new Bitmap(n);
			IntPriorityQueue queue = new FIFOQueueIntNoReduce();

			// perform BFS and find a path of non saturated edges from source to sink
			queue.enqueue(source);
			visited.set(source);
			if (directed) {
				bfs: for (;;) {
					if (queue.isEmpty())
						return; /* no path to sink, we are done */
					int u = queue.dequeueInt();
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (!isResidual(e))
							continue;
						int v = eit.targetInt();
						if (visited.get(v))
							continue;
						backtrack[v] = e;
						if (v == sink) {
							/* found an augmenting path, push flow on it */
							pushAlongPathDirected(backtrack);

							/* reset BFS */
							queue.clear();
							visited.clear();
							visited.set(source);
							queue.enqueue(source);
							continue bfs;
						}
						visited.set(v);
						queue.enqueue(v);
					}
					for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (!hasFlow(e))
							continue;
						int v = eit.sourceInt();
						if (visited.get(v))
							continue;
						backtrack[v] = e;
						if (v == sink) {
							/* found an augmenting path, push flow on it */
							pushAlongPathDirected(backtrack);

							/* reset BFS */
							queue.clear();
							visited.clear();
							visited.set(source);
							queue.enqueue(source);
							continue bfs;
						}
						visited.set(v);
						queue.enqueue(v);
					}
				}
			} else {
				bfs: for (;;) {
					if (queue.isEmpty())
						return; /* no path to sink, we are done */
					int u = queue.dequeueInt();
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v;
						if (u == g.edgeSource(e)) {
							if (!isResidual(e))
								continue;
							v = g.edgeTarget(e);
						} else {
							assert u == g.edgeTarget(e);
							if (!isTwinResidualUndirected(e))
								continue;
							v = g.edgeSource(e);
						}
						if (visited.get(v))
							continue;
						backtrack[v] = e;
						if (v == sink) {
							/* found an augmenting path, push flow on it */
							pushAlongPathUndirected(backtrack);

							/* reset BFS */
							queue.clear();
							visited.clear();
							visited.set(source);
							queue.enqueue(source);
							continue bfs;
						}
						visited.set(v);
						queue.enqueue(v);
					}
				}
			}
		}

		abstract void pushAlongPathDirected(int[] backtrack);

		abstract void pushAlongPathUndirected(int[] backtrack);

		abstract boolean hasFlow(int e);

		abstract boolean isSaturated(int e);

		abstract boolean isResidual(int e);

		abstract boolean isTwinResidualUndirected(int e);

	}

	private class WorkerDouble extends Worker {

		final double[] capacity;
		final double[] residualCapacity;

		private static final double EPS = 0.0001;

		WorkerDouble(IndexGraph gOrig, IFlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
			capacity = new double[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();
		}

		double computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(capacity, residualCapacity);
		}

		@Override
		void pushAlongPathDirected(int[] backtrack) {
			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				double ec;
				int nextP;
				if (g.edgeTarget(e) == p) {
					ec = getResidualCapacity(e);
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					ec = flow(e);
					nextP = g.edgeTarget(e);
				}
				assert ec >= EPS;
				f = Math.min(f, ec);
				p = nextP;
			}
			assert f >= EPS;

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int nextP;
				if (g.edgeTarget(e) == p) {
					residualCapacity[e] -= f;
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					residualCapacity[e] += f;
					nextP = g.edgeTarget(e);
				}
				p = nextP;
			}
		}

		@Override
		void pushAlongPathUndirected(int[] backtrack) {
			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				double ec;
				int nextP;
				if (g.edgeTarget(e) == p) {
					ec = getResidualCapacity(e);
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					ec = getTwinResidualCapacity(e);
					nextP = g.edgeTarget(e);
				}
				assert ec >= EPS;
				f = Math.min(f, ec);
				p = nextP;
			}
			assert f >= EPS;

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int nextP;
				if (g.edgeTarget(e) == p) {
					residualCapacity[e] -= f;
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					residualCapacity[e] += f;
					nextP = g.edgeTarget(e);
				}
				p = nextP;
			}
		}

		double flow(int e) {
			return capacity[e] - residualCapacity[e];
		}

		@Override
		boolean hasFlow(int e) {
			return flow(e) > EPS;
		}

		double getResidualCapacity(int e) {
			return residualCapacity[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > EPS;
		}

		@Override
		boolean isTwinResidualUndirected(int e) {
			return getTwinResidualCapacity(e) > EPS;
		}

		double getTwinResidualCapacity(int e) {
			assert !directed;
			return 2 * capacity[e] - residualCapacity[e];
		}
	}

	private class WorkerInt extends Worker {

		final int[] capacity;
		final int[] residualCapacity;

		WorkerInt(IndexGraph gOrig, IFlowNetworkInt net, int source, int sink) {
			super(gOrig, net, source, sink);
			capacity = new int[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();
		}

		int computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(capacity, residualCapacity);
		}

		@Override
		void pushAlongPathDirected(int[] backtrack) {
			// find out what is the maximum flow we can pass
			int f = Integer.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int ec, nextP;
				if (g.edgeTarget(e) == p) {
					ec = getResidualCapacity(e);
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					ec = flow(e);
					nextP = g.edgeTarget(e);
				}
				assert ec > 0;
				f = Math.min(f, ec);
				p = nextP;
			}
			assert f > 0;

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int nextP;
				if (g.edgeTarget(e) == p) {
					residualCapacity[e] -= f;
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					residualCapacity[e] += f;
					nextP = g.edgeTarget(e);
				}
				p = nextP;
			}
		}

		@Override
		void pushAlongPathUndirected(int[] backtrack) {
			// find out what is the maximum flow we can pass
			int f = Integer.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int ec, nextP;
				if (g.edgeTarget(e) == p) {
					ec = getResidualCapacity(e);
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					ec = getTwinResidualCapacity(e);
					nextP = g.edgeTarget(e);
				}
				assert ec > 0;
				f = Math.min(f, ec);
				p = nextP;
			}
			assert f > 0;

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				int nextP;
				if (g.edgeTarget(e) == p) {
					residualCapacity[e] -= f;
					nextP = g.edgeSource(e);
				} else {
					assert g.edgeSource(e) == p;
					residualCapacity[e] += f;
					nextP = g.edgeTarget(e);
				}
				p = nextP;
			}
		}

		int flow(int e) {
			return capacity[e] - residualCapacity[e];
		}

		@Override
		boolean hasFlow(int e) {
			return flow(e) > 0;
		}

		int getResidualCapacity(int e) {
			return residualCapacity[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= 0;
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		@Override
		boolean isTwinResidualUndirected(int e) {
			return getTwinResidualCapacity(e) > 0;
		}

		int getTwinResidualCapacity(int e) {
			assert !directed;
			return 2 * capacity[e] - residualCapacity[e];
		}
	}

}
