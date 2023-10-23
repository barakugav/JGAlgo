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

package com.jgalgo.alg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LowestCommonAncestorDynamicTestUtils extends TestUtils {

	private LowestCommonAncestorDynamicTestUtils() {}

	static void fullBinaryTreesRandOps(LowestCommonAncestorDynamic.Builder builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(64);
		tester.addPhase().withArgs(16, 32).repeat(64);
		tester.addPhase().withArgs(64, 64).repeat(32);
		tester.addPhase().withArgs(64, 128).repeat(32);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 2048).repeat(4);
		tester.addPhase().withArgs(1000, 4096).repeat(1);
		tester.run((n, m) -> {
			Collection<Op> ops = generateRandOpsOnFullBinaryTree(n, m, seedGen.nextSeed());
			testLCA(builder, n, ops);
		});
	}

	static void randTrees(LowestCommonAncestorDynamic.Builder builder, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(16, 32).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(512, 512).repeat(4);
		tester.addPhase().withArgs(512, 2048).repeat(4);
		tester.addPhase().withArgs(1000, 4096).repeat(1);
		tester.addPhase().withArgs(4100, 8000).repeat(1);
		tester.run((n, m) -> {
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

		List<Op> ops = new ObjectArrayList<>();
		int verticesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = verticesCount++;
		ops.add(new OpAddLeaf(root));
		verticesCount++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = (verticesCount - 1) / 2;
					ops.add(new OpAddLeaf(p));
					verticesCount++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(verticesCount);
					int y = rand.nextInt(verticesCount);
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

		List<Op> ops = new ObjectArrayList<>();
		int verticesCount = 0;

		/* insert first two elements */
		ops.add(new OpInitTree());
		int root = verticesCount++;
		ops.add(new OpAddLeaf(root));
		verticesCount++;

		for (int op : opsOrder) {
			switch (op) {
				case addLeafOp: {
					int p = rand.nextInt(verticesCount);
					ops.add(new OpAddLeaf(p));
					verticesCount++;
					break;
				}
				case lcaOp: {
					int x = rand.nextInt(verticesCount);
					int y = rand.nextInt(verticesCount);
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
		List<LowestCommonAncestorDynamic.Vertex> vertices = new ObjectArrayList<>();
		LowestCommonAncestorDynamic lca = builder.build();

		for (Op op0 : ops) {
			if (op0 instanceof OpInitTree) {
				LowestCommonAncestorDynamic.Vertex root = lca.initTree();
				root.setData(0);
				vertices.add(root);

			} else if (op0 instanceof OpAddLeaf) {
				OpAddLeaf op = (OpAddLeaf) op0;
				LowestCommonAncestorDynamic.Vertex parent = vertices.get(op.parent);
				LowestCommonAncestorDynamic.Vertex leaf = lca.addLeaf(parent);
				leaf.setData(parent.<Integer>getData() + 1);
				vertices.add(leaf);

			} else if (op0 instanceof OpLCAQuery) {
				OpLCAQuery op = (OpLCAQuery) op0;

				LowestCommonAncestorDynamic.Vertex x = vertices.get(op.x), y = vertices.get(op.y);
				if (x.<Integer>getData() > y.<Integer>getData()) {
					LowestCommonAncestorDynamic.Vertex temp = x;
					x = y;
					y = temp;
				}
				while (x.<Integer>getData() < y.<Integer>getData())
					y = y.getParent();
				while (x != y) {
					x = x.getParent();
					y = y.getParent();
				}

				LowestCommonAncestorDynamic.Vertex lcaExpected = x;
				LowestCommonAncestorDynamic.Vertex lcaActual =
						lca.findLowestCommonAncestor(vertices.get(op.x), vertices.get(op.y));
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
