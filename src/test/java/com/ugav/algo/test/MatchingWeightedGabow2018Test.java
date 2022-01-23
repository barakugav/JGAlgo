package com.ugav.algo.test;

import com.ugav.algo.MatchingWeightedGabow2018;

public class MatchingWeightedGabow2018Test {

	@Test
	public static boolean randBipartiteGraphsWeight1() {
//		TestUtils.initTestRand(-4170745660168808903L);
		return MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeighted() {
//		TestUtils.initTestRand(-7629286844248850371L);
		return MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randBipartiteGraphsWeightedPerfect() {
//		TestUtils.initTestRand(5405066339104234749L);
		return MatchingWeightedTestUtils.randBipartiteGraphsWeightedPerfect(MatchingWeightedGabow2018.getInstance());
	}

	@Test
	public static boolean randGraphsWeight1() {
//		TestUtils.initTestRand(5405066339104234749L);
		return MatchingUnweightedTestUtils.randGraphs(MatchingWeightedGabow2018.getInstance());
	}

//	@Test
//	public static boolean randGraphsWeighted() {
//		// 6085131206103358154 TODO
//		// 4690909470914004813 TODO
//		TestUtils.initTestRand(-7376937600184379829L);
//		return MatchingWeightedTestUtils.randGraphsWeighted(MatchingWeightedGabow2018.getInstance());
//	}

//	@Test
//	public static boolean randGraphsWeightedPerfect() {
//		return MatchingWeightedTestUtils.randGraphsWeightedPerfect(MatchingWeightedGabow2018.getInstance());
//	}

}
