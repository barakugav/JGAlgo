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
package com.jgalgo.example;

import com.jgalgo.alg.tree.LowestCommonAncestorDynamic;
import com.jgalgo.alg.tree.LowestCommonAncestorStatic;
import com.jgalgo.graph.Graph;

/**
 * This example demonstrates how to use the lowest common ancestor algorithm.
 *
 * @author Barak Ugav
 */
public class LowestCommonAncestorExample {

	private LowestCommonAncestorExample() {}

	/**
	 * This example demonstrates how to use the static lowest common ancestor algorithm.
	 */
	@SuppressWarnings("boxing")
	public static void staticLcaExample() {
		/* Create a full binary tree of height 3 */
		Graph<String, Integer> tree = Graph.newUndirected();
		tree.addVertex("root");
		tree.addVertex("Smith");
		tree.addVertex("Johnson");
		tree.addVertex("Williams");
		tree.addVertex("Jones");
		tree.addVertex("Brown");
		tree.addVertex("Davis");
		tree.addEdge("root", "Smith", 1);
		tree.addEdge("root", "Johnson", 2);
		tree.addEdge("Smith", "Williams", 3);
		tree.addEdge("Smith", "Jones", 4);
		tree.addEdge("Johnson", "Brown", 5);
		tree.addEdge("Johnson", "Davis", 6);

		/* Pre process the tree for LCA queries */
		LowestCommonAncestorStatic lcaAlgo = LowestCommonAncestorStatic.newInstance();
		LowestCommonAncestorStatic.DataStructure<String, Integer> lcaDs = lcaAlgo.preProcessTree(tree, "root");

		/* Find the lowest common ancestor of any pair of vertices in the tree */
		assert lcaDs.findLca("Smith", "Johnson").equals("root");
		assert lcaDs.findLca("Smith", "Williams").equals("Smith");
		assert lcaDs.findLca("Williams", "Jones").equals("Smith");
		assert lcaDs.findLca("Brown", "Davis").equals("Johnson");
		assert lcaDs.findLca("Williams", "Davis").equals("root");
	}

	/**
	 * This example demonstrates how to use the dynamic lowest common ancestor algorithm.
	 */
	public static void dynamicLcaExample() {
		/* Create a full binary tree of height 3 and perform LCA queries during the construction */
		LowestCommonAncestorDynamic lcaAlgo = LowestCommonAncestorDynamic.newInstance();
		LowestCommonAncestorDynamic.Vertex rt = lcaAlgo.initTree();

		LowestCommonAncestorDynamic.Vertex v1 = lcaAlgo.addLeaf(rt);
		LowestCommonAncestorDynamic.Vertex v2 = lcaAlgo.addLeaf(rt);
		assert lcaAlgo.findLowestCommonAncestor(v1, v2) == rt;

		LowestCommonAncestorDynamic.Vertex v3 = lcaAlgo.addLeaf(v1);
		assert lcaAlgo.findLowestCommonAncestor(v1, v3) == v1;

		LowestCommonAncestorDynamic.Vertex v4 = lcaAlgo.addLeaf(v1);
		assert lcaAlgo.findLowestCommonAncestor(v3, v4) == v1;

		LowestCommonAncestorDynamic.Vertex v5 = lcaAlgo.addLeaf(v2);
		LowestCommonAncestorDynamic.Vertex v6 = lcaAlgo.addLeaf(v2);
		assert lcaAlgo.findLowestCommonAncestor(v5, v6) == v2;
		assert lcaAlgo.findLowestCommonAncestor(v3, v6) == rt;
	}

	/**
	 * Main function that runs the examples.
	 *
	 * @param args main args
	 */
	public static void main(String[] args) {
		staticLcaExample();
		dynamicLcaExample();
	}

}
