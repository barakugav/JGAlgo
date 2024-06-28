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
package com.jgalgo.alg.hamilton;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import com.jgalgo.alg.path.IPath;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.BitmapSet;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Frank Rubin's algorithm for finding Hamiltonian paths in directed and undirected graphs.
 *
 * <p>
 * The algorithm perform a depth-first search on the graph, maintaining a path of edges that is extended by a single
 * edge in each step. The algorithm maintains a set of edges that are required to be in the path, and a set of edges
 * that are not allowed to be in the path.
 *
 * <p>
 * Based on 'A Search Procedure for Hamilton Paths and Circuits ' by Frank Rubin, 1974. Some improvements, such as
 * sorting edges extending the current path by their degree, are taken from 'Backtracking (the) Algorithms on the
 * Hamiltonian Cycle Problem' by J. SleegersD. V. D. Berg, 2021.
 *
 * @author Barak Ugav
 */
public class HamiltonianPathRubin extends HamiltonianPathAlgoAbstractBasedCycle {

	/**
	 * Create a new Hamiltonian path algorithm.
	 *
	 * <p>
	 * Please prefer using {@link HamiltonianPathAlgo#newInstance()} to get a default implementation for the
	 * {@link HamiltonianPathAlgo} interface.
	 */
	public HamiltonianPathRubin() {}

	@Override
	protected Iterator<IPath> hamiltonianCyclesIter(IndexGraph g) {
		final int n = g.vertices().size();
		if (n == 0)
			return Collections.emptyIterator();
		if (n == 1)
			return ObjectIterators.singleton(IPath.valueOf(g, 0, 0, Fastutil.list()));
		if (g.isDirected()) {
			return new IterDirected(g);
		} else {
			return new IterUndirected(g);
		}
	}

	private abstract static class IterBase implements Iterator<IPath> {

		final IndexGraph g;
		final int n;

		final Bitmap existingEdges;
		final int[] existingOutDegree;
		final int[] existingOutEdgeNext;
		final int[] existingOutEdgePrev;
		final int[] existingInEdgeNext;
		final int[] existingInEdgePrev;

		final IntArrayList path;
		int pathHead;
		final Bitmap onPath;
		int onPathCount;

		final BitmapSet verticesToExamine;
		final BitmapSet visited;
		final IntArrayList bfsStack;

		final int[] dfsEdges;
		final int[] dfsEdgesOffset;
		final int[] dfsEdgesNext;

		final int originVertex;

		private IPath next;

		IterBase(IndexGraph g) {
			this.g = g;
			n = g.vertices().size();
			assert n >= 2;
			final int m = g.edges().size();

			existingEdges = new Bitmap(m);
			existingOutDegree = new int[n];
			existingOutEdgeNext = new int[m];
			existingOutEdgePrev = new int[m];
			existingInEdgeNext = new int[m];
			existingInEdgePrev = new int[m];
			Arrays.fill(existingOutEdgeNext, -1);
			Arrays.fill(existingOutEdgePrev, -1);
			Arrays.fill(existingInEdgeNext, -1);
			Arrays.fill(existingInEdgePrev, -1);

			path = new IntArrayList(n);
			onPath = new Bitmap(n);

			int dfsEdgesArrSize;
			if (g.isDirected()) {
				dfsEdgesArrSize = m;
			} else if (!g.isAllowSelfEdges()) {
				dfsEdgesArrSize = 2 * m;
			} else {
				dfsEdgesArrSize = range(n).map(v -> g.outEdges(v).size()).sum();
			}
			dfsEdges = new int[dfsEdgesArrSize];
			dfsEdgesOffset = new int[n + 1];
			dfsEdgesNext = new int[n];

			verticesToExamine = new BitmapSet(n);
			visited = new BitmapSet(n);
			bfsStack = new IntArrayList(n);

			originVertex = range(n)
					.<IntIntPair>mapToObj(v -> IntIntPair.of(v, g.outEdges(v).size()))
					.min((p1, p2) -> Integer.compare(p1.secondInt(), p2.secondInt()))
					.get()
					.firstInt();
		}

