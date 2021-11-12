package com.ugav.algo;

import com.ugav.algo.RMQPowerOf2Table.PowerOf2Table;

public abstract class RMQLinearAbstract implements RMQ {

    RMQLinearAbstract() {
    }

    void preprocessRMQ(DataStructure ds) {
	RMQ.Comperator comperator = ds.comperator;

	for (int b = 0; b < ds.blockNum; b++) {
	    int base = b * ds.blockSize;

	    int min = 0;
	    for (int i = 0; i < ds.blockSize - 1; i++) {
		if (comperator.compare(base + i + 1, base + min) < 0)
		    min = i + 1;
		ds.blocksLeftMinimum[b][i] = base + min;
	    }

	    min = ds.blockSize - 1;
	    for (int i = ds.blockSize - 2; i >= 0; i--) {
		if (comperator.compare(base + i, base + min) < 0)
		    min = i;
		ds.blocksRightMinimum[b][i] = base + min;
	    }
	}

	RMQPowerOf2Table.preprocessRMQ(ds.xlogxTable);

	initInterBlock(ds);
    }

    abstract void initInterBlock(DataStructure ds);

    static abstract class DataStructure implements RMQ.Result {

	final int n;
	final int blockSize;
	final int blockNum;
	final RMQ.Comperator comperator;

	final int blocksRightMinimum[][];
	final int blocksLeftMinimum[][];
	final PowerOf2Table xlogxTable;

	DataStructure(int n, RMQ.Comperator comperator) {
	    this.n = n;
	    this.comperator = comperator;

	    blockSize = getBlockSize(n);
	    blockNum = (int) Math.ceil((double) n / blockSize);

	    blocksRightMinimum = new int[blockNum][blockSize - 1];
	    blocksLeftMinimum = new int[blockNum][blockSize - 1];

	    xlogxTable = new PowerOf2Table(blockNum,
		    (i, j) -> comperator.compare(blocksRightMinimum[i][0], blocksRightMinimum[j][0]));
	}

	abstract int getBlockSize(int n);

	@Override
	public int query(int i, int j) {
	    if (i < 0 || j <= i || j > n)
		throw new IllegalArgumentException("Illegal indices [" + i + "," + j + "]");
	    if (i + 1 == j)
		return i;
	    j--;

	    int blk0 = i / blockSize;
	    int blk1 = j / blockSize;
	    int innerI = i % blockSize;
	    int innerJ = j % blockSize;

	    if (blk0 != blk1) {
		int blk0min = innerI == blockSize - 1 ? blk0 * blockSize + innerI : blocksRightMinimum[blk0][innerI];
		int blk1min = innerJ == 0 ? blk1 * blockSize + innerJ : blocksLeftMinimum[blk1][innerJ - 1];
		int min = comperator.compare(blk0min, blk1min) < 0 ? blk0min : blk1min;

		if (blk0 + 1 != blk1) {
		    int middleBlk = xlogxTable.query(blk0 + 1, blk1);
		    int middleMin = blocksRightMinimum[middleBlk][0];
		    min = comperator.compare(min, middleMin) < 0 ? min : middleMin;
		}

		return min;
	    } else {
		return queryInterBlock(blk0, innerI, innerJ);
	    }

	}

	abstract int queryInterBlock(int block, int i, int j);

    }

}
