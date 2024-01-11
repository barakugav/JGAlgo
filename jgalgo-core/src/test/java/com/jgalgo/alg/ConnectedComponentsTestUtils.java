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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import java.util.Set;
import com.jgalgo.graph.Graph;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

class ConnectedComponentsTestUtils {

	static <V, E> IntObjectPair<Object2IntMap<V>> calcUndirectedConnectivity(Graph<V, E> g) {
		int n = g.vertices().size();
		int ccNum = 0;
		Object2IntMap<V> vertexToCC = new Object2IntOpenHashMap<>(n);
		vertexToCC.defaultReturnValue(-1);

		for (V start : g.vertices()) {
			if (vertexToCC.getInt(start) != -1)
				continue;
			int ccIdx = ccNum++;
			for (BfsIter<V, E> it = BfsIter.newInstance(g, start); it.hasNext();)
				vertexToCC.put(it.next(), ccIdx);
		}
		return IntObjectPair.of(ccNum, vertexToCC);
	}

	static <V, E> IntObjectPair<Object2IntMap<V>> calcDirectedConnectivity(Graph<V, E> g) {
		int n = g.vertices().size();
		Object2ObjectMap<V, Set<V>> reach = new Object2ObjectOpenHashMap<>();
		for (V start : g.vertices()) {
			Set<V> vReach = new ObjectOpenHashSet<>();
			for (BfsIter<V, E> it = BfsIter.newInstance(g, start); it.hasNext();)
				vReach.add(it.next());
			reach.put(start, vReach);
		}

		int ccNum = 0;
		Object2IntMap<V> vertexToCC = new Object2IntOpenHashMap<>(n);
		vertexToCC.defaultReturnValue(-1);

		for (V u : g.vertices()) {
			if (vertexToCC.getInt(u) != -1)
				continue;
			int ccIdx = ccNum++;
			vertexToCC.put(u, ccIdx);
			for (V v : reach.get(u))
				if (reach.get(v).contains(u))
					vertexToCC.put(v, ccIdx);
		}
		return IntObjectPair.of(ccNum, vertexToCC);
	}

	static <V, E> void assertConnectivityResultsEqual(Graph<V, E> g, IntObjectPair<Object2IntMap<V>> r1,
			VertexPartition<V, E> r2) {
		assertEquals(r1.firstInt(), r2.numberOfBlocks());
		Int2IntMap cc1To2Map = new Int2IntOpenHashMap(r2.numberOfBlocks());
		for (V u : g.vertices()) {
			int cc1 = r1.second().getInt(u);
			int cc2 = r2.vertexBlock(u);
			if (cc1To2Map.containsKey(cc1)) {
				int cc1Mapped = cc1To2Map.get(cc1);
				assertEquals(cc1Mapped, cc2);
			} else {
				cc1To2Map.put(cc1, cc2);
			}
		}
	}

	static <V, E> void validateConnectivityResult(Graph<V, E> g, VertexPartition<V, E> res) {
		BitSet ccs = new BitSet();
		for (V v : g.vertices())
			ccs.set(res.vertexBlock(v));
		assertEquals(ccs.cardinality(), res.numberOfBlocks());
		for (int cc : range(res.numberOfBlocks()))
			assertTrue(ccs.get(cc));
	}

}