		@Override
		public boolean hasNext() {
			if (next == null && pathHead >= 0)
				next = advance();
			return next != null;
		}

		@Override
		public IPath next() {
			if (!hasNext())
				throw new NoSuchElementException();
			IPath ret = next;
			next = null;
			return ret;
		}

		abstract IPath advance();

		void setupDfsHeadEdges() {
			/* move all deleted edges to one side of the array */
			for (int i = dfsEdgesOffset[pathHead], j = dfsEdgesOffset[pathHead + 1] - 1; i < j;) {
				while (i < j && existingEdges.get(dfsEdges[i]))
					i++;
				while (i < j && !existingEdges.get(dfsEdges[j]))
					j--;
				if (i < j) {
					int tmp = dfsEdges[i];
					dfsEdges[i] = dfsEdges[j];
					dfsEdges[j] = tmp;
					i++;
					j--;
				}
			}
			/* sort existing edges by their degree */
			int from = dfsEdgesOffset[pathHead], to = dfsEdgesOffset[pathHead] + existingOutDegree[pathHead];
			if (g.isDirected()) {
				IntArrays.quickSort(dfsEdges, from, to, (e1, e2) -> {
					int u1 = g.edgeTarget(e1), u2 = g.edgeTarget(e2);
					return Integer.compare(existingOutDegree[u1], existingOutDegree[u2]);
				});
			} else {
				IntArrays.quickSort(dfsEdges, from, to, (e1, e2) -> {
					int u1 = g.edgeEndpoint(e1, pathHead), u2 = g.edgeEndpoint(e2, pathHead);
					return Integer.compare(existingOutDegree[u1], existingOutDegree[u2]);
				});
			}
			dfsEdgesNext[path.size()] = from;
		}

		IntIterable pathHeadEdges() {
			return () -> new IntIterator() {
				int depth = path.size();
				final int to = dfsEdgesOffset[pathHead] + existingOutDegree[pathHead];

				@Override
				public boolean hasNext() {
					return dfsEdgesNext[depth] < to;
				}

				@Override
				public int nextInt() {
					return dfsEdges[dfsEdgesNext[depth]++];
				}
			};
		}

	}

	private static final class IterDirected extends IterBase {

		private final Bitmap requiredEdges;
		private final Bitmap hasRequiredOutEdge;
		private final Bitmap hasRequiredInEdge;
		private final int[] requiredEdgesHistory;
		private final int[] requiredEdgesHistoryOffset;

		private final int[] existingOutEdgeHead;
		private final int[] existingInEdgeHead;
		private final int[] deletedEdgesHistory;
		private final int[] deletedEdgesHistoryOffset;

		IterDirected(IndexGraph g) {
			super(g);
			assert g.isDirected();
			final int m = g.edges().size();

			existingEdges.setAll();
			requiredEdges = new Bitmap(m);
			hasRequiredOutEdge = new Bitmap(n);
			hasRequiredInEdge = new Bitmap(n);
			requiredEdgesHistory = new int[m];
			requiredEdgesHistoryOffset = new int[n];

			existingOutEdgeHead = new int[n];
			existingInEdgeHead = new int[n];
			Arrays.fill(existingOutEdgeHead, -1);
			Arrays.fill(existingInEdgeHead, -1);

			for (int v : range(n)) {
				IntIterator outEdges = g.outEdges(v).iterator();
				if (outEdges.hasNext()) {
					int head = outEdges.nextInt();
					existingOutEdgeHead[v] = head;
					for (int prev = head; outEdges.hasNext();) {
						int e = outEdges.nextInt();
						existingOutEdgeNext[prev] = e;
						existingOutEdgePrev[e] = prev;
						prev = e;
					}
					existingOutDegree[v] = g.outEdges(v).size();
				}
				IntIterator inEdges = g.inEdges(v).iterator();
				if (inEdges.hasNext()) {
					int head = inEdges.nextInt();
					existingInEdgeHead[v] = head;
					for (int prev = head; inEdges.hasNext();) {
						int e = inEdges.nextInt();
						existingInEdgeNext[prev] = e;
						existingInEdgePrev[e] = prev;
						prev = e;
					}
				}
			}

			deletedEdgesHistory = new int[m];
			deletedEdgesHistoryOffset = new int[n];

			int nextDfsEdgeOffset = 0;
			for (int v : range(n)) {
				dfsEdgesOffset[v] = nextDfsEdgeOffset;
				for (int e : g.outEdges(v))
					dfsEdges[nextDfsEdgeOffset++] = e;
			}
			assert nextDfsEdgeOffset == dfsEdges.length;
			dfsEdgesOffset[n] = nextDfsEdgeOffset;

			pathHead = originVertex;
			onPath.set(pathHead);
			onPathCount++;

			verticesToExamine.addAll(range(n));
			isAdmissiblePath0();
			verticesToExamine.clear();
			requiredEdgesHistoryOffset[0] = 0;
			deletedEdgesHistoryOffset[0] = 0;

			setupDfsHeadEdges();
		}

