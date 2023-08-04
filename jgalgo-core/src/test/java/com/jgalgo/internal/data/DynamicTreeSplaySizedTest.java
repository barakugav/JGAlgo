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

package com.jgalgo.internal.data;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.data.DynamicTreeSplayTest.Op;
import com.jgalgo.internal.util.TestBase;

public class DynamicTreeSplaySizedTest extends TestBase {

	@Test
	public void testRandOps() {
		final long seed = 0x5ec72b4b420cd8d4L;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		var holder = new Object() {
			DynamicTreeSplayExtension.TreeSize treeSizeExt;
		};
		DynamicTreeSplayTest.testRandOps(maxWeight -> {
			holder.treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
			return new DynamicTreeSplayExtended(maxWeight, List.of(holder.treeSizeExt));
		}, ops, node -> holder.treeSizeExt.getTreeSize(node), seed);
	}

	@Test
	public void testRandOpsInt() {
		final long seed = 0x9efac04f7e9404cdL;
		List<Op> ops = List.of(Op.MakeTree, Op.FindRoot, Op.FindMinEdge, Op.AddWeight, Op.Link, Op.Cut, Op.Size);
		var holder = new Object() {
			DynamicTreeSplayExtension.TreeSize treeSizeExt;
		};
		DynamicTreeSplayTest.testRandOps(maxWeight -> {
			holder.treeSizeExt = new DynamicTreeSplayExtension.TreeSize();
			return new DynamicTreeSplayIntExtended((int) maxWeight, List.of(holder.treeSizeExt));
		}, ops, node -> holder.treeSizeExt.getTreeSize(node), seed);
	}
}
