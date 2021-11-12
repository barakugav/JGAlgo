package com.ugav.algo;

class Utils {

    private Utils() {
	throw new InternalError();
    }

    private static final double LOG2 = Math.log(2);
    private static final double LOG2_INV = 1 / LOG2;

    static double log2(double x) {
	return Math.log(x) * LOG2_INV;
    }

}