		@Override
		IPath advance() {
			dfs: for (;;) {
				final int u = pathHead;
				final int depth = path.size();
				if (depth == n - 1) {
					for (int e : pathHeadEdges()) {
						final int v = g.edgeTarget(e);
						if (v == originVertex) {
							int[] nextPath = new int[n];
							path.toArray(nextPath);
							nextPath[n - 1] = e;
							return IPath.valueOf(g, originVertex, originVertex, Fastutil.list(nextPath));
						}
					}
				} else {
					for (int e : pathHeadEdges()) {
						assert existingEdges.get(e);
						final int v = g.edgeTarget(e);
						if (!onPath.get(v)) {
							requiredEdgesHistoryOffset[depth] =
									path.isEmpty() ? 0 : requiredEdgesHistoryOffset[depth - 1];
							deletedEdgesHistoryOffset[depth] =
									path.isEmpty() ? 0 : deletedEdgesHistoryOffset[depth - 1];
							boolean isAdmissible = isAdmissiblePath(e, v);
							if (isAdmissible) {
								pathHead = v;
								onPath.set(pathHead);
								onPathCount++;
								path.add(e);
								setupDfsHeadEdges();
								continue dfs;
							}
							rollBackRequiredAndDeletedEdges();
						}
					}
				}

				onPath.clear(u);
				onPathCount--;
				if (path.isEmpty()) {
					pathHead = -1;
					return null;
				}
				int lastEdge = path.popInt();
				pathHead = g.edgeSource(lastEdge);
				rollBackRequiredAndDeletedEdges();
			}
		}

		private boolean isAdmissiblePath(int extendEdge, int newPathHead) {
			requireEdge(extendEdge);
			boolean isAdmissible = isAdmissiblePath0();
			if (!isAdmissible) {
				verticesToExamine.clear();
				return false;
			}

			/* F6. Fail if any set of required arcs forms a closed circuit, other than a Hamiltonian cycle */
			startLoop: for (int start : range(n)) {
				if (!visited.add(start))
					continue;
				for (int u = start; hasRequiredOutEdge.get(u);) {
					assert existingOutDegree[u] == 1;
					int requiredEdge = existingOutEdgeHead[u];
					int v = g.edgeTarget(requiredEdge);
					if (!visited.add(v)) {
						if (v == start) {
							boolean fullHamiltonianCycle = start == 0 && visited.size() == n;
							visited.clear();
							return fullHamiltonianCycle;
						}
						continue startLoop;
					}
					u = v;
				}
			}
			visited.clear();

			/*
			 * F7. Fail if for any node not in the partial path there is no directed path to that node from the last
			 * node in the partial path.
			 */
			visited.set(newPathHead);
			bfsStack.add(newPathHead);
			while (!bfsStack.isEmpty()) {
				int u = bfsStack.popInt();
				for (int e : existingOutEdges(u)) {
					int v = g.edgeTarget(e);
					if (onPath.get(v))
						continue;
					if (visited.add(v))
						bfsStack.add(v);
				}
			}
			int visitedCount = visited.size();
			visited.clear();
			if (visitedCount != n - onPathCount)
				return false; /* F7 */

			/*
			 * F8. Fail if for any node not in the partial path there is no directed path from that node to the initial
			 * node of the partial path.
			 */
			visited.set(originVertex);
			bfsStack.add(originVertex);
			while (!bfsStack.isEmpty()) {
				int v = bfsStack.popInt();
				for (int e : existingInEdges(v)) {
					int u = g.edgeSource(e);
					if (onPath.get(u))
						continue;
					if (visited.add(u))
						bfsStack.add(u);
				}
			}
			visitedCount = visited.size() - 1; /* we should not count originVertex */
			visited.clear();
			if (visitedCount != n - onPathCount)
				return false; /* F8 */

			return true;
		}

