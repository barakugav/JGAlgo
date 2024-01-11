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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.LinkedListFixedSize;
import com.jgalgo.internal.util.Assertions;

class FlowCirculationPushRelabel implements FlowCirculationBase {

	@Override
	public IFlow computeCirculation(IndexGraph g, IWeightFunction capacity, IWeightFunction supply) {
		if (WeightFunction.isInteger(capacity) && WeightFunction.isInteger(supply)) {
			return new WorkerInt(g, (IWeightFunctionInt) capacity, (IWeightFunctionInt) supply).computeCirculation();
		} else {
			return new WorkerDouble(g, capacity, supply).computeCirculation();
		}
	}

	private static class Worker {

		final IndexGraph g;
		final IWeightFunction capacityOrig;

		final int[] label;
		final LinkedListFixedSize.Doubly layersActive;
		final int[] layersHeadActive;
		int maxLayerActive;

		Worker(IndexGraph g, IWeightFunction capacityOrig, IWeightFunction supply) {
			Assertions.onlyDirected(g);
			this.g = g;
			this.capacityOrig = capacityOrig;
			final int n = g.vertices().size();

			label = new int[n];
			layersActive = new LinkedListFixedSize.Doubly(n);
			layersHeadActive = new int[n + 1];
		}

		void activate(int v) {
			int layer = label[v];
			if (layersHeadActive[layer] != LinkedListFixedSize.None)
				layersActive.connect(v, layersHeadActive[layer]);
			layersHeadActive[layer] = v;
			if (maxLayerActive < layer)
				maxLayerActive = layer;
		}

		void deactivate(int v) {
			int layer = label[v];
			if (layersHeadActive[layer] == v)
				layersHeadActive[layer] = layersActive.next(v);
			layersActive.disconnect(v);
		}

		int highestActive() {
			for (;; maxLayerActive--) {
				if (maxLayerActive < 0)
					return -1;
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[maxLayerActive];
			}
		}
	}

	private static class WorkerDouble extends Worker {

		final double[] capacity;
		final double[] flow;
		final double[] excess;
		private final double eps;

		WorkerDouble(IndexGraph g, IWeightFunction capacityOrig, IWeightFunction supply) {
			super(g, capacityOrig, supply);

			final int n = g.vertices().size();
			final int m = g.edges().size();

			flow = new double[m];
			capacity = new double[m];
			for (int e : range(m))
				capacity[e] = capacityOrig.weight(e);

			excess = new double[n];
			for (int v : range(n))
				excess[v] = supply.weight(v);

			double supplyEps = Arrays.stream(excess).filter(e -> e > 0).min().orElse(0) * 1e-8;
			double capacityEps = Arrays.stream(capacity).filter(c -> c > 0).min().orElse(0) * 1e-8;
			eps = Math.min(supplyEps, capacityEps);

			double excessSum = Arrays.stream(excess).sum();
			if (Math.abs(excessSum) > eps)
				throw new IllegalArgumentException("sum of supply is not zero");

			/* init greedily */
			for (int e : range(m)) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				if (-excess[v] >= capacity[e]) {
					flow[e] = capacity[e];
					excess[u] -= capacity[e];
					excess[v] += capacity[e];
				} else if (excess[v] <= 0) {
					double fc = -excess[v];
					flow[e] = fc;
					excess[u] -= fc;
					excess[v] = 0;
				}
			}

			/* init labels */
			Arrays.fill(layersHeadActive, LinkedListFixedSize.None);
			for (int v : range(n)) {
				if (excess[v] > eps) {
					label[v] = 1;
					activate(v);
				}
			}
		}

