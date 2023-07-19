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

import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.TestBase;

public class MatchingWeightedBlossomVTest extends TestBase {

	@Test
	public void testSimpleNoBlossomsUnweighted() {
		IndexGraph g = IndexGraphFactory.newUndirected().newGraph();
		int v0 = g.addVertex();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		g.addEdge(v0, v1);
		g.addEdge(v1, v2);
		g.addEdge(v2, v3);
		g.addEdge(v3, v0);

		MatchingWeightedBlossomV bv = new MatchingWeightedBlossomV();
		Matching m = bv.computeMaximumWeightedPerfectMatching(g, WeightFunction.CardinalityWeightFunction);

		MatchingUnweightedTestUtils.validateMatching(g, m);
	}

	@Test
	public void testSimpleNoBlossomsWeighted() {
		IndexGraph g = IndexGraphFactory.newUndirected().newGraph();
		int v0 = g.addVertex();
		int v1 = g.addVertex();
		int v2 = g.addVertex();
		int v3 = g.addVertex();
		int e0 = g.addEdge(v0, v1);
		int e1 = g.addEdge(v1, v2);
		int e2 = g.addEdge(v2, v3);
		int e3 = g.addEdge(v3, v0);
		Weights.Int w = g.addEdgesWeights("weights", int.class);
		w.set(e0, 22);
		w.set(e1, 36);
		w.set(e2, 68);
		w.set(e3, 53);

		MatchingWeightedBlossomV bv = new MatchingWeightedBlossomV();
		Matching m = bv.computeMaximumWeightedPerfectMatching(g, w);

		MatchingUnweightedTestUtils.validateMatching(g, m);
	}

	@Test
	public void testSimpleWithBlossomsUnweighted() {
		IndexGraph g = IndexGraphFactory.newUndirected().newGraph();
		int v00 = g.addVertex();
		int v01 = g.addVertex();
		int v02 = g.addVertex();
		int v03 = g.addVertex();
		int v04 = g.addVertex();
		g.addEdge(v00, v01);
		g.addEdge(v00, v02);
		g.addEdge(v01, v03);
		g.addEdge(v02, v04);
		g.addEdge(v03, v04);

		int v10 = g.addVertex();
		int v11 = g.addVertex();
		int v12 = g.addVertex();
		int v13 = g.addVertex();
		int v14 = g.addVertex();
		g.addEdge(v10, v11);
		g.addEdge(v10, v12);
		g.addEdge(v11, v13);
		g.addEdge(v12, v14);
		g.addEdge(v13, v14);

		g.addEdge(v04, v14);

		MatchingWeightedBlossomV bv = new MatchingWeightedBlossomV();
		Matching m = bv.computeMaximumWeightedPerfectMatching(g, WeightFunction.CardinalityWeightFunction);

		MatchingUnweightedTestUtils.validateMatching(g, m);
	}

	@Test
	public void testSimpleWithBlossomsWeighted() {
		IndexGraph g = IndexGraphFactory.newUndirected().newGraph();
		int v00 = g.addVertex();
		int v01 = g.addVertex();
		int v02 = g.addVertex();
		int v03 = g.addVertex();
		int v04 = g.addVertex();
		int e00 = g.addEdge(v00, v01);
		int e01 = g.addEdge(v00, v02);
		int e02 = g.addEdge(v01, v03);
		int e03 = g.addEdge(v02, v04);
		int e04 = g.addEdge(v03, v04);

		int v10 = g.addVertex();
		int v11 = g.addVertex();
		int v12 = g.addVertex();
		int v13 = g.addVertex();
		int v14 = g.addVertex();
		int e10 = g.addEdge(v10, v11);
		int e11 = g.addEdge(v10, v12);
		int e12 = g.addEdge(v11, v13);
		int e13 = g.addEdge(v12, v14);
		int e14 = g.addEdge(v13, v14);

		int e20 = g.addEdge(v04, v14);

		Weights.Int w = g.addEdgesWeights("weights", int.class);
		w.set(e00, 10);
		w.set(e01, 10);
		w.set(e02, 2);
		w.set(e03, 2);
		w.set(e04, 6);
		w.set(e10, 10);
		w.set(e11, 10);
		w.set(e12, 2);
		w.set(e13, 2);
		w.set(e14, 6);
		w.set(e20, 40);

		MatchingWeightedBlossomV bv = new MatchingWeightedBlossomV();
		Matching m = bv.computeMaximumWeightedPerfectMatching(g, w);

		MatchingUnweightedTestUtils.validateMatching(g, m);
	}

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0xdc0142bc90ed9aa1L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0xcd3edea7e4e109a0L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0xbb4dc78ba9bb50c8L;
		MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(new MatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x1a6996c7a2931d77L;
		MatchingUnweightedTestUtils.randGraphs(new MatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0xd9e8826b15df790eL;
		MatchingWeightedTestUtils.randGraphsWeighted(new MatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		final long seed = 0xba5ca7c6e494fa05L;
		MatchingWeightedTestUtils.randGraphsWeightedPerfect(new MatchingWeightedBlossomV(), seed);
	}

}
