package com.jgalgo;

public interface TSPMetric {

	/**
	 * Calculate the Traveling Salesperson Problem solution for metric world
	 *
	 * @param distances n x n table of distances between each two points. In the
	 *                  metric world every three vertices u,v,w should satisfy
	 *                  {@code d[u,v] + d[v,w] <= d[u,w]}
	 * @return list of the n vertices ordered by the calculated path
	 */
	public int[] calcTSP(double[][] distances);

	public static void checkArgDistanceTableSymmetric(double[][] distances) {
		int n = distances.length;
		for (int u = 0; u < n; u++)
			if (distances[u].length != n)
				throw new IllegalArgumentException("Distances table is not full");
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				if (distances[u][v] != distances[v][u])
					throw new IllegalArgumentException("Distance is not symmetric: (" + u + ", " + v + ") "
							+ distances[u][v] + " != " + distances[v][u]);
	}

	public static void checkArgDistanceTableIsMetric(double[][] distances) {
		final double eps = 0.001;
		int n = distances.length;
		for (int u = 0; u < n; u++)
			if (distances[u].length != n)
				throw new IllegalArgumentException("Distances table is not full");
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				for (int w = v + 1; w < n; w++)
					if (distances[u][v] + distances[v][w] + eps < distances[u][w])
						throw new IllegalArgumentException("Distance table is not metric: (" + u + ", " + v + ", " + w
								+ ") " + distances[u][v] + " + " + distances[v][w] + " < " + distances[u][w]);
	}

}
