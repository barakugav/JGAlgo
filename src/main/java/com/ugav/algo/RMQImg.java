package com.ugav.algo;

import java.util.Objects;

public class RMQImg implements RMQ {

    private RMQImg() {
    }

    private static final RMQImg INSTANCE = new RMQImg();

    public static RMQImg getInstace() {
	return INSTANCE;
    }

    @Override
    public RMQ.Result preprocessRMQ(RMQ.Comperator comperator, int n) {
	if (n <= 0)
	    throw new IllegalArgumentException();
	Objects.requireNonNull(comperator);

	RMQDataStructure dataStructure = new RMQDataStructure(n, comperator);
	// dataStructure.init();
	return dataStructure;
    }

    private static class RMQDataStructure implements RMQ.Result {

	final int n;
	final int blockSize;
	final int blockNum;
	final Block[] blocks;
	final RMQ.Comperator comperator;

	RMQDataStructure(int n, RMQ.Comperator comperator) {
	    this.n = n;
	    this.comperator = comperator;

	    blockSize = 5; /* TODO */
	    blockNum = 5; /* TODO */
	    blocks = new Block[blockNum];
	}

	void init(RMQ.Comperator comperator, int begin, int end) {

	}

	@Override
	public int query(int i, int j) {
	    // TODO Auto-generated method stub
	    return 0;
	}

	private static class Block {

	}

    }

}
