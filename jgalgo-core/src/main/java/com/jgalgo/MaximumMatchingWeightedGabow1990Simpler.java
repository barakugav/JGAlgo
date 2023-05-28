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

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Edmonds' Blossom algorithm for Maximum weighted matching with Gabow's implementation WITHOUT dynamic LCA data
 * structure.
 * <p>
 * This algorithm runs in \(O(m n \log n)\) time and uses linear space. The asymptotically running time is lower than
 * the regular {@link MaximumMatchingWeightedGabow1990} implementation, but it runs faster in practice. Instead of using
 * {@link SubtreeMergeFindMin} and {@link LCADynamic}, a simple heap is used to tracker 'blossom' steps.
 * <p>
 * Based on the original paper 'Paths, Trees, and Flowers' by Jack Edmonds (1965), later improved by 'An Efficient
 * Implementation of Edmonds Algorithm for Maximum Matching on Graphs' by Harold N. Gabow (1976), and using the
 * efficient dynamic LCA from 'Data Structures for Weighted Matching and Nearest Common Ancestors with Linking' by
 * Harold N. Gabow (1990) resulting in the final running time.
 *
 * @author Barak Ugav
 */
class MaximumMatchingWeightedGabow1990Simpler extends MaximumMatchingWeightedGabow1990Abstract {

	/**
	 * Create a new maximum weighted matching object.
	 */
	MaximumMatchingWeightedGabow1990Simpler() {}

	@Override
	Worker newWorker(Graph gOrig, EdgeWeightFunc w, HeapReferenceable.Builder<Object, Object> heapBuilder,
			DebugPrintsManager debugPrint) {
		return new Worker(gOrig, w, heapBuilder, debugPrint);
	}

	private static class Worker extends MaximumMatchingWeightedGabow1990Abstract.Worker {

		/* Heap storing all the blossom and augmenting events */
		final Heap<EdgeEvent> blossomEvents;

		Worker(Graph gOrig, EdgeWeightFunc w, HeapReferenceable.Builder<Object, Object> heapBuilder,
				DebugPrintsManager debugPrint) {
			super(gOrig, w, heapBuilder, debugPrint);
			blossomEvents = Heap.newBuilder().<EdgeEvent>elementsTypeObj()
					.build((e1, e2) -> Double.compare(e1.slack, e2.slack));
		}

		@Override
		double computeNextDelta3() {
			while (!blossomEvents.isEmpty()) {
				EdgeEvent blossomEvent = blossomEvents.findMin();
				int u = g.edgeSource(blossomEvent.e);
				int v = g.edgeTarget(blossomEvent.e);
				assert isEven(u) && isEven(v);
				Blossom U = evens.findBlossom(u), V = evens.findBlossom(v);
				if (U != V)
					return blossomEvent.slack / 2;
				blossomEvents.extractMin();
			}
			return Double.MAX_VALUE;
		}

		@Override
		EdgeEvent extractNextBlossomEvent() {
			return blossomEvents.extractMin();
		}

		@Override
		void searchEnd() {
			super.searchEnd();
			blossomEvents.clear();
		}

		@Override
		void growStep() {
			debug.print("growStep (root=",
					Integer.valueOf(evens.findBlossom(g.edgeSource(growEvents.findMin().key().e)).root), "): ",
					Integer.valueOf(growEvents.findMin().key().e));

			// Grow step
			assert delta == growEventsKey(growEvents.findMin().key());
			int e = growEvents.extractMin().key().e;
			int u = g.edgeSource(e), v = g.edgeTarget(e);

			Blossom U = evens.findBlossom(u), V = odds.findBlossom(v);
			assert !V.isEven && !isInTree(V);

			// Add odd vertex
			V.root = U.root;
			V.treeParentEdge = edgeVal.get(e).twin;
			V.isEven = false;
			V.delta1 = delta;
			assert V.growRef.key().e == e;
			V.growRef = null;
			if (!V.isSingleton())
				V.expandRef = expandEvents.insert(Double.valueOf(V.z0 / 2 + V.delta1), V);
			debug.print(" ", V);

			// Immediately add it's matched edge and vertex as even vertex
			int matchedEdge = matched[V.base];
			V = topBlossom(g.edgeTarget(matchedEdge));
			V.root = U.root;
			V.treeParentEdge = edgeVal.get(matchedEdge).twin;
			if (V.growRef != null) {
				growEvents.remove(V.growRef);
				V.growRef = null;
			}
			makeEven(V);
			debug.println(" ", V);
		}

