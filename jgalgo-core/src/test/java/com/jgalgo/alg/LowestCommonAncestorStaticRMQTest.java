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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class LowestCommonAncestorStaticRMQTest extends TestBase {

	static class Query<V> {
		V u;
		V v;
		V lca;

		Query(V u, V v, V lca) {
			this.u = u;
			this.v = v;
			this.lca = lca;
		}
	}

	static <V, E> Query<V>[] randLcaQueries(Graph<V, E> g, V root, int queriesNum, long seed) {
		Random rand = new Random(seed);
		@SuppressWarnings("unchecked")
		Query<V>[] queries = new Query[queriesNum];

		int n = g.vertices().size();
		Object2ObjectMap<V, V> parent = new Object2ObjectOpenHashMap<>(n);
		Object2IntMap<V> depth = new Object2IntOpenHashMap<>(n);

		for (BfsIter<V, E> it = BfsIter.newInstance(g, root); it.hasNext();) {
			V v = it.next();
			E e = it.lastEdge();
			if (e == null) {
				parent.put(v, null);
				depth.put(v, 0);
			} else {
				V p = g.edgeEndpoint(e, v);
				parent.put(v, p);
				depth.put(v, depth.getInt(p) + 1);
			}
		}

		for (int query = 0; query < queriesNum; query++) {
			V u = Graphs.randVertex(g, rand);
			V v = Graphs.randVertex(g, rand);

			int uDepth = depth.getInt(u), vDepth = depth.getInt(v);
			/* assume v is deeper */
			V up = u, vp = v;
			if (uDepth > vDepth) {
				up = v;
				vp = u;
				int temp = uDepth;
				uDepth = vDepth;
				vDepth = temp;
			}
			/* iterate up to equal level */
			for (; uDepth < vDepth; vDepth--)
				vp = parent.get(vp);
			while (!up.equals(vp)) {
				up = parent.get(up);
				vp = parent.get(vp);
			}
			V lca = up;

			queries[query] = new Query<>(u, v, lca);
		}
		return queries;
	}

	private static <V, E> void testLCA(Graph<V, E> g, V root, Supplier<? extends LowestCommonAncestorStatic> builder,
			Query<V>[] queries) {
		LowestCommonAncestorStatic lca = builder.get();
		LowestCommonAncestorStatic.DataStructure<V, E> lcaDS = lca.preProcessTree(g, root);

		for (Query<V> query : queries) {
			V expected = query.lca;
			V actual = lcaDS.findLca(query.u, query.v);
			assertEquals(expected, actual, "<- [" + query.u + "," + query.v + "]");
		}
	}

	@Test
	public void testRandTrees() {
		final long seed = 0x16f0491558fa62f8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.addPhase().withArgs(512, 512).repeat(16);
		tester.addPhase().withArgs(4096, 4096).repeat(4);
		tester.addPhase().withArgs(16384, 16384).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			Integer root = g.vertices().iterator().next();
			Query<Integer>[] queries = randLcaQueries(g, root, m, seedGen.nextSeed());
			testLCA(g, root, LowestCommonAncestorStaticRMQ::new, queries);
		});
	}

	@SuppressWarnings("boxing")
	@Test
	public void nonTree() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		LowestCommonAncestorStatic algo = new LowestCommonAncestorStaticRMQ();
		assertThrows(IllegalArgumentException.class, () -> algo.preProcessTree(g, 0));
	}

}
