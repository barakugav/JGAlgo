package com.ugav.jgalgo;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.GraphImplTestUtils.GraphImpl;

public class GraphTableTest extends TestUtils {

	private static GraphImpl tableImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				return directed ? new GraphTableDirected(vertices) : new GraphTableUndirected(vertices);
			}
		};
	}

	@Test
	public void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(tableImpl());
	}

	@Test
	public void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(tableImpl());
	}

	@Test
	public void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(tableImpl());
	}

	@Test
	public void testUndirectedRandOps() {
		GraphImplTestUtils.testUndirectedRandOps(tableImpl());
	}

}
