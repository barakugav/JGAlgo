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

import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IWeightsByte;
import com.jgalgo.graph.IWeightsChar;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsFloat;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IWeightsObj;
import com.jgalgo.graph.IWeightsShort;

interface WeightsStringer {

	String getWeightAsString(int id);

	static WeightsStringer newInstance(IWeights<?> weights) {
		return newInstance(weights, null, null);
	}

	static WeightsStringer newInstance(IWeights<?> weights, String nonNumberPrefix, String nonNumberSuffix) {
		boolean nonNumberEnclosing = false;
		if (nonNumberPrefix != null || nonNumberSuffix != null) {
			if (nonNumberPrefix == null)
				nonNumberPrefix = "";
			if (nonNumberSuffix == null)
				nonNumberSuffix = "";
			nonNumberEnclosing = true;
		}

		if (weights instanceof IWeightsByte) {
			IWeightsByte ws = (IWeightsByte) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsShort) {
			IWeightsShort ws = (IWeightsShort) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsInt) {
			IWeightsInt ws = (IWeightsInt) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsLong) {
			IWeightsLong ws = (IWeightsLong) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsFloat) {
			IWeightsFloat ws = (IWeightsFloat) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsDouble) {
			IWeightsDouble ws = (IWeightsDouble) weights;
			return elm -> String.valueOf(ws.get(elm));

		} else if (weights instanceof IWeightsBool) {
			IWeightsBool ws = (IWeightsBool) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof IWeightsChar) {
			IWeightsChar ws = (IWeightsChar) weights;
			if (nonNumberEnclosing) {
				String pre = nonNumberPrefix, post = nonNumberSuffix;
				return elm -> pre + String.valueOf(ws.get(elm)) + post;
			} else {
				return elm -> String.valueOf(ws.get(elm));
			}

		} else if (weights instanceof IWeightsObj) {
			IWeightsObj<?> ws = (IWeightsObj<?>) weights;
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
