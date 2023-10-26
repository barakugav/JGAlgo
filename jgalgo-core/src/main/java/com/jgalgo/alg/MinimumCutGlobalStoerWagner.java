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

import java.util.BitSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.HeapReference;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Stoer-Wagner Algorithm for global minimum cut.
 * <p>
 * The algorithm runs in \(O(mn + n^2 \log n)\).
 * <p>
 * Based on 'A Simple Min-Cut Algorithm' by Mechthild Stoer and Frank Wagner (1997).
 *
 * @author Barak Ugav
 */
class MinimumCutGlobalStoerWagner extends MinimumCutGlobalAbstract {

	@Override
	VertexBiPartition computeMinimumCut(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyUndirected(g);
		final int n = g.vertices().size();
		if (n < 2)
			throw new IllegalArgumentException("There is no valid cut in a graph with less than two vertices");

		w = WeightFunctions.localEdgeWeightFunction(g, w);
		Assertions.Graphs.onlyPositiveEdgesWeights(g, w);

		ContractableGraph cg = new ContractableGraph(g);
		BitSet cut = new BitSet(n);

		HeapReferenceable<Double, Integer> heap =
				HeapReferenceable.newBuilder().keysTypePrimitive(double.class).valuesTypePrimitive(int.class).build();
		@SuppressWarnings("unchecked")
		HeapReference<Double, Integer>[] vRefs = new HeapReference[n];

		BitSet minimumCut = new BitSet(n);
		double minimumCutWeight = Double.MAX_VALUE;

		while (cg.numberOfSuperVertices() > 1) {
			/* Start with an arbitrary cut with a single vertex */
			cut.clear();
			cut.set(0);

			/* Insert all (super) vertices to the heap */
			assert heap.isEmpty();
			for (int U = 0; U < cg.numberOfSuperVertices(); U++)
				assert vRefs[U] == null;
			for (int U = 1; U < cg.numberOfSuperVertices(); U++) {
				double weightsSum = 0;
				for (ContractableGraph.EdgeIter eit = cg.outEdges(U); eit.hasNext();) {
					int e = eit.nextInt();
					assert eit.source() == U;
					int V = eit.target();
					if (cut.get(V))
						weightsSum += w.weight(e);
				}
				vRefs[U] = heap.insert(Double.valueOf(-weightsSum), Integer.valueOf(U));
			}

			double cutOfThePhaseWeight = 0;
			int S = -1, T = -1;
			minimumCutPhase: for (;;) {
				HeapReference<Double, Integer> min = heap.extractMin();
				int U = min.value().intValue();
				vRefs[U] = null;
				cut.set(U);

				if (heap.size() == 1)
					S = U;
				if (heap.size() == 0)
					T = U;

				if (heap.size() > 0) {
					/* Not the last vertex */
					/* Decrease (actually increase) key of all neighbors (super) vertices not in the cut */
					for (ContractableGraph.EdgeIter eit = cg.outEdges(U); eit.hasNext();) {
						int e = eit.nextInt();
						assert eit.source() == U;
						int V = eit.target();
						if (!cut.get(V)) {
							HeapReference<Double, Integer> vRef = vRefs[V];
							double weightsSum = -vRef.key().doubleValue();
							weightsSum += w.weight(e);
							heap.decreaseKey(vRef, Double.valueOf(-weightsSum));
						}
					}
				} else {
					/* Last vertex, no need to decrease keys */
					/* Find the cut-of-the-phase and its weight */
					for (ContractableGraph.EdgeIter eit = cg.outEdges(U); eit.hasNext();) {
						int e = eit.nextInt();
						assert eit.source() == U;
						assert cut.get(eit.target());
						cutOfThePhaseWeight += w.weight(e);
					}
					break minimumCutPhase;
				}
			}
			assert T != -1;
			if (S == -1)
				S = 0;

			if (minimumCutWeight > cutOfThePhaseWeight) {
				minimumCutWeight = cutOfThePhaseWeight;
				minimumCut.clear();
				for (IntIterator it = cg.superVertexVertices(T); it.hasNext();)
					minimumCut.set(it.nextInt());
			}

			cg.contract(S, T);
		}

		return new VertexBiPartitions.FromBitSet(g, minimumCut);
	}

}
