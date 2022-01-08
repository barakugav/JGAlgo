package com.ugav.algo.test;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graphs;
import com.ugav.algo.SSSP;
import com.ugav.algo.SSSPBellmanFord;

public class SSSPBellmanFordTest {

	@Test
	public static boolean randGraphPositiveInt() {
		return SSSPTestUtils.testSSSPDirectedPositiveInt(SSSPBellmanFord.getInstace());
	}

	@Test
	public static boolean negaticCircle() {
		SSSP algo = SSSPBellmanFord.getInstace();

		int[][] adjacencyMatrix = { { 0, 1, 0, 0 }, { 0, 0, 2, 0 }, { 0, 0, 0, 6 }, { -10, 0, 0, 0 }, };
		int source = 0;

		Graph<Integer> g = GraphsTestUtils.createGraphFromAdjacencyMatrixWeightedInt(adjacencyMatrix,
				DirectedType.Directed);
		SSSP.Result<Integer> results = algo.calcDistances(g, Graphs.WEIGHT_INT_FUNC_DEFAULT, source);

		if (!results.foundNegativeCircle()) {
			TestUtils.printTestStr("Expected a negative circle to be found\n");
			return false;
		}

		return true;
	}

}
