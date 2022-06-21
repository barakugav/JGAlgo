package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.GraphArray;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.MDSTTarjan1977;
import com.ugav.algo.MSTKruskal1956;
import com.ugav.algo.MatchingGabow1976;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MaxFlowEdmondsKarp;

class GraphImplTestUtils extends TestUtils {

	@FunctionalInterface
	static interface GraphImpl {
		<E> Graph<E> newGraph(DirectedType directedType, int... vertices);
	}

	static final GraphImpl GRAPH_IMPL_DEFAULT = new GraphImpl() {

		@Override
		public <E> Graph<E> newGraph(DirectedType directedType, int... vertices) {
			if (vertices.length == 1) {
				return new GraphArray<>(directedType, vertices[0]);
			} else {
				return new GraphBipartiteArray<>(directedType, vertices[0], vertices[1]);
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
