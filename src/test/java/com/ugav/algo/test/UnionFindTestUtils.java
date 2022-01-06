package com.ugav.algo.test;

import java.util.Random;
import java.util.function.Supplier;

import com.ugav.algo.UnionFind;

class UnionFindTestUtils {

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

	private static void randUnionFindOps(int n, UnionFindOp[] ops) {
		Random rand = new Random(TestUtils.nextRandSeed());

		final int OP_FIND = 0;
		final int OP_UNION = 1;
		final int OP_NUM = 2;

		for (int m = 0; m < ops.length; m++) {
			switch (rand.nextInt(OP_NUM)) {
			case OP_FIND:
				ops[m] = new UnionFindOpFind(rand.nextInt(n));
				break;
			case OP_UNION:
				ops[m] = new UnionFindOpUnion(rand.nextInt(n), rand.nextInt(n));
				break;
			default:
				throw new InternalError();
			}
		}
	}

	static boolean randOps(Supplier<? extends UnionFind> builder) {
		return randOps(builder, 4096, 4096);
	}

	private static boolean randOps(Supplier<? extends UnionFind> builder, int n, int m) {
		UnionFindOp[] ops = new UnionFindOp[m];
		randUnionFindOps(n, ops);

		return unionFindOpsValidate(builder, n, ops);
	}

	private static boolean unionFindOpsValidate(Supplier<? extends UnionFind> builder, int n, UnionFindOp[] ops) {
		UnionFind uf = builder.get();
		int[] xs = new int[n];
		int group[] = new int[n];

		for (int i = 0; i < n; i++) {
			xs[i] = i;
			group[i] = i;
			uf.make();
		}

		for (UnionFindOp op0 : ops) {
			if (op0 instanceof UnionFindOpFind) {
				UnionFindOpFind op = (UnionFindOpFind) op0;
				int actual = uf.find(op.x);
				int actualGroup = group[actual];
				int expectedGroup = group[op.x];
				if (actualGroup != expectedGroup) {
					return false;
				}
			} else if (op0 instanceof UnionFindOpUnion) {
				UnionFindOpUnion op = (UnionFindOpUnion) op0;
				uf.union(op.a, op.b);
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

}
