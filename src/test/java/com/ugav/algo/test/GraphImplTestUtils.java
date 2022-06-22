package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.GraphArrayDirected;
import com.ugav.algo.GraphArrayUndirected;
import com.ugav.algo.GraphBipartiteArrayDirected;
import com.ugav.algo.GraphBipartiteArrayUndirected;
import com.ugav.algo.MDSTTarjan1977;
import com.ugav.algo.MSTKruskal1956;
import com.ugav.algo.MatchingGabow1976;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MaxFlowEdmondsKarp;

class GraphImplTestUtils extends TestUtils {

	@FunctionalInterface
	static interface GraphImpl {
		<E> Graph<E> newGraph(boolean directed, int... vertices);
	}

	static final GraphImpl GRAPH_IMPL_DEFAULT = new GraphImpl() {

		@Override
		public <E> Graph<E> newGraph(boolean directed, int... vertices) {
			if (directed) {
				if (vertices.length == 1) {
					return new GraphArrayDirected<>(vertices[0]);
				} else {
					return new GraphBipartiteArrayDirected<>(vertices[0], vertices[1]);
				}
			} else {
				if (vertices.length == 1) {
					return new GraphArrayUndirected<>(vertices[0]);
				} else {
					return new GraphBipartiteArrayUndirected<>(vertices[0], vertices[1]);
				}
			}
		}
	};

	static void testUndirectedMST(GraphImpl graphImpl) {
		MSTTestUtils.testRandGraph(MSTKruskal1956::new, graphImpl);
	}

	static void testDirectedMDST(GraphImpl graphImpl) {
		MDSTTarjan1977Test.testRandGraph(MDSTTarjan1977::new, graphImpl);
	}

	static void testDirectedMaxFlow(GraphImpl graphImpl) {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new, graphImpl);
	}

	static void testUndirectedBipartiteMatching(GraphImpl graphImpl) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::new, graphImpl);
	}

	static void testUndirectedBipartiteMatchingWeighted(GraphImpl graphImpl) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new, graphImpl);
	}

}
