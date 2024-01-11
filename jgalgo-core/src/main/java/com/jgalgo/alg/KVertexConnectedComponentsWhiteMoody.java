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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * White-Moody algorithm for finding k-vertex-connected components in an undirected graph.
 *
 * <p>
 * Based on 'Structural Cohesion and Embeddedness: A Hierarchical Concept of Social Groups' by White and Moody (2003).
 *
 * @author Barak Ugav
 */
class KVertexConnectedComponentsWhiteMoody implements KVertexConnectedComponentsAlgoBase {

	private final WeaklyConnectedComponentsAlgo unaryConnectedComponentsAlgo =
			WeaklyConnectedComponentsAlgo.newInstance();
	private final BiConnectedComponentsAlgo biConnectedComponentsAlgo = BiConnectedComponentsAlgo.newInstance();
	private final MinimumVertexCutGlobal globalConnectivityAlgo = MinimumVertexCutGlobal.newInstance();
	private final MinimumVertexCutAllGlobalKanevsky allGlobalConnectivityAlgo = new MinimumVertexCutAllGlobalKanevsky();

	@Override
	public KVertexConnectedComponentsAlgo.IResult findKVertexConnectedComponents(IndexGraph g, int k) {
		if (k < 0)
			throw new IllegalArgumentException("k must be non negative");
		List<List<IntSet>> hierarchy = findVertexConnectedComponentsHierarchy(g);
		List<IntSet> components = k >= hierarchy.size() ? List.of() : hierarchy.get(k);
		return new KVertexConnectedComponentsAlgos.IndexResult(g, components);
	}

	List<List<IntSet>> findVertexConnectedComponentsHierarchy(IndexGraph g) {
		Assertions.onlyUndirected(g);
		List<List<IntSet>> kComponents = new ArrayList<>();
		ObjIntConsumer<IntSet> addComponent = (c, k) -> {
			while (kComponents.size() <= k)
				kComponents.add(new ArrayList<>());
			kComponents.get(k).add(c);
		};

		/* Compute the 0-connected components (isolated vertices) and 1-connected (weakly connected) manually */
		IVertexPartition ccs1 = (IVertexPartition) unaryConnectedComponentsAlgo.findWeaklyConnectedComponents(g);
		for (int b : range(ccs1.numberOfBlocks())) {
			boolean isIsolated = ccs1.blockVertices(b).size() == 1;
			int compConnectivity = isIsolated ? 0 : 1;
			addComponent.accept(ccs1.blockVertices(b), compConnectivity);
		}

		/* Compute the 2-connected components (bi-comps) manually */
		BiConnectedComponentsAlgo.IResult ccs2 =
				(BiConnectedComponentsAlgo.IResult) biConnectedComponentsAlgo.findBiConnectedComponents(g);
		for (int b : range(ccs2.getNumberOfBiCcs())) {
			// TODO bi-comp algorithm should not return comps of size 2. should assert here
			/* avoid considering dyads as bicomponents */
			if (ccs2.getBiCcVertices(b).size() > 2)
				addComponent.accept(ccs2.getBiCcVertices(b), 2);
		}

		final int n = g.vertices().size();
		var isCliqueState = new Object() {
			final Bitmap cBitmap = new Bitmap(n);
			final IntList neighbors = new IntArrayList();
			final Bitmap neighborsBitmap = new Bitmap(n);
		};
		Predicate<IntSet> isClique = c -> {
			if (c.size() <= 1)
				return true;
			Bitmap cBitmap = isCliqueState.cBitmap;
			for (int v : c)
				cBitmap.set(v);
			boolean result = true;
			verticesLoop: for (int u : c) {
				IntList neighbors = isCliqueState.neighbors;
				Bitmap neighborsBitmap = isCliqueState.neighborsBitmap;
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (cBitmap.get(v) && !neighborsBitmap.get(v)) {
						neighbors.add(v);
						neighborsBitmap.set(v);
					}
				}
				for (int v : c) {
					if (u != v && !neighborsBitmap.get(v)) {
						result = false;
						break verticesLoop;
					}
				}
				neighborsBitmap.clearAllUnsafe(neighbors);
				neighbors.clear();
			}
			cBitmap.clearAllUnsafe(c);
			return result;
		};