		private boolean isAdmissiblePath0() {
			/*
			 * TODO D3. Delete any arc which forms a closed circuit with required arcs, unless it completes the Hamilton
			 * circuit
			 */

			while (!verticesToExamine.isEmpty()) {
				int v = verticesToExamine.pop();

				/* F1. Fail if any vertex becomes isolated, that is, has no incident arc */
				/* F2. Fail if any vertex has only one incident arc */
				/* F3. Fail if any vertex has no directed arc entering (leaving) */
				/* F4. Fail if any vertex has two required directed arcs entering (leaving) */
				/* F5. Fail if any vertex has three required arcs incident */
				/* R1. If a vertex has only one directed arc entering (leaving), then that arc is required */
				IntIterator singleInIter = existingInEdges(v).iterator();
				if (!singleInIter.hasNext())
					return false; /* F1, F2, F3 */
				int singleIn = singleInIter.nextInt();
				boolean moreThanOneIn = singleInIter.hasNext();
				if (!moreThanOneIn) {
					// boolean admissible =
					requireEdge(singleIn); /* R1 */
					// if (!admissible)
					// return false; /* F4, F5 */
				}
				IntIterator singleOutIter = existingOutEdges(v).iterator();
				if (!singleOutIter.hasNext())
					return false; /* F1, F2, F3 */
				int singleOut = singleOutIter.nextInt();
				boolean moreThanOneOut = singleOutIter.hasNext();
				if (!moreThanOneOut) {
					// boolean admissible =
					requireEdge(singleOut); /* R1 */
					// if (!admissible)
					// return false; /* F4, F5 */
				}
			}
			return true;
		}

		private void requireEdge(int e) {
			if (!requiredEdges.set(e))
				return;

			requiredEdgesHistory[requiredEdgesHistoryOffset[path.size()]++] = e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);

			/* F4. Fail if any vertex has two required directed arcs entering (leaving) */
			/* F5. Fail if any vertex has three required arcs incident */
			/* This can't happen because we delete all other edges the first time we mark some edge as required */
			// if (!hasRequiredOutEdge.set(u))
			// return false; /* F4, F5 */
			// if (!hasRequiredInEdge.set(v))
			// return false; /* F4, F5 */
			boolean theOnlyRequiredEdge1 = hasRequiredOutEdge.set(u);
			boolean theOnlyRequiredEdge2 = hasRequiredInEdge.set(v);
			assert theOnlyRequiredEdge1 && theOnlyRequiredEdge2;

			/*
			 * D2. If a vertex has a required directed arc entering (leaving), then all undecided directed arcs entering
			 * (leaving) may be deleted
			 */
			/*
			 * We iterate over out/in edges and delete edges at the same time. In standard collections this will throw
			 * ConcurrentModificationException. Its OK here because the iteration over the linked list of the out/in
			 * edges always keep a pointer to the next edge, so we can delete the previous edge and continue the
			 * iteration. Don't try this at home.
			 */
			for (int e2 : existingOutEdges(u))
				if (!requiredEdges.get(e2))
					deleteEdge(e2); /* D2 */
			for (int e2 : existingInEdges(v))
				if (!requiredEdges.get(e2))
					deleteEdge(e2); /* D2 */

			verticesToExamine.add(u);
			verticesToExamine.add(v);
		}

