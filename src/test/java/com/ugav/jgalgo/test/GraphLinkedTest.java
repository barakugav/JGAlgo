package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.Graph;
import com.ugav.jgalgo.GraphLinkedDirected;
import com.ugav.jgalgo.GraphLinkedUndirected;
import com.ugav.jgalgo.test.GraphImplTestUtils.GraphImpl;

public class GraphLinkedTest extends TestUtils {

	private static GraphImpl graphImpl() {
		return new GraphImpl() {

			@Override
			public Graph newGraph(boolean directed, int vertices) {
				if (directed) {
					return new GraphLinkedDirected(vertices);
				} else {
					return new GraphLinkedUndirected(vertices);
				}
			}
		};
	}

	@Test
	public void testVertexAdd() {
		GraphImplTestUtils.testVertexAdd(graphImpl());
	}

	@Test
	public void testCreateWithNVertices() {
		GraphImplTestUtils.testCreateWithNVertices(graphImpl());
	}

	@Test
	public void testAddEdge() {
		GraphImplTestUtils.testAddEdge(graphImpl());
	}

	@Test
	public void testGetEdge() {
		GraphImplTestUtils.testGetEdge(graphImpl());
	}

	@Test
	public void testGetEdges() {
		GraphImplTestUtils.testGetEdges(graphImpl());
	}

	@Test
	public void testEdgeIter() {
		GraphImplTestUtils.testEdgeIter(graphImpl());
	}

	@Test
	public void testDgree() {
		GraphImplTestUtils.testDgree(graphImpl());
	}

	@Test
	public void testUndirectedMST() {
		GraphImplTestUtils.testUndirectedMST(graphImpl());
	}

	@Test
	public void testDirectedMDST() {
		GraphImplTestUtils.testDirectedMDST(graphImpl());
	}

	@Test
	public void testDirectedMaxFlow() {
		GraphImplTestUtils.testDirectedMaxFlow(graphImpl());
	}

	@Test
	public void testUndirectedRandOps() {
		GraphImplTestUtils.testUndirectedRandOps(graphImpl());
	}

}