		/*
		 * For a graph, we first compute its connectivity k, and than compute all the k-vertex-cuts in the graph,
		 * generating sub graphs with connectivity greater than k. Each such sub graph (or component) is than explored
		 * recursively, until tha graph is either trivial (single vertex) or a clique. We start the recursion from the
		 * bi-connected components of the graph, as we know how to compute them very efficiently.
		 */
		// TODO use tri-connected algorithm as a base, instead of bi-connected algo. Runs in linear time
		Stack<IntObjectPair<Iterator<IntSet>>> stack = new ObjectArrayList<>();
		for (int b : range(ccs2.getNumberOfBiCcs())) {
			IntSet biccVertices = ccs2.getBiCcVertices(b);
			if (biccVertices.size() <= 2)
				continue;
			/* Create the subgraph of the bi-connected components (avoid copying if the whole graph is bi-connected) */
			IntGraph biCc = biccVertices.size() < n ? g.subGraphCopy(biccVertices, null) : g;
			IndexGraph iBiCc = biCc.indexGraph();
			IndexIntIdMap biCcViMap = biCc.indexGraphVerticesMap();

			/* Compute the connectivity of the component */
			int topConnectivity = globalConnectivityAlgo.computeMinimumCut(iBiCc, null).size();
			if (topConnectivity > 2)
				addComponent.accept(biccVertices, topConnectivity);

			/* Add further sub-graphs of the components, generated from all the vertex-cuts of the component */
			Iterator<IntSet> cuts = allGlobalConnectivityAlgo.minimumCutsIter(iBiCc, topConnectivity);
			if (cuts.hasNext()) {
				List<IntSet> partitions = generateSubCompsFromCuts(iBiCc, cuts, topConnectivity);
				Iterator<IntSet> partitionsIter =
						IterTools.map(partitions.iterator(), s -> IndexIdMaps.indexToIdSet(s, biCcViMap));
				stack.push(IntObjectPair.of(topConnectivity, partitionsIter));
			}

			/* Explore recursively the sub graphs */
			while (!stack.isEmpty()) {
				IntObjectPair<Iterator<IntSet>> pair = stack.top();
				Iterator<IntSet> compsIter = pair.second();
				if (!compsIter.hasNext()) {
					stack.pop();
					continue;
				}
				final int parentConnectivity = pair.firstInt();
				IntSet compVertices = compsIter.next();

				/* Recursion end condition, if the component is a clique */
				if (isClique.test(compVertices)) {
					int thisK = compVertices.size() - 1;
					if (thisK > parentConnectivity && thisK > 2)
						addComponent.accept(compVertices, thisK);
					continue;
				}

				/* Create the component sub graph */
				IntGraph comp = biCc.subGraphCopy(compVertices, null);
				IndexGraph iComp = comp.indexGraph();
				IndexIntIdMap compViMap = comp.indexGraphVerticesMap();

				/* Compute the component connectivity */
				final int compConnectivity = globalConnectivityAlgo.computeMinimumCut(iComp, null).size();
				if (compConnectivity > parentConnectivity && compConnectivity > 2)
					addComponent.accept(compVertices, compConnectivity);

				/* Add further sub-graphs of the components, generated from all the vertex-cuts of the component */
				cuts = allGlobalConnectivityAlgo.minimumCutsIter(iComp, compConnectivity);
				if (cuts.hasNext()) {
					List<IntSet> partitions = generateSubCompsFromCuts(iComp, cuts, compConnectivity);
					Iterator<IntSet> partitionsIter =
							IterTools.map(partitions.iterator(), s -> IndexIdMaps.indexToIdSet(s, compViMap));
					stack.push(IntObjectPair.of(compConnectivity, partitionsIter));
				}
			}
		}

		/*
		 * Propagate components to all levels. We might need to copy a components of connectivity k to the list of
		 * components of connectivity k-1, if the vertices of the component are not included in any comp at connectivity
		 * k-1.
		 */
		Bitmap kBitmap = new Bitmap(n);
		for (int k = kComponents.size() - 1; k >= 0; k--) {
			if (k == kComponents.size() - 1) {
				kComponents.set(k, consolidateSets(g, kComponents.get(k), k));
			} else if (kComponents.get(k).isEmpty()) {
				kComponents.set(k, consolidateSets(g, kComponents.get(k + 1), k));
			} else {
				for (IntSet c : kComponents.get(k))
					for (int v : c)
						kBitmap.set(v);
				for (IntSet c : kComponents.get(k + 1)) {
					for (int v : c) {
						if (!kBitmap.get(v)) {
							kComponents.get(k).add(c);
							break;
						}
					}
				}
				kComponents.set(k, consolidateSets(g, kComponents.get(k), k));
				for (IntSet c : kComponents.get(k))
					for (int v : c)
						kBitmap.clear(v);
			}
		}