		private void deleteEdge(int e) {
			boolean wasNonDeleted = existingEdges.clear(e);
			assert wasNonDeleted;

			deletedEdgesHistory[deletedEdgesHistoryOffset[path.size()]++] = e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int outPrev = existingOutEdgePrev[e], outNext = existingOutEdgeNext[e];
			int inPrev = existingInEdgePrev[e], inNext = existingInEdgeNext[e];
			if (outPrev >= 0) {
				existingOutEdgeNext[outPrev] = outNext;
			} else {
				existingOutEdgeHead[u] = outNext;
			}
			if (outNext >= 0)
				existingOutEdgePrev[outNext] = outPrev;
			existingOutDegree[u]--;

			if (inPrev >= 0) {
				existingInEdgeNext[inPrev] = inNext;
			} else {
				existingInEdgeHead[v] = inNext;
			}
			if (inNext >= 0)
				existingInEdgePrev[inNext] = inPrev;

			// existingOutEdgePrev[e] = existingOutEdgeNext[e] = existingInEdgePrev[e] = existingInEdgeNext[e] = -1;

			verticesToExamine.add(u);
			verticesToExamine.add(v);
		}

		private void rollBackRequiredAndDeletedEdges() {
			int reqFrom = requiredEdgesHistoryOffset[path.size()];
			int reqTo = path.isEmpty() ? 0 : requiredEdgesHistoryOffset[path.size() - 1];
			for (int i = reqFrom - 1; i >= reqTo; i--) {
				int e = requiredEdgesHistory[i];
				requiredEdges.clear(e);
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				boolean wasRequired1 = hasRequiredOutEdge.clear(u);
				boolean wasRequired2 = hasRequiredInEdge.clear(v);
				assert wasRequired1 && wasRequired2;
			}

			int delFrom = deletedEdgesHistoryOffset[path.size()];
			int delTo = path.isEmpty() ? 0 : deletedEdgesHistoryOffset[path.size() - 1];
			for (int i = delFrom - 1; i >= delTo; i--) {
				int e = deletedEdgesHistory[i];
				existingEdges.set(e);
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int outHead = existingOutEdgeHead[u], inHead = existingInEdgeHead[v];
				existingOutEdgeNext[e] = outHead;
				if (outHead >= 0)
					existingOutEdgePrev[outHead] = e;
				existingOutEdgeHead[u] = e;
				existingOutEdgePrev[e] = -1;
				existingOutDegree[u]++;
				existingInEdgeNext[e] = inHead;
				if (inHead >= 0)
					existingInEdgePrev[inHead] = e;
				existingInEdgeHead[v] = e;
				existingInEdgePrev[e] = -1;
			}
		}

		private IntIterable existingOutEdges(int source) {
			return edgesLinkedList(existingOutEdgeHead[source], existingOutEdgeNext);
		}

		private IntIterable existingInEdges(int target) {
			return edgesLinkedList(existingInEdgeHead[target], existingInEdgeNext);
		}

