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
package com.jgalgo.alg.cores;

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import java.util.function.IntConsumer;
import com.jgalgo.alg.EdgeDirection;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;

/**
 * Linear cores computing algorithm.
 *
 * <p>
 * The algorithm compute the core number of each vertex by computing the 0-core, than the 1-core, 2-core, ect. It does
 * so by removing all vertices with degree less than the current core number.
 *
 * <p>
 * The algorithm runs in linear time.
 *
 * <p>
 * Based on 'An O(m) Algorithm for Cores Decomposition of Networks' by Batagelj, V. and Zaversnik, M.
 *
 * @author Barak Ugav
 */
public class CoresAlgoImpl extends CoresAlgoAbstract {

	/**
	 * Create a new cores computing algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link CoresAlgo#newInstance()} to get a default implementation for the {@link CoresAlgo}
	 * interface.
	 */
	public CoresAlgoImpl() {}

	@Override
	protected CoresAlgo.IResult computeCores(IndexGraph g, EdgeDirection degreeType) {
		Objects.requireNonNull(degreeType);

		final int n = g.vertices().size();
		final boolean directed = g.isDirected();

		/* cache the degree of each vertex */
		int[] degree = new int[n];
		int maxDegree = 0;
		if (!directed || degreeType == EdgeDirection.Out) {
			for (int v : range(n)) {
				degree[v] = g.outEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else if (degreeType == EdgeDirection.In) {
			for (int v : range(n)) {
				degree[v] = g.inEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else {
			assert degreeType == EdgeDirection.All;
			for (int v : range(n)) {
				degree[v] = g.outEdges(v).size() + g.inEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		}

		/* arrange the vertices in sorted bins per degree */
		/* vertices[bin[d], bin[d+1]) are the vertices with degree d */
		/* vertices[pos[v]] == v */
		int[] bin = new int[maxDegree + 1];
		int[] vertices = new int[n];
		int[] pos = new int[n];
		for (int v : range(n))
			bin[degree[v]]++;
		for (int start = 0, d = 0; d <= maxDegree; d++) {
			int verticesNum = bin[d];
			bin[d] = start;
			start += verticesNum;
		}
		for (int v : range(n)) {
			pos[v] = bin[degree[v]];
			vertices[pos[v]] = v;
			bin[degree[v]]++;
		}
		for (int i = maxDegree; i > 0; i--)
			bin[i] = bin[i - 1];
		bin[0] = 0;

		IntConsumer decreaseDegree = v -> {
			int vDegree = degree[v];
			int vPos = pos[v];
			int vBinStart = bin[vDegree];
			if (vPos != vBinStart) {
				int wPos = vBinStart;
				int w = vertices[wPos];
				pos[v] = wPos;
				pos[w] = vPos;
				vertices[vPos] = w;
				vertices[wPos] = v;
			}
			bin[vDegree]++;
			degree[v]--;
		};

		/* iterate over the vertices in increase order of their degree */
		for (int p : range(n)) {
			int u = vertices[p];
			assert pos[u] == p;
			int uDegree = degree[u];

			/* we 'remove' u from the graph, actually just decreasing the degree of its neighbors */
			if (!directed || degreeType == EdgeDirection.In) {
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else if (degreeType == EdgeDirection.Out) {
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.sourceInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else {
				assert degreeType == EdgeDirection.All;
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.sourceInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			}
		}

		int[] core = degree;
		return new CoresAlgos.IndexResult(core);
	}

}