		return kComponents;
	}

	List<IntSet> generateSubCompsFromCuts(IndexGraph g, Iterator<IntSet> cuts, int k) {
		final int n = g.vertices().size();
		Bitmap cut = new Bitmap(n);
		for (IntSet c : IterTools.foreach(cuts))
			for (int v : c)
				cut.set(v);

		Bitmap nodes = cut.copy();
		nodes.not();
		for (int v : range(n))
			if (g.outEdges(v).size() <= k)
				nodes.clear(v);

		Bitmap visited = nodes.copy();
		visited.not();
		int[] comp = new int[n];
		List<IntSet> comps = new ArrayList<>();

		for (int r : range(n)) {
			if (visited.get(r))
				continue;
			int compSize = 0;
			int bfsNextIdx = 0;

			comp[compSize++] = r;
			visited.set(r);
			while (bfsNextIdx < compSize) {
				int u = comp[bfsNextIdx++];
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(r))
						continue;
					comp[compSize++] = v;
					visited.set(v);
				}
			}

			int neighborCutVerticesNum = 0;
			Bitmap neighborCutVerticesBitmap = cut.copy();
			neighborCutVerticesBitmap.not();
			for (int uIdx : range(compSize)) {
				int u = comp[uIdx];
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (!neighborCutVerticesBitmap.get(v)) {
						comp[compSize + neighborCutVerticesNum++] = v;
						neighborCutVerticesBitmap.set(v);
					}
				}
			}
			// neighborCutVerticesBitmap.clearAllUnsafe(
			// ImmutableIntArraySet.withNaiveContains(comp, compSize, compSize + neighborCutVerticesNum));
			compSize += neighborCutVerticesNum;

			comps.add(ImmutableIntArraySet.withNaiveContains(Arrays.copyOf(comp, compSize)));
		}

		return consolidateSets(g, comps, k + 1);
	}

	private static List<IntSet> consolidateSets(IndexGraph g, List<IntSet> sets, int k) {
		final int n = g.vertices().size();
		List<IntSet> unionSet = new ArrayList<>();

		int setsNum = sets.size();
		Bitmap visitedSets = new Bitmap(setsNum);

		int[] comp = new int[setsNum];
		for (int startSet : range(setsNum)) {
			if (visitedSets.get(startSet))
				continue;
			int compSize = 0;
			int bfsNextIdx = 0;

			visitedSets.set(startSet);
			comp[compSize++] = startSet;
			while (bfsNextIdx < compSize) {
				int uSetIdx = comp[bfsNextIdx++];
				Bitmap uSetBitmap = new Bitmap(n);
				for (int v : sets.get(uSetIdx))
					uSetBitmap.set(v);

				for (int vSetIdx : range(setsNum)) {
					if (visitedSets.get(vSetIdx))
						continue;
					IntList intersection = new IntArrayList();
					Bitmap intersectionBitmap = new Bitmap(n);
					for (int v : sets.get(vSetIdx)) {
						if (uSetBitmap.get(v)) {
							intersection.add(v);
							intersectionBitmap.set(v);
						}
					}
					if (intersection.size() >= k) {
						visitedSets.set(vSetIdx);
						comp[compSize++] = vSetIdx;
					}
					intersectionBitmap.clearAllUnsafe(intersection);
					intersection.clear();
				}
			}

			IntArrayList union = new IntArrayList();
			Bitmap unionBitmap = new Bitmap(n);
			for (int i : range(compSize)) {
				for (int v : sets.get(comp[i])) {
					if (!unionBitmap.get(v)) {
						union.add(v);
						unionBitmap.set(v);
					}
				}
			}
			unionSet.add(ImmutableIntArraySet.withNaiveContains(union.toIntArray()));
		}
		return unionSet;
	}

}
