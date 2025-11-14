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
package com.jgalgo.alg.connect;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.ds.DoubleIntReferenceableHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Stoer-Wagner Algorithm for global minimum cut.
 *
 * <p>
 * The algorithm runs in \(O(mn + n^2 \log n)\).
 *
 * <p>
 * Based on 'A Simple Min-Cut Algorithm' by Mechthild Stoer and Frank Wagner (1997).
 *
 * @author Barak Ugav
 */
public class MinimumEdgeCutGlobalStoerWagner extends MinimumEdgeCutGlobalAbstract {

	/**
	 * Create a new instance of the algorithm.
	 *
	 * <p>
	 * Please prefer using {@link MinimumEdgeCutGlobal#newInstance()} to get a default implementation for the
	 * {@link MinimumEdgeCutGlobal} interface.
	 */
	public MinimumEdgeCutGlobalStoerWagner() {}

	@Override
	protected IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		final int n = g.vertices().size();
		if (n < 2)
			throw new IllegalArgumentException("There is no valid cut in a graph with less than two vertices");

		w = WeightFunctions.localEdgeWeightFunction(g, w);
		Assertions.onlyPositiveEdgesWeights(g, w);
		w = IWeightFunction.replaceNullWeightFunc(w);

		ContractableGraph cg = new ContractableGraph(g);
		Bitmap cut = new Bitmap(n);

		DoubleIntReferenceableHeap heap = DoubleIntReferenceableHeap.newInstance();
		DoubleIntReferenceableHeap.Ref[] vRefs = new DoubleIntReferenceableHeap.Ref[n];

		Bitmap minimumCut = new Bitmap(n);
		double minimumCutWeight = Double.MAX_VALUE;

		while (cg.numberOfSuperVertices() > 1) {
			/* Start with an arbitrary cut with a single vertex */
			cut.clear();
			cut.set(0);

			/* Insert all (super) vertices to the heap */
			assert heap.isEmpty();
			assert range(cg.numberOfSuperVertices()).allMatch(U -> vRefs[U] == null);
			for (int U : range(1, cg.numberOfSuperVertices())) {
				double weightsSum = 0;
				for (ContractableGraph.EdgeIter eit = cg.outEdges(U); eit.hasNext();) {
					int e = eit.nextInt();
					assert eit.source() == U;
					int V = eit.target();
					if (cut.get(V))
						weightsSum += w.weight(e);
				}
				vRefs[U] = heap.insert(-weightsSum, U);
			}

			double cutOfThePhaseWeight = 0;
			int S = -1, T = -1;
			minimumCutPhase: for (int prev = -1;;) {
				DoubleIntReferenceableHeap.Ref min = heap.extractMin();
				int U = min.value();
				vRefs[U] = null;
				cut.set(U);

				if (heap.isEmpty()) {
					S = prev;
					T = U;
				}
				prev = U;

				if (heap.isNotEmpty()) {
					/* Not the last vertex */
					/* Decrease (actually increase) key of all neighbors (super) vertices not in the cut */
					for (ContractableGraph.EdgeIter eit = cg.outEdges(U); eit.hasNext();) {
						int e = eit.nextInt();
						assert eit.source() == U;
						int V = eit.target();
						if (!cut.get(V)) {
							DoubleIntReferenceableHeap.Ref vRef = vRefs[V];
							double weightsSum = -vRef.key();
							weightsSum += w.weight(e);
							heap.decreaseKey(vRef, -weightsSum);
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
			assert T >= 0;
			if (S < 0)
				S = 0;

			if (minimumCutWeight > cutOfThePhaseWeight) {
				minimumCutWeight = cutOfThePhaseWeight;
				minimumCut.clear();
				for (IntIterator it = cg.superVertexVertices(T); it.hasNext();)
					minimumCut.set(it.nextInt());
			}

			cg.contract(S, T);
		}

		return IVertexBiPartition.fromBitmap(g, minimumCut);
	}

}
