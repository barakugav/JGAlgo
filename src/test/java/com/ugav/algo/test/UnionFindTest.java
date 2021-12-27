package com.ugav.algo.test;

import java.util.Random;

import com.ugav.algo.UnionFind;
import com.ugav.algo.UnionFindImpl;

public class UnionFindTest {

	private static class UnionFindOp {

	}

	private static class UnionFindOpFind extends UnionFindOp {
		final int x;

		UnionFindOpFind(int x) {
			this.x = x;
		}

	}

	private static class UnionFindOpUnion extends UnionFindOp {
		final int a;
		final int b;

		UnionFindOpUnion(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}

	private static void randUnionFindOps(int n, UnionFindOp[] ops, long seed) {
		Random rand = new Random(seed ^ 0x5ccbdb0409c5d81eL);

		for (int m = 0; m < ops.length; m++) {
			switch (rand.nextInt(2)) {
			case 0:
				ops[m] = new UnionFindOpFind(rand.nextInt(n));
				break;
			case 1:
				ops[m] = new UnionFindOpUnion(rand.nextInt(n), rand.nextInt(n));
				break;
			default:
				break;
			}
		}
	}

	private static boolean testUnionFind(UnionFind uf) {
		return testUnionFind(uf, 4096, 4096);
	}

	private static boolean testUnionFind(UnionFind uf, int n, int m) {
		long seed = Utils.randSeed();
		UnionFindOp[] ops = new UnionFindOp[m];
		randUnionFindOps(n, ops, seed);

		return unionFindOpsValidate(uf, n, ops);
	}

	private static boolean unionFindOpsValidate(UnionFind uf, int n, UnionFindOp[] ops) {
		int[] xs = new int[n];
		int group[] = new int[n];
		@SuppressWarnings("unchecked")
		UnionFind.Element<Integer>[] es = new UnionFind.Element[n];

		for (int i = 0; i < n; i++) {
			xs[i] = i;
			group[i] = i;
			es[i] = uf.make(i);
		}

		for (UnionFindOp op0 : ops) {
			if (op0 instanceof UnionFindOpFind) {
				UnionFindOpFind op = (UnionFindOpFind) op0;
				UnionFind.Element<Integer> actual = uf.find(es[op.x]);
				int actualGroup = group[actual.get()];
				int expectedGroup = group[op.x];
				if (actualGroup != expectedGroup) {
					return false;
				}
			} else if (op0 instanceof UnionFindOpUnion) {
				UnionFindOpUnion op = (UnionFindOpUnion) op0;
				uf.union(es[op.a], es[op.b]);
				int agroup = group[op.a];
				int bgroup = group[op.b];
				for (int i = 0; i < n; i++)
					if (group[i] == bgroup)
						group[i] = agroup;
			} else
				throw new IllegalArgumentException("Unkown unionfind op: " + op0);
		}
		return true;
	}

	@Test
	public static boolean regular() {
		return testUnionFind(UnionFindImpl.getInstance());
	}

}
