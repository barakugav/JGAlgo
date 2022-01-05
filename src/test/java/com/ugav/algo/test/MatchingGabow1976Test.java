package com.ugav.algo.test;

import java.util.Collection;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.MatchingBipartite;
import com.ugav.algo.MatchingGabow1976;

public class MatchingGabow1976Test {

	@Test
	public static boolean randBipartiteGraphs() {
		return MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingBipartite() {

			@Override
			public <E> Collection<Edge<E>> calcMaxMatching(GraphBipartite<E> g) {
				return MatchingGabow1976.getInstance().calcMaxMatching(g);
			}
		});
	}

	@Test
	public static boolean randGraphs() {
		return MatchingTestUtils.randGraphs(MatchingGabow1976.getInstance());
	}

}
