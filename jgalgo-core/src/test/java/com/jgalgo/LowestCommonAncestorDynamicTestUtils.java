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

package com.jgalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import it.unimi.dsi.fastutil.ints.IntArrays;

public class LowestCommonAncestorDynamicTestUtils extends TestUtils {

	private LowestCommonAncestorDynamicTestUtils() {}

	static void fullBinaryTreesRandOps(LowestCommonAncestorDynamic.Builder builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(64, 16, 16), phase(64, 16, 32), phase(32, 64, 64), phase(32, 64, 128),
				phase(4, 512, 512), phase(4, 512, 2048), phase(1, 1000, 4096));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Collection<Op> ops = generateRandOpsOnFullBinaryTree(n, m, seedGen.nextSeed());
			testLCA(builder, n, ops);
		});
	}

	static void randTrees(LowestCommonAncestorDynamic.Builder builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64), phase(64, 64, 128),
				phase(4, 512, 512), phase(4, 512, 2048), phase(1, 1000, 4096), phase(1, 4100, 8000));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Collection<Op> ops = generateRandOps(n, m, seedGen.nextSeed());
			testLCA(builder, n, ops);
		});
	}

	static Collection<Op> generateRandOpsOnFullBinaryTree(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		IntArrays.shuffle(opsOrder, rand);

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = (nodesCount - 1) / 2;
					ops.add(new OpAddLeaf(p));
					nodesCount++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(nodesCount);
					int y = rand.nextInt(nodesCount);
					ops.add(new OpLCAQuery(x, y));
					break;
				}
				default:
					throw new IllegalStateException();
			}
		}
		return ops;
	}

	public static Collection<Op> generateRandOps(int n, int m, long seed) {
		if (n < 2)
			throw new IllegalArgumentException();
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());

		final int addLeafOp = 0;
		final int lcaOp = 1;
		int[] opsOrder = new int[n - 2 + m];
		Arrays.fill(opsOrder, 0, n - 2, addLeafOp);
		Arrays.fill(opsOrder, n - 2, n - 2 + m, lcaOp);
		IntArrays.shuffle(opsOrder, rand);

		List<Op> ops = new ArrayList<>();
		int nodesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = nodesCount++;
		ops.add(new OpAddLeaf(root));
		nodesCount++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = rand.nextInt(nodesCount);
					ops.add(new OpAddLeaf(p));
					nodesCount++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(nodesCount);
					int y = rand.nextInt(nodesCount);
					ops.add(new OpLCAQuery(x, y));
					break;
				}
				default:
					throw new IllegalStateException();
			}
		}
		return ops;
	}

	@SuppressWarnings("boxing")
	static void testLCA(LowestCommonAncestorDynamic.Builder builder, int n, Collection<Op> ops) {
		List<LowestCommonAncestorDynamic.Node> nodes = new ArrayList<>();
		LowestCommonAncestorDynamic lca = builder.build();

		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				LowestCommonAncestorDynamic.Node root = lca.initTree();
				root.setNodeData(0);
				nodes.add(root);

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				LowestCommonAncestorDynamic.Node parent = nodes.get(op.parent);
				LowestCommonAncestorDynamic.Node leaf = lca.addLeaf(parent);
				leaf.setNodeData(parent.<Integer>getNodeData() + 1);
				nodes.add(leaf);

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;

				LowestCommonAncestorDynamic.Node x = nodes.get(op.x), y = nodes.get(op.y);
				if (x.<Integer>getNodeData() > y.<Integer>getNodeData()) {
					LowestCommonAncestorDynamic.Node temp = x;
					x = y;
					y = temp;
				}
				while (x.<Integer>getNodeData() < y.<Integer>getNodeData())
					y = y.getParent();
				while (x != y) {
					x = x.getParent();
					y = y.getParent();
				}

				LowestCommonAncestorDynamic.Node lcaExpected = x;
				LowestCommonAncestorDynamic.Node lcaActual = lca.findLowestCommonAncestor(nodes.get(op.x), nodes.get(op.y));
				assertEquals(lcaExpected, lcaActual, "LCA has an expected value");

			} else {
				throw new IllegalStateException();
			}
		}
	}

	public static class Op {
	}

	public static class OpInitTree extends Op {
	}

	public static class OpAddLeaf extends Op {
		public final int parent;

		OpAddLeaf(int parent) {
			this.parent = parent;
		}
	}

	public static class OpLCAQuery extends Op {
		public final int x, y;

		OpLCAQuery(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}
