package com.ugav.jgalgo;

import java.util.Arrays;

class ColoringResultImpl implements Coloring.Result {

	int colorsNum;
	final int[] colors;

	ColoringResultImpl(Graph g) {
		colors = new int[g.vertices().size()];
		Arrays.fill(colors, -1);
	}

	@Override
	public int colorsNum() {
		return colorsNum;
	}

	@Override
	public int colorOf(int v) {
		return colors[v];
	}

}
