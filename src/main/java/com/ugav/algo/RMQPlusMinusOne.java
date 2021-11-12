package com.ugav.algo;

import java.util.HashMap;
import java.util.Objects;

public class RMQPlusMinusOne extends RMQLinearAbstract {

    private static final RMQPlusMinusOne INSTANCE = new RMQPlusMinusOne();

    public static RMQPlusMinusOne getInstace() {
	return INSTANCE;
    }

    @Override
    public RMQ.Result preprocessRMQ(RMQ.Comperator comperator, int n) {
	if (n <= 0)
	    throw new IllegalArgumentException();
	Objects.requireNonNull(comperator);

	RMQPlusMinusOne.DataStructure ds = new RMQPlusMinusOne.DataStructure(n, comperator);

	preprocessRMQ(ds);

	return ds;
    }

    int[] calcDemoBlock(int key, int blockSize) {
	int demoBlock[] = new int[blockSize];

	demoBlock[0] = 0;
	for (int i = 1; i < demoBlock.length; i++)
	    demoBlock[i] = demoBlock[i - 1] + ((key & (1 << (i - 1))) != 0 ? -1 : 1);

	return demoBlock;
    }

    void printDemoBlock(RMQPlusMinusOne.DataStructure ds, int k) {
	int demoBlock[] = calcDemoBlock(k, ds.blockSize);

	System.out.println("Demo block: (key = " + k + ")");
	for (int i = 0; i < demoBlock.length; i++)
	    System.out.print("" + demoBlock[i] + " ");
	System.out.println();
    }

    int calcBlockKey(RMQPlusMinusOne.DataStructure ds, int b) {
	int key = 0;

	int base = b * ds.blockSize;
	for (int i = ds.blockSize - 2; i >= 0; i--) {
	    key <<= 1;
	    if (ds.comperator.compare(base + i + 1, base + i) < 0)
		key |= 1;
	}

	return key;
    }

    @Override
    void initInterBlock(RMQLinearAbstract.DataStructure ds0) {
	RMQPlusMinusOne.DataStructure ds = (RMQPlusMinusOne.DataStructure) ds0;

	HashMap<Integer, RMQ.Result> tables = new HashMap<>();

	for (int b = 0; b < ds.blockNum; b++) {
	    int key = calcBlockKey(ds, b);

	    ds.interBlocksDs[b] = tables.computeIfAbsent(Integer.valueOf(key), k0 -> {
		int k = k0.intValue();
		int demoBlock[] = calcDemoBlock(k, ds.blockSize);

		return RMQLookupTable.getInstace().preprocessRMQ(new IntArrayComperator(demoBlock), demoBlock.length);
	    });
	}
    }

    static class DataStructure extends RMQLinearAbstract.DataStructure {

	final RMQ.Result interBlocksDs[];

	DataStructure(int n, Comperator comperator) {
	    super(n, comperator);
	    interBlocksDs = new RMQ.Result[blockNum];
	}

	@Override
	int getBlockSize(int n) {
	    return (int) Math.ceil(Utils.log2(n) / 2);
	}

	@Override
	int queryInterBlock(int block, int i, int j) {
	    return block * blockSize + interBlocksDs[block].query(i, j + 1);
	}

    }

}
