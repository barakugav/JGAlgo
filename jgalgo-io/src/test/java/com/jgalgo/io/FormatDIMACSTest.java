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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Weights;

public class FormatDIMACSTest {

	@Test
	public void parseDimacsEdgeGraph() {
		String data = "";
		data += "c this is the graph with vertices {1,2,3,4,5} and edges {(1,2),(2,3),(2,4),(3,4),(4,5)}\n";
		data += "p edge 5 5\n";
		data += "e 1 2\n";
		data += "e 2 3\n";
		data += "e 2 4\n";
		data += "e 3 4\n";
		data += "e 4 5\n";
		final Graph g = GraphReader.newInstance("dimacs").readGraph(new StringReader(data));
	
		final StringWriter writer = new StringWriter();
		GraphWriter.newInstance("dimacs").writeGraph(g, writer);
		String data2 = writer.toString();

		String check_data2 = "";
		check_data2 += "c DIMACS written graph by JGAlgo\n";
		check_data2 += "p edge 5 5\n";
		check_data2 += "e 1 2\n";
		check_data2 += "e 2 3\n";
		check_data2 += "e 2 4\n";
		check_data2 += "e 3 4\n";
		check_data2 += "e 4 5\n";
		check_data2 = check_data2.replace("\n", System.lineSeparator());
		String check_data3 = "";
		check_data3 += "c DIMACS written graph by JGAlgo\n";
		check_data3 += "p edge 5 5\n";
		check_data3 += "e 1 2\n";
		check_data3 += "e 2 4\n";
		check_data3 += "e 2 3\n";
		check_data3 += "e 3 4\n";
		check_data3 += "e 4 5\n";
		check_data3 = check_data3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(check_data2.trim()))
			assertEquals(data2.trim(), check_data2.trim());
		else if (data2.trim().equals(check_data3.trim()))
			assertEquals(data2.trim(), check_data3.trim());
		else
			assertEquals(data2.trim(), check_data2.trim());
	}

	@Test
	public void parseDimacsSpGraph() {
		String data = "";
		data += "c this is the graph\n";
		data += "p sp 5 5\n";
		data += "e 1 2 3\n";
		data += "e 2 3 9\n";
		data += "e 2 4 15\n";
		data += "e 3 4 2\n";
		data += "e 4 5 7\n";
		final Graph g = GraphReader.newInstance("dimacs").readGraph(new StringReader(data));

		final StringWriter writer = new StringWriter();
		GraphWriter.newInstance("dimacs").writeGraph(g, writer);
		String data2 = writer.toString();

		String check_data2 = "";
		check_data2 += "c DIMACS written graph by JGAlgo\n";
		check_data2 += "p sp 5 5\n";
		check_data2 += "e 1 2 3\n";
		check_data2 += "e 2 3 9\n";
		check_data2 += "e 2 4 15\n";
		check_data2 += "e 3 4 2\n";
		check_data2 += "e 4 5 7\n";
		check_data2 = check_data2.replace("\n", System.lineSeparator());
		String check_data3 = "";
		check_data3 += "c DIMACS written graph by JGAlgo\n";
		check_data3 += "p sp 5 5\n";
		check_data3 += "e 1 2 3\n";
		check_data3 += "e 2 4 15\n";
		check_data3 += "e 2 3 9\n";
		check_data3 += "e 3 4 2\n";
		check_data3 += "e 4 5 7\n";
		check_data3 = check_data3.replace("\n", System.lineSeparator());

		if (data2.trim().equals(check_data2.trim()))
			assertEquals(data2.trim(), check_data2.trim());
		else if (data2.trim().equals(check_data3.trim()))
			assertEquals(data2.trim(), check_data3.trim());
		else
			assertEquals(data2.trim(), check_data2.trim());
	}

}
