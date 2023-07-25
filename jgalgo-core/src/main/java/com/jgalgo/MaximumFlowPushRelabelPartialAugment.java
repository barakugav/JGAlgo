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

import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * The Partial Augment–Relabel Algorithm for the Maximum Flow Problem.
 * <p>
 * Based on 'The Partial Augment–Relabel Algorithm for the Maximum Flow Problem' by Andrew V. Goldberg.
 *
 * @author Barak Ugav
 */
class MaximumFlowPushRelabelPartialAugment extends MaximumFlowPushRelabelHighestFirst {

	MaximumFlowPushRelabelPartialAugment() {}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		return new WorkerDouble(gOrig, net, sources, sinks);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks) {
		return new WorkerInt(gOrig, net, sources, sinks);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelHighestFirst.WorkerDouble {

		private static final int MAX_AUGMENT_PATH_LENGTH = 4;
		private final IntArrayList path = new IntArrayList(MAX_AUGMENT_PATH_LENGTH);

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);
		}

		@Override
		void discharge(int searchSource) {
			assert hasExcess(searchSource);
			assert path.isEmpty();

			dfs: for (int u = searchSource;;) {
				int uLabel = label[u];
				for (EdgeIter it = edgeIters[u];;) {
					int e = it.peekNext();
					int v = g.edgeTarget(e);
					boolean isAdmissible = isResidual(e) && uLabel == label[v] + 1;
					if (isAdmissible) {
						path.add(e);
						if (path.size() == MAX_AUGMENT_PATH_LENGTH || v == sink) {
							if (v != sink && !hasExcess(v))
								activate(v);
							pushOnPath(searchSource);
							path.clear();
							if (!hasExcess(searchSource))
								return;
							u = searchSource;
						} else {
							u = v;
						}
						continue dfs;
					}
					it.nextInt();
					if (!it.hasNext()) {
						// Finished iterating over all vertex edges.
						// relabel and Reset iterator

						boolean isSearchSource = u == searchSource, hasExcess = true;
						if (!isSearchSource) {
							hasExcess = hasExcess(u);
							if (hasExcess) {
								removeFromLayerActive(u, uLabel);
							} else {
								removeFromLayerInactive(u, uLabel);
							}
						}

						relabel(u, uLabel + 1);
						if ((uLabel = label[u]) >= n) {
							/*
							 * empty layer gap heuristic was activated, all vertices on the path have greater or equal
							 * label as u, all are unreachable from sink
							 */
							path.clear();
							return;
						}

						if (!isSearchSource) {
							if (hasExcess) {
								addToLayerActive(u, uLabel);
							} else {
								addToLayerInactive(u, uLabel);
							}
						}

						it = edgeIters[u] = g.outEdges(u).iterator();
						assert it.hasNext();
						if (!isSearchSource) {
							assert !path.isEmpty();
							int lastEdge = path.popInt();
							assert u == g.edgeTarget(lastEdge);
							u = g.edgeSource(lastEdge);
						}
						continue dfs;
					}
				}
			}
		}

		private void pushOnPath(int pathSource) {
			assert !path.isEmpty();
			double f = excess[pathSource];
			for (int e : path)
				f = Math.min(f, getResidualCapacity(e));
			assert f > 0;
			for (int e : path) {
				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
				assert residualCapacity[e] >= -EPS;
				assert residualCapacity[t] >= -EPS;
			}
			int pathSink = g.edgeTarget(path.getInt(path.size() - 1));
			excess[pathSource] -= f;
			excess[pathSink] += f;
		}

	}

	private static class WorkerInt extends MaximumFlowPushRelabelHighestFirst.WorkerInt {

		private static final int MAX_AUGMENT_PATH_LENGTH = 4;
		private final IntArrayList path = new IntArrayList(MAX_AUGMENT_PATH_LENGTH);

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);
		}

		@Override
		void discharge(int searchSource) {
			assert hasExcess(searchSource);
			assert path.isEmpty();

			dfs: for (int u = searchSource;;) {
				int uLabel = label[u];
				for (EdgeIter it = edgeIters[u];;) {
					int e = it.peekNext();
					int v = g.edgeTarget(e);
					boolean isAdmissible = isResidual(e) && uLabel == label[v] + 1;
					if (isAdmissible) {
						path.add(e);
						if (path.size() == MAX_AUGMENT_PATH_LENGTH || v == sink) {
							if (v != sink && !hasExcess(v))
								activate(v);
							pushOnPath(searchSource);
							path.clear();
							if (!hasExcess(searchSource))
								return;
							u = searchSource;
						} else {
							u = v;
						}
						continue dfs;
					}
					it.nextInt();
					if (!it.hasNext()) {
						// Finished iterating over all vertex edges.
						// relabel and Reset iterator

						boolean isSearchSource = u == searchSource, hasExcess = true;
						if (!isSearchSource) {
							hasExcess = hasExcess(u);
							if (hasExcess) {
								removeFromLayerActive(u, uLabel);
							} else {
								removeFromLayerInactive(u, uLabel);
							}
						}

						relabel(u, uLabel + 1);
						if ((uLabel = label[u]) >= n) {
							/*
							 * empty layer gap heuristic was activated, all vertices on the path have greater or equal
							 * label as u, all are unreachable from sink
							 */
							path.clear();
							return;
						}

						if (!isSearchSource) {
							if (hasExcess) {
								addToLayerActive(u, uLabel);
							} else {
								addToLayerInactive(u, uLabel);
							}
						}

						it = edgeIters[u] = g.outEdges(u).iterator();
						assert it.hasNext();
						if (!isSearchSource) {
							assert !path.isEmpty();
							int lastEdge = path.popInt();
							assert u == g.edgeTarget(lastEdge);
							u = g.edgeSource(lastEdge);
						}
						continue dfs;
					}
				}
			}
		}

		private void pushOnPath(int pathSource) {
			assert !path.isEmpty();
			int f = excess[pathSource];
			for (int e : path)
				f = Math.min(f, getResidualCapacity(e));
			assert f > 0;
			for (int e : path) {
				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
				assert residualCapacity[e] >= 0;
				assert residualCapacity[t] >= 0;
			}
			int pathSink = g.edgeTarget(path.getInt(path.size() - 1));
			excess[pathSource] -= f;
			excess[pathSink] += f;
		}

	}

}
