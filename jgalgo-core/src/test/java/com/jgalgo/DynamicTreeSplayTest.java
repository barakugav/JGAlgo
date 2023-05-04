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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

public class DynamicTreeSplayTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0xc5fb8821e8139b3eL;
		testRandOps(DynamicTreeSplay::new, seed);
	}

	@Test
	public void testRandOpsInt() {
		final long seed = 0xdaf8a976847115a1L;
		testRandOps(maxWeight -> new DynamicTreeSplayInt((int) maxWeight), seed);
	}

	static void testRandOps(DoubleFunction<? extends DynamicTree> builder, long seed) {
		testRandOps(builder, List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut), seed);
	}

	static void testRandOps(DoubleFunction<? extends DynamicTree> builder, List<Op> ops, long seed) {
		testRandOps(builder, ops, null, seed);
	}

	static void testRandOps(DoubleFunction<? extends DynamicTree> builder, List<Op> ops,
			ToIntFunction<DynamicTree.Node> sizeFunc, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(256, 16), phase(64, 32), phase(64, 64), phase(32, 128), phase(16, 512),
				phase(16, 2048), phase(8, 4096), phase(4, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int m = args[0];
			testRandOps(builder, m, ops, sizeFunc, seedGen.nextSeed());
		});
	}

	static enum Op {
		MakeTree, FindRoot, FindMinEdge, AddWeight, Link, Cut, Size
	}

	static class TrackerNode {
		final int id;
		DynamicTree.Node dtNode;
		TrackerNode parent;
		final List<TrackerNode> children = new ArrayList<>();
		int edgeWeight;

		TrackerNode(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "<" + id + ">";
		}
	}

	@SuppressWarnings("boxing")
	private static void testRandOps(DoubleFunction<? extends DynamicTree> builder, final int m, List<Op> ops,
			ToIntFunction<DynamicTree.Node> sizeFunc, long seed) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		debug.println("\tnew iteration");
		Random rand = new Random(seed);

		final int MAX_WEIGHT = 10000000;
		final int MAX_WEIGHT_LINK = 1000;
		final int MAX_WEIGHT_ADD = 100;
		List<TrackerNode> nodes = new ArrayList<>();
		List<TrackerNode> roots = new ArrayList<>();
		DynamicTree tree = builder.apply(MAX_WEIGHT);

		Function<TrackerNode, TrackerNode> findRoot = node -> {
			TrackerNode root;
			for (root = node; root.parent != null;)
				root = root.parent;
			return root;
		};

		for (int i = 0; i < m;) {
			Op op = ops.get(rand.nextInt(ops.size()));
			switch (op) {
				case MakeTree: {
					TrackerNode node = new TrackerNode(nodes.size());
					DynamicTree.Node dtNode = tree.makeTree();
					node.dtNode = dtNode;
					debug.println(op, "() -> ", dtNode);
					nodes.add(node);
					roots.add(node);
					break;
				}
				case FindRoot: {
					if (nodes.isEmpty())
						continue;
					TrackerNode node = nodes.get(rand.nextInt(nodes.size()));
					debug.println(op, "(", node, ")");

					TrackerNode root = findRoot.apply(node);
					DynamicTree.Node expected = root.dtNode;
					DynamicTree.Node actual = tree.findRoot(node.dtNode);
					assertEquals(expected, actual, "FindRoot failure");
					break;
				}
				case FindMinEdge: {
					if (nodes.isEmpty())
						continue;
					TrackerNode node = nodes.get(rand.nextInt(nodes.size()));
					debug.println(op, "(", node, ")");

					TrackerNode min = null;
					for (TrackerNode p = node; p.parent != null; p = p.parent)
						if (min == null || p.edgeWeight <= min.edgeWeight)
							min = p;
					Object[] expected = min != null ? new Object[] { min.dtNode, min.edgeWeight } : null;

					DynamicTree.MinEdge actual0 = tree.findMinEdge(node.dtNode);
					Object[] actual =
							actual0 != null ? new Object[] { actual0.source(), (int) Math.round(actual0.weight()) }
									: null;

					assertTrue(Arrays.equals(expected, actual),
							"FindMinEdge failure: " + Arrays.toString(expected) + " != " + Arrays.toString(actual));
					break;
				}
				case AddWeight: {
					if (nodes.isEmpty())
						continue;
					TrackerNode node = nodes.get(rand.nextInt(nodes.size()));
					int weight = rand.nextInt(MAX_WEIGHT_ADD * 2 + 1) - MAX_WEIGHT_ADD;
					debug.println(op, "(", node, ", ", weight, ")");

					tree.addWeight(node.dtNode, weight);

					for (TrackerNode p = node; p.parent != null; p = p.parent)
						p.edgeWeight += weight;

					break;
				}
				case Link: {
					if (roots.size() < 2)
						continue;
					final int RETRY_MAX = 100;
					TrackerNode a = null, b = null;
					boolean found = false;
					for (int r = 0; r < RETRY_MAX; r++) {
						a = roots.get(rand.nextInt(roots.size()));
						b = nodes.get(rand.nextInt(nodes.size()));
						if (findRoot.apply(b) != a) {
							found = true;
							break;
						}
					}
					if (!found)
						continue;
					assert a.parent == null;
					int weight = rand.nextInt(MAX_WEIGHT_LINK);
					debug.println(op, "(", a, ", ", b, ", ", weight, ")");

					tree.link(a.dtNode, b.dtNode, weight);

					a.parent = b;
					a.edgeWeight = weight;
					b.children.add(a);
					roots.remove(a);

					break;
				}
				case Cut: {
					if (nodes.size() <= roots.size() || rand.nextInt(3) != 0)
						continue;
					final int RETRY_MAX = 100;
					TrackerNode node = null;
					boolean found = false;
					for (int r = 0; r < RETRY_MAX; r++) {
						node = nodes.get(rand.nextInt(nodes.size()));
						if (node.parent != null) {
							found = true;
							break;
						}
					}
					if (!found)
						continue;
					debug.println(op, "(", node, ")");

					node.parent.children.remove(node);
					node.parent = null;
					node.edgeWeight = 0;
					roots.add(node);

					tree.cut(node.dtNode);
					break;
				}
				case Size: {
					if (nodes.isEmpty())
						continue;
					TrackerNode node = nodes.get(rand.nextInt(nodes.size()));

					int expected = 0;
					List<TrackerNode> stack = new ArrayList<>();
					stack.add(findRoot.apply(node));
					while (!stack.isEmpty()) {
						expected++;
						TrackerNode n = stack.get(stack.size() - 1);
						stack.remove(stack.size() - 1);
						stack.addAll(n.children);
					}

					int actual = sizeFunc.applyAsInt(node.dtNode);
					assertEquals(expected, actual, "Wrong size");
					break;
				}
				default:
					throw new IllegalStateException();
			}
			i++;
		}
	}

}
