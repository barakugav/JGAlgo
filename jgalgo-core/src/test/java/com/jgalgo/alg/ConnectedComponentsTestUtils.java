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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ConnectedComponentsTestUtils {

	static Pair<Integer, Int2IntMap> calcUndirectedConnectivity(IntGraph g) {
		int n = g.vertices().size();
		int ccNum = 0;
		Int2IntMap vertexToCC = new Int2IntOpenHashMap(n);
		vertexToCC.defaultReturnValue(-1);

		for (int start : g.vertices()) {
			if (vertexToCC.get(start) != -1)
				continue;
			int ccIdx = ccNum++;
			for (Bfs.IntIter it = Bfs.newInstance(g, start); it.hasNext();)
				vertexToCC.put(it.nextInt(), ccIdx);
		}
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
	}

	static Pair<Integer, Int2IntMap> calcDirectedConnectivity(IntGraph g) {
		int n = g.vertices().size();
		Int2ObjectMap<IntSet> reach = new Int2ObjectOpenHashMap<>();
		for (int start : g.vertices()) {
			IntSet vReach = new IntOpenHashSet();
			for (Bfs.IntIter it = Bfs.newInstance(g, start); it.hasNext();)
				vReach.add(it.nextInt());
			reach.put(start, vReach);
		}

		int ccNum = 0;
		Int2IntMap vertexToCC = new Int2IntOpenHashMap(n);
		vertexToCC.defaultReturnValue(-1);

		for (int u : g.vertices()) {
			if (vertexToCC.get(u) != -1)
				continue;
			int ccIdx = ccNum++;
			vertexToCC.put(u, ccIdx);
			for (int v : reach.get(u))
				if (reach.get(v).contains(u))
					vertexToCC.put(v, ccIdx);
		}
		return Pair.of(Integer.valueOf(ccNum), vertexToCC);
	}

	static void assertConnectivityResultsEqual(IntGraph g, Pair<Integer, Int2IntMap> r1, VertexPartition r2) {
		assertEquals(r1.first(), r2.numberOfBlocks());
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r2.numberOfBlocks());
		for (int u : g.vertices()) {
			int cc1 = r1.second().get(u);
			int cc2 = r2.vertexBlock(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	static void validateConnectivityResult(IntGraph g, VertexPartition res) {
		BitSet ccs = new BitSet();
		for (int v : g.vertices())
			ccs.set(res.vertexBlock(v));
		assertEquals(ccs.cardinality(), res.numberOfBlocks());
		for (int cc = 0; cc < res.numberOfBlocks(); cc++)
			assertTrue(ccs.get(cc));
	}

}
