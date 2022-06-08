package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class TestList {

	private static final Collection<Class<?>> RMQ_TESTS = List.of(
			RMQLookupTableTest.class,
			RMQPowerOf2TableTest.class,
			RMQPlusMinusOneBenderFarachColton2000Test.class,
			RMQGabowBentleyTarjan1984Test.class);

	private static final Collection<Class<?>> HEAP_TESTS = List.of(
			HeapBinaryTest.class,
			HeapBinomialTest.class,
			HeapFibonacciTest.class,
			RedBlackTreeTest.class,
			RedBlackTreeExtendedTest.class,
			SplayTreeTest.class,
			DynamicTreeSplayTest.class);

	private static final Collection<Class<?>> GRAPHS_TESTS = List.of(
			GraphsTest.class,
			GraphArrayTest.class,
			GraphLinkedTest.class,
			GraphTableTest.class);

	private static final Collection<Class<?>> MISC_TESTS = List.of(
			ArraysTest.class,
			UnionFindPtrTest.class,
			UnionFindArrayTest.class,
			UnionFindValueArrayTest.class,
			SplitFindMinArrayTest.class,
			LCARMQBenderFarachColton2000Test.class,
			LCAGabowSimpleTest.class,
			LCAGabow2017Test.class,
			TPMKomlos1985King1997Hagerup2009Test.class,
			SubtreeMergeFindminTest.class,
			MaxFlowEdmondsKarpTest.class,
			MaxFlowPushRelabelTest.class,
			MaxFlowPushRelabelWithDynamicTreesTest.class,
			MaxFlowDinicTest.class,
			TSPMetricTest.class);

	private static final Collection<Class<?>> SHORTEST_PATH_TESTS = List.of(
			SSSPDijkstraTest.class,
			SSSPDial1969Test.class,
			SSSPBellmanFordTest.class,
			SSSPGoldberg1995Test.class);

	private static final Collection<Class<?>> MST_TESTS = List.of(
			MSTBoruvka1926Test.class,
			MSTKruskal1956Test.class,
			MSTPrim1957Test.class,
			MSTYao1976Test.class,
			MSTFredmanTarjan1987Test.class,
			MSTKargerKleinTarjan1995Test.class,
			MDSTTarjan1977Test.class,
			MSTPrefTest.class);

	private static final Collection<Class<?>> MATCHING_TESTS = List.of(
			MatchingBipartiteHopcroftKarp1973Test.class,
			MatchingGabow1976Test.class,
			MatchingWeightedBipartiteSSSPTest.class,
			MatchingWeightedBipartiteHungarianMethodTest.class,
			MatchingWeightedGabow2017Test.class);

	static final Collection<Class<?>> TEST_CLASSES;
	static {
		Collection<Class<?>> l = new ArrayList<>();
		l.addAll(GRAPHS_TESTS);
		l.addAll(RMQ_TESTS);
		l.addAll(HEAP_TESTS);
		l.addAll(MISC_TESTS);
		l.addAll(SHORTEST_PATH_TESTS);
		l.addAll(MST_TESTS);
		l.addAll(MATCHING_TESTS);
		TEST_CLASSES = Collections.unmodifiableCollection(l);
	}

}