		IFlow computeCirculation() {
			final int n = g.vertices().size();
			final double lowerBound = 0;

			mainLoop: for (int act; (act = highestActive()) != -1;) {
				deactivate(act);
				double exc = excess[act];
				assert exc > eps;
				int actLevel = maxLayerActive;
				int minLayer = n;
				for (IEdgeIter eit = g.outEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					double fc = capacity[e] - flow[e];
					if (fc <= eps)
						continue;
					if (label[v] >= actLevel) {
						if (label[v] < minLayer)
							minLayer = label[v];
						continue;
					}
					if (fc >= exc) {
						/* saturated push */
						flow[e] += exc;
						excess[act] = 0;

						excess[v] += exc;
						if (excess[v] > eps && excess[v] <= exc + eps)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = capacity[e];
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (excess[v] > eps && excess[v] <= fc + eps)
							activate(v);
					}
				}
				for (IEdgeIter eit = g.inEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.sourceInt();
					double fc = flow[e] - lowerBound;
					if (fc <= eps)
						continue;
					if (label[v] >= actLevel) {
						if (label[v] < minLayer)
							minLayer = label[v];
						continue;
					}
					if (fc >= exc) {
						/* saturated push */
						flow[e] -= exc;
						excess[act] = 0;
						excess[v] += exc;
						if (excess[v] > eps && excess[v] <= exc + eps)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = lowerBound;
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (excess[v] > eps && excess[v] <= fc + eps)
							activate(v);
					}
				}

				excess[act] = exc;
				if (exc <= eps) {
					// deactivate(act);
				} else if (minLayer == n) {
					throw new IllegalArgumentException("no valid circulation exists");
				} else {
					label[act] = minLayer + 1;
					activate(act);
				}
			}

			assert g.vertices().intStream().allMatch(v -> excess[v] <= eps);
			for (int e : range(g.edges().size()))
				flow[e] = Math.max(0, Math.min(flow[e], capacityOrig.weight(e)));
			return new Flows.FlowImpl(g, flow);
		}
	}

	private static class WorkerInt extends Worker {

		final int[] capacity;
		final int[] flow;
		final int[] excess;

		WorkerInt(IndexGraph g, IWeightFunctionInt capacityOrig, IWeightFunctionInt supply) {
			super(g, capacityOrig, supply);

			final int n = g.vertices().size();
			final int m = g.edges().size();

			flow = new int[m];
			capacity = new int[m];
			for (int e : range(m))
				capacity[e] = capacityOrig.weightInt(e);

			excess = new int[n];
			int excessSum = 0;
			for (int v : range(n))
				excessSum += excess[v] = supply.weightInt(v);
			if (excessSum != 0)
				throw new IllegalArgumentException("sum of supply is not zero");

			/* init greedily */
			for (int e : range(m)) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				if (-excess[v] >= capacity[e]) {
					flow[e] = capacity[e];
					excess[u] -= capacity[e];
					excess[v] += capacity[e];
				} else if (excess[v] <= 0) {
					int fc = -excess[v];
					flow[e] = fc;
					excess[u] -= fc;
					excess[v] = 0;
				}
			}

			/* init labels */
			Arrays.fill(layersHeadActive, LinkedListFixedSize.None);
			for (int v : range(n)) {
				if (excess[v] > 0) {
					label[v] = 1;
					activate(v);
				}
			}
		}

		IFlow computeCirculation() {
			final int n = g.vertices().size();
			final int lowerBound = 0;

			mainLoop: for (int act; (act = highestActive()) != -1;) {
				deactivate(act);
				int exc = excess[act];
				assert exc > 0;
				int actLevel = maxLayerActive;
				int minLayer = n;
				for (IEdgeIter eit = g.outEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					int fc = capacity[e] - flow[e];
					if (fc <= 0)
						continue;
					if (label[v] >= actLevel) {
						if (label[v] < minLayer)
							minLayer = label[v];
						continue;
					}
					if (fc >= exc) {
						/* saturated push */
						flow[e] += exc;
						excess[act] = 0;

						excess[v] += exc;
						if (excess[v] > 0 && excess[v] <= exc)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = capacity[e];
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (excess[v] > 0 && excess[v] <= fc)
							activate(v);
					}
				}
				for (IEdgeIter eit = g.inEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.sourceInt();
					int fc = flow[e] - lowerBound;
					if (fc <= 0)
						continue;
					if (label[v] >= actLevel) {
						if (label[v] < minLayer)
							minLayer = label[v];
						continue;
					}
					if (fc >= exc) {
						/* saturated push */
						flow[e] -= exc;
						excess[act] = 0;
						excess[v] += exc;
						if (excess[v] > 0 && excess[v] <= exc)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = lowerBound;
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (excess[v] > 0 && excess[v] <= fc)
							activate(v);
					}
				}

				excess[act] = exc;
				if (exc <= 0) {
					// deactivate(act);
				} else if (minLayer == n) {
					throw new IllegalArgumentException("no valid circulation exists");
				} else {
					label[act] = minLayer + 1;
					activate(act);
				}
			}

			double[] flow0 = new double[flow.length];
			assert g.vertices().intStream().allMatch(v -> excess[v] == 0);
			for (int e : range(g.edges().size()))
				flow0[e] = flow[e];
			return new Flows.FlowImpl(g, flow0);
		}
	}

}
