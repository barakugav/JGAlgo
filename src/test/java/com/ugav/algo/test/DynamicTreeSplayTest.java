package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.ugav.algo.DebugPrintsManager;
import com.ugav.algo.DynamicTree;
import com.ugav.algo.DynamicTreeSplay;

public class DynamicTreeSplayTest extends TestUtils {

	@Test
	public static boolean randOps() {
		List<Phase> phases = List.of(phase(1024, 16), phase(256, 32), phase(256, 64), phase(128, 128), phase(64, 512),
				phase(64, 2048), phase(64, 4096), phase(32, 16384));
		return runTestMultiple(phases, (testIter, args) -> {
			int m = args[0];
			return randOps(m);
		});
	}

	private static enum Op {
		MakeTree, FindRoot, FindMinEdge, AddWeight, Link, Cut, Evert
	}

	private static class Node {
		final int tnode;
		Node parent;
		int edgeWeight;

		Node(int tnode) {
			this.tnode = tnode;
		}

		@Override
		public String toString() {
			return "<" + tnode + ">";
		}
	}

	private static boolean randOps(final int m) {
		DebugPrintsManager debug = new DebugPrintsManager(false);
		debug.println("\tnew iteration");
		Random rand = new Random(nextRandSeed());

		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut);
		final int MAX_WEIGHT = 10000000;
		final int MAX_WEIGHT_LINK = 1000;
		final int MAX_WEIGHT_ADD = 100;
		List<Node> nodes = new ArrayList<>();
		List<Node> roots = new ArrayList<>();
		DynamicTree<Void> tree = new DynamicTreeSplay<>(MAX_WEIGHT);

		Function<Node, Node> findRoot = node -> {
			Node root;
			for (root = node; root.parent != null;)
				root = root.parent;
			return root;
		};

		for (int i = 0; i < m;) {
			Op op = ops.get(rand.nextInt(ops.size()));
			switch (op) {
			case MakeTree: {
				int tnode = tree.makeTree();
				debug.println("" + op + "() -> " + tnode);
				Node node = new Node(tnode);
				nodes.add(node);
				roots.add(node);
				break;
			}
			case FindRoot: {
				if (nodes.isEmpty())
					continue;
				Node node = nodes.get(rand.nextInt(nodes.size()));
				debug.println("" + op + "(" + node + ")");

				Node root = findRoot.apply(node);
				int expected = root.tnode;

				int actual = tree.findRoot(node.tnode);

				if (expected != actual) {
					printTestStr("FindRoot failure: " + expected + " != " + actual + "\n");
					return false;
				}
				break;
			}
			case FindMinEdge: {
				if (nodes.isEmpty())
					continue;
				Node node = nodes.get(rand.nextInt(nodes.size()));
				debug.println("" + op + "(" + node + ")");

				Node min = null;
				for (Node p = node; p.parent != null; p = p.parent)
					if (min == null || p.edgeWeight <= min.edgeWeight)
						min = p;
				int[] expected = min != null ? new int[] { min.tnode, min.parent.tnode, min.edgeWeight } : null;

				DynamicTree.MinEdge<Void> actual0 = tree.findMinEdge(node.tnode);
				int[] actual = actual0 != null
						? new int[] { actual0.u(), actual0.v(), (int) Math.round(actual0.weight()) }
						: null;

				if (!Arrays.equals(expected, actual)) {
					printTestStr("FindMinEdge failure: " + Arrays.toString(expected) + " != " + Arrays.toString(actual)
							+ "\n");
					return false;
				}
				break;
			}
			case AddWeight: {
				if (nodes.isEmpty())
					continue;
				Node node = nodes.get(rand.nextInt(nodes.size()));
				int weight = rand.nextInt(MAX_WEIGHT_ADD * 2 + 1) - MAX_WEIGHT_ADD;
				debug.println("" + op + "(" + node + ", " + weight + ")");

				tree.addWeight(node.tnode, weight);

				for (Node p = node; p.parent != null; p = p.parent)
					p.edgeWeight += weight;

				break;
			}
			case Link: {
				if (roots.size() < 2)
					continue;
				final int RETRY_MAX = 100;
				Node a = null, b = null;
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
				debug.println("" + op + "(" + a + ", " + b + ", " + weight + ")");

				tree.link(a.tnode, b.tnode, weight, null);

				a.parent = b;
				a.edgeWeight = weight;
				roots.remove(a);

				break;
			}
			case Cut: {
				if (nodes.size() <= roots.size() || rand.nextInt(3) != 0)
					continue;
				final int RETRY_MAX = 100;
				Node node = null;
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
				debug.println("" + op + "(" + node + ")");

				node.parent = null;
				node.edgeWeight = 0;
				roots.add(node);

				tree.cut(node.tnode);
				break;
			}
			case Evert: {
				throw new UnsupportedOperationException();
			}
			default:
				throw new InternalError();
			}
			i++;
		}
		return true;
	}

}
