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
package com.jgalgo.io;

import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;

interface WeightsStringer {

	String getWeightAsString(int id);

	static WeightsStringer newInstance(Weights<?> weights) {
		return newInstance(weights, null, null);
	}

	static WeightsStringer newInstance(Weights<?> weights, String nonNumberPrefix, String nonNumberSuffix) {
		boolean nonNumberEnclosing = false;
		if (nonNumberPrefix != null || nonNumberSuffix != null) {
			if (nonNumberPrefix == null)
				nonNumberPrefix = "";
			if (nonNumberSuffix == null)
				nonNumberSuffix = "";
			nonNumberEnclosing = true;
		}

		if (weights instanceof WeightsByte) {
			WeightsByte ws = (WeightsByte) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsShort) {
			WeightsShort ws = (WeightsShort) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsInt) {
			WeightsInt ws = (WeightsInt) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsLong) {
			WeightsLong ws = (WeightsLong) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsFloat) {
			WeightsFloat ws = (WeightsFloat) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsDouble) {
			WeightsDouble ws = (WeightsDouble) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof WeightsBool) {
			WeightsBool ws = (WeightsBool) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof WeightsChar) {
			WeightsChar ws = (WeightsChar) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof WeightsObj) {
			WeightsObj<?> ws = (WeightsObj<?>) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else {
			throw new IllegalArgumentException("Unsupported weights type: " + weights.getClass());
		}
	}

}
