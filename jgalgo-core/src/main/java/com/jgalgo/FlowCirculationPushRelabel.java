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

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.data.LinkedListFixedSize;
import com.jgalgo.internal.util.Assertions;

class FlowCirculationPushRelabel extends FlowCirculations.AbstractImpl {

	@Override
	void computeCirculation(IndexGraph g, FlowNetwork net, WeightFunction supply) {
		new Worker(g, net, supply).computeCirculation();
	}

	private static class Worker {

		final IndexGraph g;
		final FlowNetwork net;

		final double[] capacity;
		final double[] flow;
		final double[] excess;

		final int[] label;
		final BitSet isActive;
		final LinkedListFixedSize.Doubly layersActive;
		final int[] layersHeadActive;
		int maxLayerActive;

		Worker(IndexGraph g, FlowNetwork net, WeightFunction supply) {
			Assertions.Graphs.onlyDirected(g);
			this.g = g;
			this.net = net;
			final int n = g.vertices().size();
			final int m = g.edges().size();

			flow = new double[m];
			capacity = new double[m];
			for (int e = 0; e < m; e++)
				capacity[e] = net.getCapacity(e);

			excess = new double[n];
			double excessSum = 0;
			for (int v = 0; v < n; v++)
				excessSum += excess[v] = supply.weight(v);
			if (excessSum != 0)
				throw new IllegalArgumentException();

			/* init greedily */
			final double lowerBound = 0;
			for (int e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (-excess[v] >= capacity[e]) {
					flow[e] = capacity[e];
					excess[u] -= capacity[e];
					excess[v] += capacity[e];
				} else if (-excess[v] < lowerBound) {
					flow[e] = lowerBound;
					excess[u] -= lowerBound;
					excess[v] += lowerBound;
				} else {
					double fc = -excess[v];
					flow[e] = fc;
					excess[u] -= fc;
					excess[v] = 0;
				}
			}

			/* init labels */
			label = new int[n];
			isActive = new BitSet(n);
			layersActive = new LinkedListFixedSize.Doubly(n);
			layersHeadActive = new int[n];
			maxLayerActive = 0;
			Arrays.fill(layersHeadActive, LinkedListFixedSize.None);
			for (int v = 0; v < n; v++) {
				if (excess[v] > 0) {
					label[v] = 1;
					activate(v);
				}
			}
		}

		private void activate(int v) {
			int layer = label[v];
			isActive.set(v);
			if (layersHeadActive[layer] != LinkedListFixedSize.None)
				layersActive.connect(v, layersHeadActive[layer]);
			layersHeadActive[layer] = v;
			if (maxLayerActive < layer)
				maxLayerActive = layer;
		}

		private void deactivate(int v) {
			int layer = label[v];
			isActive.clear(v);
			if (layersHeadActive[layer] == v)
				layersHeadActive[layer] = layersActive.next(v);
			layersActive.disconnect(v);
		}

		private int highestActive() {
			for (;; maxLayerActive--) {
				if (maxLayerActive < 0)
					return -1;
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[maxLayerActive];
			}
		}

		void computeCirculation() {
			final int n = g.vertices().size();
			final double lowerBound = 0;

			mainLoop: for (int act; (act = highestActive()) != -1;) {
				deactivate(act);
				double exc = excess[act];
				assert exc > 0;
				int actLevel = maxLayerActive;
				int minLayer = n;
				for (EdgeIter eit = g.outEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					double fc = capacity[e] - flow[e];
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
						// deactivate(act);
						excess[v] += exc;
						if (!isActive.get(v) && excess[v] > 0)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = capacity[e];
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (!isActive.get(v) && excess[v] > 0)
							activate(v);
					}
				}
				for (EdgeIter eit = g.inEdges(act).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.source();
					double fc = flow[e] - lowerBound;
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
						// deactivate(act);
						excess[v] += exc;
						if (!isActive.get(v) && excess[v] > 0)
							activate(v);
						continue mainLoop;

					} else {
						/* non-saturated push */
						flow[e] = lowerBound;
						exc -= fc; // no need to update excess[act], it will be updated later
						excess[v] += fc;
						if (!isActive.get(v) && excess[v] > 0)
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
//					if (layersHeadActive[actLevel] == -1)
//						throw new IllegalArgumentException("no valid circulation exists");
				}
			}
			for (int v = 0; v < n; v++)
				assert excess[v] == 0;
			for (int m = g.edges().size(), e = 0; e < m; e++)
				net.setFlow(e, flow[e]);
		}

	}

}