		private static IntIterable edgesLinkedList(int firstEdge, int[] nextEdge) {
			return () -> new IntIterator() {
				int e = firstEdge;

				@Override
				public boolean hasNext() {
					return e >= 0;
				}

				@Override
				public int nextInt() {
					int ret = e;
					e = nextEdge[e];
					return ret;
				}
			};
		}
	}

	private static final class IterUndirected extends IterBase {

		private final Bitmap requiredEdges;
		private final Bitmap hasRequiredEdge;
		private final int[] requiredEdgesHistory;
		private final int[] requiredEdgesHistoryOffset;

		private final int[] existingEdgeHead;
		private final int[] deletedEdgesHistory;
		private final int[] deletedEdgesHistoryOffset;

		private final IntSet usedOriginEdges;

		IterUndirected(IndexGraph g) {
			super(g);
			assert !g.isDirected();
			final int m = g.edges().size();

			requiredEdges = new Bitmap(m);
			hasRequiredEdge = new Bitmap(2 * n);
			requiredEdgesHistory = new int[m];
			requiredEdgesHistoryOffset = new int[n];

			existingEdgeHead = new int[n];
			Arrays.fill(existingEdgeHead, -1);
			for (int e : range(m))
				if (g.edgeSource(e) != g.edgeTarget(e))
					addExistingEdge(e);

			deletedEdgesHistory = new int[m];
			deletedEdgesHistoryOffset = new int[n];

			int nextDfsEdgeOffset = 0;
			for (int v : range(n)) {
				dfsEdgesOffset[v] = nextDfsEdgeOffset;
				for (int e : g.outEdges(v))
					dfsEdges[nextDfsEdgeOffset++] = e;
			}
			assert nextDfsEdgeOffset == dfsEdges.length;
			dfsEdgesOffset[n] = nextDfsEdgeOffset;

			pathHead = originVertex;
			onPath.set(pathHead);
			onPathCount++;

			verticesToExamine.addAll(range(n));
			isAdmissiblePath0();
			verticesToExamine.clear();
			requiredEdgesHistoryOffset[0] = 0;
			deletedEdgesHistoryOffset[0] = 0;

			usedOriginEdges = new IntOpenHashSet(g.outEdges(originVertex).size());

			setupDfsHeadEdges();
		}

		@Override
		IPath advance() {
			dfs: for (;;) {
				final int u = pathHead;
				final int depth = path.size();
				if (depth == n - 1) {
					for (int e : pathHeadEdges()) {
						final int v = g.edgeEndpoint(e, u);
						if (v == originVertex) {
							if (usedOriginEdges.contains(e))
								continue; /* removed edge to avoid undirected paths duplications */
							int[] nextPath = new int[n];
							path.toArray(nextPath);
							nextPath[n - 1] = e;
							return IPath.valueOf(g, originVertex, originVertex, Fastutil.list(nextPath));
						}
					}
				} else {
					for (int e : pathHeadEdges()) {
						assert existingEdges.get(e);
						final int v = g.edgeEndpoint(e, u);
						if (!onPath.get(v)) {
							requiredEdgesHistoryOffset[depth] =
									path.isEmpty() ? 0 : requiredEdgesHistoryOffset[depth - 1];
							deletedEdgesHistoryOffset[depth] =
									path.isEmpty() ? 0 : deletedEdgesHistoryOffset[depth - 1];
							boolean isAdmissible = isAdmissiblePath(e, v);
							if (isAdmissible) {
								pathHead = v;
								onPath.set(pathHead);
								onPathCount++;
								path.add(e);
								setupDfsHeadEdges();
								continue dfs;
							}
							rollBackRequiredAndDeletedEdges();
						}
					}
				}

				onPath.clear(u);
				onPathCount--;
				if (path.isEmpty()) {
					pathHead = -1;
					return null;
				}
				int lastEdge = path.popInt();
				pathHead = g.edgeEndpoint(lastEdge, pathHead);
				rollBackRequiredAndDeletedEdges();

				/*
				 * "Since the algorithm and deduction rules are oriented toward directed graphs, the Hamilton circuits
				 * in an undirected graph will be generated twice each, with the nodes named in opposite order. To
				 * prevent this redundancy, in step S3 of the search the successors of the origin node may be numbered.
				 * Then the undirected arcs to successor nodes which are numbered lower than the successor presently
				 * being considered should be deleted. Thus if 0 is the origin and 1, 2, ... , K are its successors,
				 * delete arcs (0, 1), (0, 2), -.., (0, i - 1) when considering successor i."
				 */
				if (pathHead == originVertex)
					usedOriginEdges.add(lastEdge);
			}
		}

		private boolean isAdmissiblePath(int extendEdge, int newPathHead) {
			boolean isAdmissible = requireEdge(extendEdge) && isAdmissiblePath0();
			if (!isAdmissible) {
				verticesToExamine.clear();
				return false;
			}

			/* F6. Fail if any set of required arcs forms a closed circuit, other than a Hamiltonian cycle */
			startLoop: for (int start : range(n)) {
				if (!visited.add(start))
					continue;
				int lastEdge = -1;
				for (int u = start; hasRequiredEdge.get(u * 2 + 1);) {
					assert existingOutDegree[u] == 2;
					IntIterator requiredEdges = existingEdges(u).iterator();
					int requiredEdge = requiredEdges.nextInt();
					if (requiredEdge == lastEdge)
						requiredEdge = requiredEdges.nextInt();
					int v = g.edgeEndpoint(requiredEdge, u);
					if (!visited.add(v)) {
						if (v == start) {
							boolean fullHamiltonianCycle = start == 0 && visited.size() == n;
							visited.clear();
							return fullHamiltonianCycle;
						}
						continue startLoop;
					}
					u = v;
					lastEdge = requiredEdge;
				}
			}
			visited.clear();

			/*
			 * F7. Fail if for any node not in the partial path there is no directed path to that node from the last
			 * node in the partial path.
			 */
			visited.set(newPathHead);
			bfsStack.add(newPathHead);
			while (!bfsStack.isEmpty()) {
				int v = bfsStack.popInt();
				for (int e : existingEdges(v)) {
					int w = g.edgeEndpoint(e, v);
					if (onPath.get(w))
						continue;
					if (visited.add(w))
						bfsStack.add(w);
				}
			}
			int visitedCount = visited.size();
			visited.clear();
			if (visitedCount != n - onPathCount)
				return false; /* F7 */

			/*
			 * F8. Fail if for any node not in the partial path there is no directed path from that node to the initial
			 * node of the partial path.
			 */
			boolean canConnectToOrigin = IterTools
					.stream(existingEdges(originVertex))
					.map(e -> g.edgeEndpoint(e, originVertex))
					.anyMatch(v -> !onPath.get(v));
			if (!canConnectToOrigin)
				return false; /* F8 */

			return true;
		}

		private boolean isAdmissiblePath0() {
			/*
			 * TODO D3. Delete any arc which forms a closed circuit with required arcs, unless it completes the Hamilton
			 * circuit
			 */

			while (!verticesToExamine.isEmpty()) {
				int v = verticesToExamine.pop();

				/* F1. Fail if any vertex becomes isolated, that is, has no incident arc */
				/* F2. Fail if any vertex has only one incident arc */
				/* F3. Fail if any vertex has no directed arc entering (leaving) */
				/* F4. Fail if any vertex has two required directed arcs entering (leaving) */
				/* F5. Fail if any vertex has three required arcs incident */
				/* R2. If a vertex has only two arcs incident, then both arcs are required */
				IntIterator edgeIter = existingEdges(v).iterator();
				if (!edgeIter.hasNext())
					return false; /* F1, F3 */
				int edge1 = edgeIter.nextInt();
				if (!edgeIter.hasNext())
					return false; /* F2 */
				int edge2 = edgeIter.nextInt();
				boolean moreThanOneIn = edgeIter.hasNext();
				if (!moreThanOneIn) {
					boolean admissible1 = requireEdge(edge1); /* R2 */
					if (!admissible1)
						return false; /* F4, F5 */

					boolean admissible2 = requireEdge(edge2); /* R2 */
					if (!admissible2)
						return false; /* F4, F5 */
				}
			}
			return true;
		}

		private boolean requireEdge(int e) {
			if (requiredEdges.get(e))
				return true;
			int u = g.edgeSource(e), v = g.edgeTarget(e);

			/* F4. Fail if any vertex has two required directed arcs entering (leaving) */
			/* F5. Fail if any vertex has three required arcs incident */
			if (hasRequiredEdge.get(2 * u + 1) || hasRequiredEdge.get(2 * v + 1))
				return false; /* F4, F5 */

			requiredEdges.set(e);
			requiredEdgesHistory[requiredEdgesHistoryOffset[path.size()]++] = e;

			/* D1. If a vertex has two required arcs incident, then all undecided arcs incident may be deleted */
			/*
			 * We iterate over edges and delete edges at the same time. In standard collections this will throw
			 * ConcurrentModificationException. Its OK here because the iteration over the linked list of the edges
			 * always keep a pointer to the next edge, so we can delete the previous edge and continue the iteration.
			 * Don't try this at home.
			 */
			if (!hasRequiredEdge.set(2 * u + 0)) {
				hasRequiredEdge.set(2 * u + 1);
				for (int e2 : existingEdges(u))
					if (!requiredEdges.get(e2))
						deleteEdge(e2); /* D1 */
			}
			if (!hasRequiredEdge.set(2 * v + 0)) {
				hasRequiredEdge.set(2 * v + 1);
				for (int e2 : existingEdges(v))
					if (!requiredEdges.get(e2))
						deleteEdge(e2); /* D1 */
			}

			verticesToExamine.add(u);
			verticesToExamine.add(v);
			return true;
		}

		private void deleteEdge(int e) {
			boolean wasNonDeleted = existingEdges.clear(e);
			assert wasNonDeleted;

			deletedEdgesHistory[deletedEdgesHistoryOffset[path.size()]++] = e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int outPrev = existingOutEdgePrev[e], outNext = existingOutEdgeNext[e];
			int inPrev = existingInEdgePrev[e], inNext = existingInEdgeNext[e];
			if (outPrev >= 0) {
				setExistingEdgeNext(outPrev, u, outNext);
			} else {
				existingEdgeHead[u] = outNext;
			}
			if (outNext >= 0)
				setExistingEdgePrev(outNext, u, outPrev);
			existingOutDegree[u]--;

			if (inPrev >= 0) {
				setExistingEdgeNext(inPrev, v, inNext);
			} else {
				existingEdgeHead[v] = inNext;
			}
			if (inNext >= 0)
				setExistingEdgePrev(inNext, v, inPrev);
			existingOutDegree[v]--;

			// existingOutEdgePrev[e] = existingOutEdgeNext[e] = existingInEdgePrev[e] = existingInEdgeNext[e] = -1;

			verticesToExamine.add(u);
			verticesToExamine.add(v);
		}

		private void rollBackRequiredAndDeletedEdges() {
			int reqFrom = requiredEdgesHistoryOffset[path.size()];
			int reqTo = path.isEmpty() ? 0 : requiredEdgesHistoryOffset[path.size() - 1];
			for (int i = reqFrom - 1; i >= reqTo; i--) {
				int e = requiredEdgesHistory[i];
				requiredEdges.clear(e);
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!hasRequiredEdge.clear(2 * u + 1)) {
					boolean wasRequired = hasRequiredEdge.clear(2 * u + 0);
					assert wasRequired;
				}
				if (!hasRequiredEdge.clear(2 * v + 1)) {
					boolean wasRequired = hasRequiredEdge.clear(2 * v + 0);
					assert wasRequired;
				}
			}

			int delFrom = deletedEdgesHistoryOffset[path.size()];
			int delTo = path.isEmpty() ? 0 : deletedEdgesHistoryOffset[path.size() - 1];
			for (int i = delFrom - 1; i >= delTo; i--)
				addExistingEdge(deletedEdgesHistory[i]);
		}

		private void addExistingEdge(int e) {
			existingEdges.set(e);

			int u = g.edgeSource(e), v = g.edgeTarget(e);

			int outHead = existingEdgeHead[u];
			existingOutEdgeNext[e] = outHead;
			if (outHead >= 0)
				setExistingEdgePrev(outHead, u, e);
			existingEdgeHead[u] = e;
			existingOutEdgePrev[e] = -1;
			existingOutDegree[u]++;

			int inHead = existingEdgeHead[v];
			existingInEdgeNext[e] = inHead;
			if (inHead >= 0)
				setExistingEdgePrev(inHead, v, e);
			existingEdgeHead[v] = e;
			existingInEdgePrev[e] = -1;
			existingOutDegree[v]++;
		}

		private void setExistingEdgeNext(int e, int endpoint, int next) {
			if (endpoint == g.edgeSource(e)) {
				existingOutEdgeNext[e] = next;
			} else {
				assert endpoint == g.edgeTarget(e);
				existingInEdgeNext[e] = next;
			}
		}

		private void setExistingEdgePrev(int e, int endpoint, int prev) {
			if (endpoint == g.edgeSource(e)) {
				existingOutEdgePrev[e] = prev;
			} else {
				assert endpoint == g.edgeTarget(e);
				existingInEdgePrev[e] = prev;
			}
		}

		private IntIterable existingEdges(int source) {
			return () -> new IntIterator() {
				int e = existingEdgeHead[source];

				@Override
				public boolean hasNext() {
					return e >= 0;
				}

				@Override
				public int nextInt() {
					int ret = e;
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					if (source == u) {
						e = existingOutEdgeNext[e];
					} else {
						assert source == v;
						e = existingInEdgeNext[e];
					}
					return ret;
				}
			};
		}
	}

}
