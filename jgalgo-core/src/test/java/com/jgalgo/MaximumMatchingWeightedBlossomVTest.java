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

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public class MaximumMatchingWeightedBlossomVTest extends TestBase {

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

		MaximumMatchingWeightedBlossomV bv = new MaximumMatchingWeightedBlossomV();
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

		MaximumMatchingWeightedBlossomV bv = new MaximumMatchingWeightedBlossomV();
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

		MaximumMatchingWeightedBlossomV bv = new MaximumMatchingWeightedBlossomV();
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

		MaximumMatchingWeightedBlossomV bv = new MaximumMatchingWeightedBlossomV();
		Matching m = bv.computeMaximumWeightedPerfectMatching(g, w);

		MatchingUnweightedTestUtils.validateMatching(g, m);
	}

	@Test
	public void testRandBipartiteGraphsWeight1() {
		final long seed = 0xdc0142bc90ed9aa1L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MaximumMatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeighted() {
		final long seed = 0xcd3edea7e4e109a0L;
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(new MaximumMatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandBipartiteGraphsWeightedPerfect() {
		final long seed = 0xbb4dc78ba9bb50c8L;
		MaximumMatching algo = new MaximumMatchingWeightedBlossomV();

		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 4, 4, 8),phase(2560, 4, 4, 9), 
				phase(2560, 8, 8, 8), phase(2560, 8, 8, 16), phase(32, 16, 16, 64), phase(8, 128, 128, 128), phase(4, 128, 128, 512),
				phase(1, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			Graph g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, GraphsTestUtils.defaultGraphImpl(),
					seedGen.nextSeed());
			Weights.Bool partition = g.getVerticesWeights(Weights.DefaultBipartiteWeightKey);

			MaximumMatching cardinalityAlgo = new MaximumMatchingCardinalityBipartiteHopcroftKarp();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVerticesS = new IntArrayList(cardinalityMatch.unmatchedVertices());
			IntList unmatchedVerticesT = new IntArrayList(cardinalityMatch.unmatchedVertices());
			unmatchedVerticesS.removeIf(v -> partition.getBool(v));
			unmatchedVerticesT.removeIf(v -> !partition.getBool(v));
			assert unmatchedVerticesS.size() == unmatchedVerticesT.size();
			IntLists.shuffle(unmatchedVerticesS, new Random(seedGen.nextSeed()));
			IntLists.shuffle(unmatchedVerticesT, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVerticesS.size(); i++) {
				int u = unmatchedVerticesS.getInt(i);
				int v = unmatchedVerticesT.getInt(i);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MaximumMatching validationUnweightedAlgo = new MaximumMatchingCardinalityBipartiteHopcroftKarp();
			MaximumMatching validationWeightedAlgo = algo instanceof MaximumMatchingWeightedBipartiteHungarianMethodTest
					? new MaximumMatchingWeightedGabow1990()
					: new MaximumMatchingWeightedBipartiteHungarianMethod();
			MatchingWeightedTestUtils.testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo,
					validationWeightedAlgo);
		});
	}

	@Test
	public void testRandGraphsWeight1() {
		final long seed = 0x1a6996c7a2931d77L;
		MatchingUnweightedTestUtils.randGraphs(new MaximumMatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandGraphsWeighted() {
		final long seed = 0xd9e8826b15df790eL;
		MatchingWeightedTestUtils.randGraphsWeighted(new MaximumMatchingWeightedBlossomV(), seed);
	}

	@Test
	public void testRandGraphsWeightedPerfect() {
		final long seed = 0xba5ca7c6e494fa05L;
		MaximumMatching algo = new MaximumMatchingWeightedBlossomV();

		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 8, 8), phase(128, 16, 64), phase(12, 128, 128), phase(8, 128, 512),
				phase(2, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			if (g.vertices().size() % 2 != 0)
				throw new IllegalArgumentException("there is no perfect matching");

			MaximumMatching cardinalityAlgo = new MaximumMatchingCardinalityGabow1976();
			Matching cardinalityMatch = cardinalityAlgo.computeMaximumCardinalityMatching(g);
			IntList unmatchedVertices = new IntArrayList(cardinalityMatch.unmatchedVertices());
			assert unmatchedVertices.size() % 2 == 0;
			IntLists.shuffle(unmatchedVertices, new Random(seedGen.nextSeed()));
			for (int i = 0; i < unmatchedVertices.size() / 2; i++) {
				int u = unmatchedVertices.getInt(i * 2 + 0);
				int v = unmatchedVertices.getInt(i * 2 + 1);
				g.addEdge(u, v);
			}
			assert cardinalityAlgo.computeMaximumCardinalityMatching(g).isPerfect();

			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			WeightFunction.Int w =
					GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4, seedGen.nextSeed());

			MaximumMatching validationUnweightedAlgo = new MaximumMatchingCardinalityGabow1976();
			MaximumMatching validationWeightedAlgo = new MaximumMatchingWeightedGabow1990Simpler();
			MatchingWeightedTestUtils.testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo,
					validationWeightedAlgo);
		});
	}

}
