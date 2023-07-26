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

							/* push along the admissible path */
							assert !path.isEmpty();
							pushOnPath(searchSource);

							/* If the 'source' does not have any excess, we are done */
							if (!hasExcess(searchSource)) {
								path.clear();
								return;
							}

							/* back up in the DFS tree until all edges in the path are admissible */
							for (int i = 0; i < path.size(); i++) {
								if (!isResidual(path.getInt(i))) {
									path.removeElements(i, path.size());
									break;
								}
							}
							assert path.intStream().allMatch(this::isResidual);
							u = path.isEmpty() ? searchSource : g.edgeTarget(path.getInt(path.size() - 1));
						} else {
							/* advance in the DFS */
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
			int u, v = pathSource;
			final byte Inactive = 0;
			final byte Active = 1;
			final byte SOURCE = 2;
			byte uMark = SOURCE;
			for (int e : path) {
				u = v;
				v = g.edgeTarget(e);
				double f = Math.min(excess[u], residualCapacity[e]);
				assert f >= 0;
				boolean vWasActive = hasExcess(v);

				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
				assert residualCapacity[e] >= -EPS;
				assert residualCapacity[t] >= -EPS;
				excess[u] -= f;
				excess[v] += f;
				assert excess[u] >= 0;
				assert excess[v] >= 0;

				if (uMark == Active) {
					if (!hasExcess(u))
						deactivate(u);
				} else if (uMark == Inactive) {
					if (hasExcess(u))
						activate(u);
				}
				uMark = vWasActive ? Active : Inactive;
			}
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

							/* push along the admissible path */
							assert !path.isEmpty();
							pushOnPath(searchSource);

							/* If the 'source' does not have any excess, we are done */
							if (!hasExcess(searchSource)) {
								path.clear();
								return;
							}

							/* back up in the DFS tree until all edges in the path are admissible */
							for (int i = 0; i < path.size(); i++) {
								if (!isResidual(path.getInt(i))) {
									path.removeElements(i, path.size());
									break;
								}
							}
							assert path.intStream().allMatch(this::isResidual);
							u = path.isEmpty() ? searchSource : g.edgeTarget(path.getInt(path.size() - 1));
						} else {
							/* advance in the DFS */
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
			int u, v = pathSource;
			final byte Inactive = 0;
			final byte Active = 1;
			final byte SOURCE = 2;
			byte uMark = SOURCE;
			for (int e : path) {
				u = v;
				v = g.edgeTarget(e);
				int f = Math.min(excess[u], residualCapacity[e]);
				assert f >= 0;
				boolean vWasActive = hasExcess(v);

				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
				assert residualCapacity[e] >= 0;
				assert residualCapacity[t] >= 0;
				excess[u] -= f;
				excess[v] += f;
				assert excess[u] >= 0;
				assert excess[v] >= 0;

				if (uMark == Active) {
					if (!hasExcess(u))
						deactivate(u);
				} else if (uMark == Inactive) {
					if (hasExcess(u))
						activate(u);
				}
				uMark = vWasActive ? Active : Inactive;
			}
		}

	}

}
