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
package com.jgalgo.alg.tree;

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.tree.LowestCommonAncestorStaticRMQTest.Query;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;

public class LowestCommonAncestorOfflineUnionFindTest extends TestBase {

	@SuppressWarnings("unchecked")
	private static <V, E> void testLCA(Graph<V, E> g, V root, Supplier<? extends LowestCommonAncestorOffline> builder,
			Query<V>[] queries, Random rand) {
		LowestCommonAncestorOffline lca = builder.get();
		LowestCommonAncestorOffline.Queries<V, E> qs;
		if (g instanceof IntGraph && rand.nextBoolean()) {
			qs = (LowestCommonAncestorOffline.Queries<V, E>) LowestCommonAncestorOffline.IQueries.newInstance();
		} else {
			qs = LowestCommonAncestorOffline.Queries.newInstance(g);
		}
		if (rand.nextBoolean())
			/* wrap queries object with unknown implementation */
			qs = wrapQueries(qs);
		for (int q : range(queries.length))
			qs.addQuery(queries[q].u, queries[q].v);

		LowestCommonAncestorOffline.Result<V, E> lcaResult = lca.findLCAs(g, root, qs);
		assertEquals(qs.size(), lcaResult.size());

		for (int q : range(queries.length)) {
			V expected = queries[q].lca;
			V actual = lcaResult.getLca(q);
			assertEquals(expected, actual, "<- [" + queries[q].u + "," + queries[q].v + "]");
		}
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xd2ed7577f43a2461L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.addPhase().withArgs(512, 512).repeat(16);
		tester.addPhase().withArgs(4096, 4096).repeat(4);
		tester.addPhase().withArgs(16384, 16384).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);
			Integer root = g.vertices().iterator().next();
			Query<Integer>[] queries = LowestCommonAncestorStaticRMQTest.randLcaQueries(g, root, m, seedGen.nextSeed());
			testLCA(g, root, LowestCommonAncestorOfflineUnionFind::new, queries, rand);
		});
	}

	@SuppressWarnings("unchecked")
	private static <V, E> LowestCommonAncestorOffline.Queries<V, E> wrapQueries(
			LowestCommonAncestorOffline.Queries<V, E> q) {
		if (q instanceof LowestCommonAncestorOffline.IQueries) {
			LowestCommonAncestorOffline.IQueries q0 = (LowestCommonAncestorOffline.IQueries) q;
			return (LowestCommonAncestorOffline.Queries<V, E>) new LowestCommonAncestorOffline.IQueries() {
				@Override
				public void addQuery(int u, int v) {
					q0.addQuery(u, v);
				}

				@Override
				public int getQuerySourceInt(int idx) {
					return q0.getQuerySourceInt(idx);
				}

				@Override
				public int getQueryTargetInt(int idx) {
					return q0.getQueryTargetInt(idx);
				}

				@Override
				public int size() {
					return q.size();
				}

				@Override
				public void clear() {
					q.clear();
				}
			};
		} else {
			return new LowestCommonAncestorOffline.Queries<>() {
				@Override
				public void addQuery(V u, V v) {
					q.addQuery(u, v);
				}

				@Override
				public V getQuerySource(int idx) {
					return q.getQuerySource(idx);
				}

				@Override
				public V getQueryTarget(int idx) {
					return q.getQueryTarget(idx);
				}

				@Override
				public int size() {
					return q.size();
				}

				@Override
				public void clear() {
					q.clear();
				}
			};
		}
	}

	@Test
	public void testQueries() {
		final long seed = 0xd1ffafdee16120b8L;
		final Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randTree(16, rand.nextLong());

		IntGraph gInt = IntGraph.newUndirected();
		gInt.addVertices(range(16));

		testQueries(g, LowestCommonAncestorOffline.Queries.newInstance(g), rand);
		testQueries(g.indexGraph(), LowestCommonAncestorOffline.Queries.newInstance(g.indexGraph()), rand);
		testQueries(g, LowestCommonAncestorOffline.IQueries.newInstance(), rand);
		testQueries(g,
				LowestCommonAncestorOfflineUtils.asIntQueries(LowestCommonAncestorOffline.Queries.newInstance(g)),
				rand);
		testQueries(g.indexGraph(), new LowestCommonAncestorOfflineUtils.IndexQueriesFromObjQueries<>(g,
				LowestCommonAncestorOffline.Queries.newInstance(g)), rand);
		testQueries(gInt.indexGraph(), new LowestCommonAncestorOfflineUtils.IndexQueriesFromIntQueries(gInt,
				LowestCommonAncestorOffline.IQueries.newInstance()), rand);

		assertThrows(IllegalArgumentException.class, () -> LowestCommonAncestorOfflineUtils
				.indexQueriesFromQueries(gInt, LowestCommonAncestorOffline.Queries.newInstance(g)));

	}

	private static <V, E> void testQueries(Graph<V, E> g, LowestCommonAncestorOffline.Queries<V, E> queries,
			Random rand) {
		assertEquals(0, queries.size());

		List<Pair<V, V>> expected = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			V u = Graphs.randVertex(g, rand), v = Graphs.randVertex(g, rand);
			queries.addQuery(u, v);
			expected.add(Pair.of(u, v));
		}

		assertEquals(expected.size(), queries.size());
		for (int i : range(expected.size())) {
			Pair<V, V> p = expected.get(i);
			assertEquals(p.first(), queries.getQuerySource(i));
			assertEquals(p.second(), queries.getQueryTarget(i));
		}

		queries.clear();
		assertEquals(0, queries.size());
	}

	@Test
	public void testDefaultImpl() {
		LowestCommonAncestorOffline algo = LowestCommonAncestorOffline.newInstance();
		assertEquals(LowestCommonAncestorOfflineUnionFind.class, algo.getClass());
	}

}
