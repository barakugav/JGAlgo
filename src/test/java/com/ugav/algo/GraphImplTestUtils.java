package com.ugav.algo;

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
					return new GraphArrayDirectedOld<>(vertices[0]);
				} else {
					return new GraphBipartiteArrayDirected<>(vertices[0], vertices[1]);
				}
			} else {
				if (vertices.length == 1) {
					return new GraphArrayUndirectedOld<>(vertices[0]);
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
