package com.jgalgo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Johnson's algorithm for finding all cycles in a directed graph.
 * <p>
 * The algorithm runs in {@code O((n+m)(c+1))} time and {@code O(n + m)} space
 * where {@code c} is the number of simple cycles in the graph.
 * <p>
 * Based on the paper 'finding all the elementary circuits of a directed graph'
 * by Donald b. Johnson.
 *
 * @author Barak Ugav
 */
public class CyclesFinderJohnson implements CyclesFinder {

	@Override
	public List<Path> findAllCycles(Graph g) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException();
		return findAllCycles0((DiGraph) g);
	}

	private static List<Path> findAllCycles0(DiGraph g) {
		if (Graphs.containsParallelEdges(g))
			throw new IllegalArgumentException("graph with self loops is not supported");
		int n = g.vertices().size();
		Worker worker = new Worker(g);
		for (int startIdx = 0; startIdx < n; startIdx++) {
			var p = chooseSCCInSubGraph(g, startIdx);
			if (p == null)
				break;
			StronglyConnectedComponent scc = p.e1;
			startIdx = p.e2.intValue();
			worker.findAllCycles(startIdx, scc);
			worker.reset();
		}
		return worker.cycles;
	}

	private static class Worker {

		private final DiGraph g;
		private final BitSet isBlocked;
		private final IntSet[] blockingSet;
		private final IntStack unblockStack = new IntArrayList();
		private final IntStack path = new IntArrayList();
		private final List<Path> cycles = new ArrayList<>();

		Worker(DiGraph g) {
			this.g = g;
			int n = g.vertices().size();
			isBlocked = new BitSet(n);
			blockingSet = new IntSet[n];
			for (int u = 0; u < n; u++)
				blockingSet[u] = new IntOpenHashSet();
		}

		private void reset() {
			int n = g.vertices().size();
			isBlocked.clear();
			assert unblockStack.isEmpty();
			for (int u = 0; u < n; u++)
				blockingSet[u].clear();
			assert path.isEmpty();
		}

		private boolean findAllCycles(int startV, StronglyConnectedComponent scc) {
			boolean cycleFound = false;

			int u = path.isEmpty() ? startV : g.edgeTarget(path.topInt());
			assert scc.contains(u);
			isBlocked.set(u);

			for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
				int e = it.nextInt();
				int v = it.v();
				if (!scc.contains(v))
					continue;
				if (v == startV) {
					path.push(e);
					cycles.add(new Path(g, startV, startV, new IntArrayList((IntList) path)));
					path.popInt();
					cycleFound = true;
				} else if (!isBlocked.get(v)) {
					path.push(e);
					if (findAllCycles(startV, scc))
						cycleFound = true;
					path.popInt();
				}
			}
			if (cycleFound) {
				unblock(u);
			} else {
				for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
					it.nextInt();
					int v = it.v();
					if (!scc.contains(v))
						continue;
					blockingSet[v].add(u);
				}
			}
			return cycleFound;
		}

		private void unblock(int v) {
			isBlocked.clear(v);
			for (;;) {
				for (IntIterator it = blockingSet[v].iterator(); it.hasNext();) {
					int u = it.nextInt();
					if (isBlocked.get(u)) {
						isBlocked.clear(u);
						unblockStack.push(u);
					}
				}
				blockingSet[v].clear();

				if (unblockStack.isEmpty())
					break;
				v = unblockStack.popInt();
			}
		}
	}

	private static Pair<StronglyConnectedComponent, Integer> chooseSCCInSubGraph(DiGraph g, int startIdx) {
		int nFull = g.vertices().size();
		int subToFull = startIdx;
		int nSub = nFull - subToFull;
		DiGraph gSub = new GraphArrayDirected(nSub);
		for (int uSub = 0; uSub < nSub; uSub++) {
			int uFull = uSub + subToFull;
			for (EdgeIter it = g.edgesOut(uFull); it.hasNext();) {
				it.nextInt();
				int vSub = it.v() - subToFull;
				if (vSub >= 0)
					gSub.addEdge(uSub, vSub);
			}
		}

		Connectivity.Result connectivityResult = Connectivity.findStrongConnectivityComponents(gSub);
		int[] ccSize = new int[connectivityResult.getNumberOfCC()];
		for (int uSub = 0; uSub < nSub; uSub++)
			ccSize[connectivityResult.getVertexCc(uSub)]++;

		for (; startIdx < nFull; startIdx++) {
			int uSub = startIdx - subToFull;
			if (ccSize[connectivityResult.getVertexCc(uSub)] > 1 || hasSelfEdge(gSub, uSub))
				break;
		}
		if (startIdx >= nFull)
			return null;
		int ccIdx = connectivityResult.getVertexCc(startIdx - subToFull);
		return Pair.of(new StronglyConnectedComponent(subToFull, connectivityResult, ccIdx), Integer.valueOf(startIdx));
	}

	private static boolean hasSelfEdge(DiGraph g, int u) {
		for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
			it.nextInt();
			if (it.v() == u)
				return true;
		}
		return false;
	}

	private static class StronglyConnectedComponent {

		private final int subToFull;
		private final Connectivity.Result connectivityResult;
		private final int ccIdx;

		StronglyConnectedComponent(int subToFull, Connectivity.Result connectivityResult, int ccIdx) {
			this.subToFull = subToFull;
			this.connectivityResult = connectivityResult;
			this.ccIdx = ccIdx;
		}

		boolean contains(int v) {
			int vSub = v - subToFull;
			if (vSub < 0)
				return false;
			return ccIdx == connectivityResult.getVertexCc(vSub);
		}

	}

}
