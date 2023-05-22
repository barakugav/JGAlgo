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
	public int colorOf(int vertex) {
		return colors[vertex];
	}

}
