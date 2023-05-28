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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import com.jgalgo.GraphsTestUtils.RandomGraphBuilder;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphViewsTest extends TestBase {

	@Test
	public void unmodifiableView() {
		final long seed = 0x23c189a8ecd10232L;
		final Random rand = new Random(seed);
		Graph orig = new RandomGraphBuilder(seed).n(1005).m(8465).directed(true).parallelEdges(true).selfEdges(true)
				.cycles(true).connected(false).build();
		Graph unmodifiable = orig.unmodifiableView();

		Runnable checkGraphs = () -> {
			assertEquals(orig.vertices(), unmodifiable.vertices());
			assertEquals(orig.edges(), unmodifiable.edges());
			for (IntIterator eit = orig.edges().iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				assertEquals(orig.edgeSource(e), unmodifiable.edgeSource(e));
				assertEquals(orig.edgeTarget(e), unmodifiable.edgeTarget(e));
			}
			for (IntIterator uit = orig.vertices().iterator(); uit.hasNext();) {
				int u = uit.nextInt();
				assertEquals(setOf(orig.edgesOut(u)), setOf(unmodifiable.edgesOut(u)));
				assertEquals(setOf(orig.edgesIn(u)), setOf(unmodifiable.edgesIn(u)));

				for (EdgeIter eit = unmodifiable.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					assertEquals(u, eit.source());
					assertEquals(orig.edgeEndpoint(e, u), eit.target());
				}
			}
		};

		Consumer<Graph> modifyGraph = g -> {
			int v1 = g.addVertex();
			int v2 = g.addVertex();
			int v3 = g.addVertex();
			g.addEdge(v1, v2);
			g.addEdge(v2, v3);
			g.addEdge(v3, v1);
			int[] vs = g.vertices().toIntArray();
			g.removeVertex(vs[rand.nextInt(vs.length)]);
		};

		/* Check the graph */
		checkGraphs.run();

		/* Modify the original graph and check the view is updating */
		modifyGraph.accept(orig);
		checkGraphs.run();
	}

	@Test
	public void reversedView() {
		final long seed = 0xe472d90a46061b2fL;
		final Random rand = new Random(seed);
		Graph orig = new RandomGraphBuilder(seed).n(1005).m(8465).directed(true).parallelEdges(true).selfEdges(true)
				.cycles(true).connected(false).build();
		Graph rev = orig.reverseView();

		Runnable checkGraphs = () -> {
			assertEquals(orig.vertices(), rev.vertices());
			assertEquals(orig.edges(), rev.edges());
			for (IntIterator eit = orig.edges().iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				assertEquals(orig.edgeSource(e), rev.edgeTarget(e));
				assertEquals(orig.edgeTarget(e), rev.edgeSource(e));
			}
			for (IntIterator uit = orig.vertices().iterator(); uit.hasNext();) {
				int u = uit.nextInt();
				assertEquals(setOf(orig.edgesOut(u)), setOf(rev.edgesIn(u)));
				assertEquals(setOf(orig.edgesIn(u)), setOf(rev.edgesOut(u)));

				for (EdgeIter eit = rev.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					assertEquals(u, eit.source());
					assertEquals(orig.edgeEndpoint(e, u), eit.target());
				}
			}
		};

		Consumer<Graph> modifyGraph = g -> {
			int v1 = g.addVertex();
			int v2 = g.addVertex();
			int v3 = g.addVertex();
			g.addEdge(v1, v2);
			g.addEdge(v2, v3);
			g.addEdge(v3, v1);
			int[] vs = g.vertices().toIntArray();
			g.removeVertex(vs[rand.nextInt(vs.length)]);
		};

		/* Check the graph */
		checkGraphs.run();

		/* Modify the original graph and check the view is updating */
		modifyGraph.accept(orig);
		checkGraphs.run();

		/* Modify the view graph and check the original graph is updating */
		modifyGraph.accept(rev);
		checkGraphs.run();
	}

	private static IntSet setOf(IntIterator it) {
		return new IntOpenHashSet(it);
	}

}