		@Override
		void blossomStep(int e) {
			debug.println("blossomStep");
			int eu = g.edgeSource(e), ev = g.edgeTarget(e);
			assert isEven(eu) && isEven(ev);
			Blossom U = evens.findBlossom(eu), V = evens.findBlossom(ev);
			if (U == V)
				return; // Edge in same blossom, ignore

			// Create new blossom
			Blossom base = lcaInSearchTree(U, V);
			Blossom newb = new Blossom(base.base);
			newb.root = base.root;
			newb.treeParentEdge = base.treeParentEdge;
			newb.isEven = true;
			newb.child = base;
			newb.delta0 = delta;

			// Add all sub blossoms
			unionQueue.clear();
			scanQueue.clear();
			Blossom[] bs = new Blossom[] { U, V };
			for (Blossom b : bs) {
				boolean prevIsRight = b == U;
				Blossom prev = b == U ? V : U;
				int toPrevEdge = b == U ? e : edgeVal.get(e).twin;

				for (;;) {
					// handle even sub blossom
					assert b.isEven;
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					unionQueue.enqueue(b.base);

					if (b == base)
						break;
					prev = b;
					toPrevEdge = edgeVal.get(matched[b.base]).twin;
					assert matched[b.base] == b.treeParentEdge;
					b = topBlossom(g.edgeSource(toPrevEdge));

					// handle odd vertex
					assert !b.isEven;
					b.deltaOdd += delta - b.delta1;
					if (!b.isSingleton())
						b.z0 = dualVal(b);
					b.parent = newb;
					connectSubBlossoms(b, prev, toPrevEdge, !prevIsRight);
					for (IntIterator vit = b.vertices(); vit.hasNext();) {
						int v = vit.nextInt();
						blossoms[v].isEven = true;
						unionQueue.enqueue(v);
						scanQueue.enqueue(v);
					}
					b.delta0 = delta;
					if (!b.isSingleton()) {
						expandEvents.remove(b.expandRef);
						b.expandRef = null;
					}

					prev = b;
					toPrevEdge = edgeVal.get(b.treeParentEdge).twin;
					b = topBlossom(g.edgeSource(toPrevEdge));
				}
			}

			// Union all sub blossom in find0 data structure
			while (!unionQueue.isEmpty())
				evens.union(newb.base, unionQueue.dequeueInt());
			evens.setBlossom(newb.base, newb);

			// Scan new edges from all new even vertices
			while (!scanQueue.isEmpty()) {
				int u = scanQueue.dequeueInt();
				insertGrowEventsFromVertex(u);
				insertBlossomEventsFromVertex(u);
			}
		}

		@Override
		void makeEven(Blossom V) {
			V.isEven = true;
			V.delta0 = delta;
			int base = V.base;
			for (IntIterator vit = V.vertices(); vit.hasNext();) {
				int v = vit.nextInt();
				blossoms[v].isEven = true;
				evens.union(base, v);
			}
			evens.setBlossom(base, V);

			for (IntIterator vit = V.vertices(); vit.hasNext();) {
				int v = vit.nextInt();
				insertGrowEventsFromVertex(v);
				insertBlossomEventsFromVertex(v);
			}
		}

		@Override
		void addBlossomEvent(int e, double slackBar) {
			blossomEvents.add(new EdgeEvent(e, slackBar));
		}

	}

}
