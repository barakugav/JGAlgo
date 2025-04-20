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
package com.jgalgo.alg.planar;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlanarityTesterBoyerMyrvold extends PlanarityTesterAbstract {

    @Override
    protected PlanarityTester.Result<Integer, Integer> findPlanarEmbedding(IndexGraph g, boolean addAsEdgesWeights) {
        Assertions.onlyUndirected(g);
        Assertions.noParallelEdges(g, "graphs with parallel edges are not supported");
        Assertions.noSelfEdges(g, "graphs with self edges are not supported");

        Worker2 worker = new Worker2(g);
        var planarityResult = worker.computePlanarity();

        if (planarityResult.isOk()) {
            return Result.planar(() -> {
                for (Worker.Node dfsTreeRoot : worker.dfsTreeRoots)
                    Worker.cleanUpDfs(dfsTreeRoot);

                IWeightsLong weights;
                Long defaultWeight = Long.valueOf(IntPair.of(-1, -1));
                if (addAsEdgesWeights) {
                    weights = g.addEdgesWeights(PlanarEmbedding.EdgeOrderWeightKey, long.class, defaultWeight);
                } else {
                    weights = Weights.createExternalEdgesWeights(g, long.class, defaultWeight);
                }

                for (Worker.Node node : worker.nodes) {
                    for (Worker.Node child : node.separatedDfsChildList) {
                        Worker.Node virtualRoot = child.initialComponentRoot;
                        node.embedded.append(virtualRoot.embedded);
                    }

                    for (var it = node.embedded.listIterator(); it.hasNext();) {
                        int i = it.nextIndex();
                        int edge = it.next().graphEdge();
                        long x = weights.get(edge);
                        if (g.edgeSource(edge) == node.graphVertex)
                            x = IntPair.setFirst(x, i);
                        if (g.edgeTarget(edge) == node.graphVertex)
                            x = IntPair.setSecond(x, i);
                        weights.set(edge, x);
                    }
                }

                return new PlanarEmbeddings.IndexEmbedding(g, weights);
            });
        } else {
            return Result.nonPlanar(() -> {
                // Clean up the dfs tree
                for (Worker.Node dfsTreeRoot : worker.dfsTreeRoots)
                    Worker.cleanUpDfs(dfsTreeRoot);
                for (Worker.Node bicompRoot : worker.virtualBicompRoots) {
                    if (bicompRoot.outerFaceNeighbor1 == null)
                        continue; // merged with parent bicomp
                    bicompRoot.removeShortCircuitEdges();
                    Worker2.fixBoundaryOrder(bicompRoot);
                }

                return Optional.of(worker.getKuratowskiSubdivision(planarityResult.unwrapError()));
            });
        }
    }

    private static final boolean Side1 = false;
    private static final boolean Side2 = true;

    private static boolean[] sides() {
        return new boolean[] { Side1, Side2 };
    }

    private static class Worker {
        /**
         * The graph we're testing planarity of
         */
        final IndexGraph g;
        /**
         * The number of vertices in the {@code graph}
         */
        private final int n;
        /**
         * List of the vertices of the {@code graph} in their internal representation. After the
         * orientation of the {@code graph} and edge sorting, nodes in this list are sorted according to
         * their dfs indexes
         */
        final Node[] nodes;
        /**
         * List of the dfs tree roots of the {@code graph}. This list has length more than 1 if the
         * input {@code graph} isn't connected
         */
        final List<Node> dfsTreeRoots;
        /**
         * List of the virtual biconnected component roots. Initially, a virtual biconnected component
         * root is created for every node in the {@code graph}, except for the dfs roots. These
         * component roots don't belong to the {@code graph}. At each step of the algorithm, every
         * biconnected component has its own unique component root.
         */
        final List<Node> virtualBicompRoots;
        /**
         * The stack containing merge information for every consecutive pair of biconnected components
         * on the path to the back edge source. After all the biconnected components are merged, this
         * stack is cleared
         */
        final Stack<MergeInfo> mergeStack;

        /**
         * Creates new instance of the planarity testing algorithm for the {@code graph}. The input
         * graph can't be null.
         *
         * @param graph the graph to test the planarity of
         */
        public Worker(IndexGraph graph) {
            this.g = Objects.requireNonNull(graph, "Graph can't be null");
            this.n = graph.vertices().size();
            this.nodes = new Node[n];
            this.dfsTreeRoots = new ObjectArrayList<>();
            this.virtualBicompRoots = new ObjectArrayList<>(n);
            this.mergeStack = new ObjectArrayList<>();
        }

        /**
         * Creates a new node by converting the {@code graphVertex} to the internal node representation.
         *
         * @param  graphVertex the vertex of the {@code graph} we're processing
         * @param  edge        the parent edge of the {@code graphVertex}, is {@code -1} for dfs tree roots
         * @param  parent      the parent node of the {@code graphVertex}
         * @param  dfsIndex    the dfs index of the {@code graphVertex}
         * @return             the newly created node
         */
        private Node createNewNode(int graphVertex, int edge, Node parent, int dfsIndex) {
            Node node;
            if (parent == null) {
                node = Node.newDfsRoot(graphVertex, dfsIndex);
                node.outerFaceNeighbor1 = node.outerFaceNeighbor2 = node;
                dfsTreeRoots.add(node);
            } else {
                assert edge >= 0;
                Edge treeEdge = Edge.newTreeEdge(edge, parent);
                Node componentRoot = Node.newVirtualBicompRoot(parent.dfsIndex, treeEdge);
                node = Node.newInstance(graphVertex, dfsIndex, parent.height + 1, componentRoot, treeEdge);
                treeEdge.target = node;

                virtualBicompRoots.add(componentRoot);

                parent.treeEdges.add(treeEdge);

                node.outerFaceNeighbor1 = node.outerFaceNeighbor2 = componentRoot;
                componentRoot.outerFaceNeighbor1 = componentRoot.outerFaceNeighbor2 = node;
            }
            return node;
        }

        /**
         * Iteratively start an orienting dfs from every {@code graph} vertex that hasn't been visited
         * yet. After orienting the graph, sorts the nodes by their lowpoints and adds them to the
         * {@code separatedDfsChildList}
         */
        private void preprocess() {
            Node[] v2node = new Node[n];

            int[] parent = new int[n];
            Arrays.fill(parent, -1);

            Stack<IEdgeIter> stack = new ObjectArrayList<>();
            int nextDfsIndex = 0;
            for (final int root : range(n)) {
                if (v2node[root] != null)
                    continue;

                v2node[root] = createNewNode(root, -1, null, nextDfsIndex);
                nextDfsIndex++;
                stack.push(g.outEdges(root).iterator());

                dfs: for (int u = root;;) {
                    final Node current = v2node[u];
                    for (IEdgeIter eit = stack.top(); eit.hasNext();) {
                        int e = eit.nextInt();
                        int v = eit.targetInt();
                        // if (dfsIndex[v] < 0) {
                        if (v2node[v] == null) {
                            // Unvisited vertex, e is a tree edge
                            parent[v] = u;

                            v2node[v] = createNewNode(v, e, current, nextDfsIndex);
                            nextDfsIndex++;
                            stack.push(g.outEdges(v).iterator());

                            u = v;
                            continue dfs;
                        } else {
                            // Visited vertex, e is a back edge
                            if (v == parent[u])
                                continue;
                            Node oppositeNode = v2node[v];
                            if (current.dfsIndex < oppositeNode.dfsIndex)
                                continue; // the reverse edge was already added as a back edge, down edge of current
                            Edge backEdge = Edge.newBackEdge(e, current, oppositeNode);
                            oppositeNode.downEdges.add(backEdge);
                            current.backEdges.add(backEdge);
                        }
                    }

                    current.leastAncestor = current.backEdges
                            .stream()
                            .mapToInt(backEdge -> backEdge.target.dfsIndex)
                            .min()
                            .orElse(current.dfsIndex);
                    current.lowpoint = current.treeEdges
                            .stream()
                            .mapToInt(treeEdge -> treeEdge.target.lowpoint)
                            .min()
                            .orElse(current.dfsIndex);
                    current.lowpoint = Math.min(current.lowpoint, current.leastAncestor);

                    // backtrack DFS one vertex up
                    stack.pop();
                    if (stack.isEmpty())
                        break;
                    int v = u;
                    u = parent[v];
                }
            }
            assert Arrays.stream(v2node).allMatch(Objects::nonNull);

            // Sort by DFS index
            for (Node node : v2node)
                nodes[node.dfsIndex] = node;

            // Sort with (stable) bucket sort by lowpoint
            int[] lowpointOffsets = new int[n];
            for (Node node : nodes)
                lowpointOffsets[node.lowpoint]++;
            for (int s = 0, i = 0; i < lowpointOffsets.length; i++) {
                int count = lowpointOffsets[i];
                lowpointOffsets[i] = s;
                s += count;
            }
            Node[] sorted = v2node; // reuse array
            for (Node node : nodes)
                sorted[lowpointOffsets[node.lowpoint]++] = node;
            for (int i : range(n)) {
                Node node = nodes[i] = sorted[i];
                if (node.parentEdge != null) {
                    Node parentNode = node.parentEdge.source;
                    node.listNode = parentNode.separatedDfsChildList.addElementLast(node);
                }
            }
        }

        /**
         * Lazily tests the planarity of the graph. The implementation below is close to the code
         * presented in the original paper
         *
         * @return true if the graph is planar, false otherwise
         */
        com.jgalgo.internal.util.Result<Void, Node> computePlanarity() {
            preprocess();
            for (int currentNode = n - 1; currentNode >= 0; currentNode--) {
                Node current = nodes[currentNode];
                for (Edge downEdge : current.downEdges)
                    walkUp(downEdge.source, current, downEdge);
                for (Edge treeEdge : current.treeEdges) {
                    assert treeEdge.target.initialComponentRoot.dfsIndex == current.dfsIndex;
                    walkDown(treeEdge.target.initialComponentRoot);
                }

                for (Edge downEdge : current.downEdges)
                    if (!downEdge.embedded)
                        return com.jgalgo.internal.util.Result.error(current);
            }
            return com.jgalgo.internal.util.Result.ok(null);
        }

        /**
         * Merges the last two biconnected components using the info stored on top of the stack. The key
         * goal of this method is to merge the outer faces of the two components and to merge the
         * embedded edges of the child component root with the embedded edges of the component parent
         * node.
         */
        private void mergeBiconnectedComponent() {
            MergeInfo info = mergeStack.pop();
            Node virtualRoot = info.child;
            if (info.isInverted()) {
                Node t = virtualRoot.outerFaceNeighbor1;
                virtualRoot.outerFaceNeighbor1 = virtualRoot.outerFaceNeighbor2;
                virtualRoot.outerFaceNeighbor2 = t;
            }
            Node root = info.parent;
            Node virtualRootChild = virtualRoot.parentEdge.target;

            root.pertinentRoots.removeNode(virtualRoot.listNode);
            root.separatedDfsChildList.removeNode(virtualRootChild.listNode);

            root
                    .mergeChildEdges(virtualRoot.embedded, info.vIn, info.vOut, info.parentNext,
                            virtualRoot.parentEdge);

            if (root.outerFaceNeighbor1 == info.parentNext) {
                root.outerFaceNeighbor2 = info.childPrev;
            } else {
                assert root.outerFaceNeighbor2 == info.parentNext;
                root.outerFaceNeighbor1 = info.childPrev;
            }
            info.childPrev.substitute(virtualRoot, root);
            virtualRoot.outerFaceNeighbor1 = virtualRoot.outerFaceNeighbor2 = null;
        }

        /**
         * Embeds the back edge {@code edge} into the list of embedded edges of the source and the
         * virtual target of the edge such that the {@code childPrev} belongs to the new inner face.
         * This method also takes care of modifying the boundary of the outer face accordingly
         *
         * @param  root      the component root
         * @param  entryDir  the component entry direction
         * @param  edge      the edge to embed
         * @param  childPrev the neighbor of the source of the edge that should belong to the inner face
         * @return           a circulator starting from the edge's source
         */
        private OuterFaceIter embedBackEdge(Node root, boolean entryDir, Edge edge, Node childPrev) {
            assert !edge.embedded;
            if (entryDir == Side1) {
                root.embedded.addLast(edge);
            } else {
                root.embedded.addFirst(edge);
            }
            Node child = edge.source;
            child.embedBackEdge(edge, childPrev);
            child.edgeToEmbed = null;
            child.backEdgeFlag = n;
            edge.embedded = true;

            child.substitute(childPrev, root);
            if (entryDir == Side1) {
                root.outerFaceNeighbor1 = child;
            } else {
                root.outerFaceNeighbor2 = child;
            }
            Node next = child.nextOnOuterFace(root);
            return new OuterFaceIter(next, child);
        }

        /**
         * Embeds a short-circuit edge from the {@code componentRoot} to the current node of the
         * {@code circulator}. Changes the outer face accordingly
         *
         * @param componentRoot the component root
         * @param entryDir      the direction used to enter the component
         * @param circulator    a circulator to the source of the new edge
         */
        private static void embedShortCircuit(Node componentRoot, boolean entryDir, OuterFaceIter circulator) {
            Node current = circulator.getCurrent(), prev = circulator.getPrev();
            Edge shortCircuit = Edge.ofShortCircuit(current, componentRoot.getParent());
            if (entryDir == Side1) {
                componentRoot.embedded.addLast(shortCircuit);
                componentRoot.outerFaceNeighbor1 = current;
            } else {
                componentRoot.embedded.addFirst(shortCircuit);
                componentRoot.outerFaceNeighbor2 = current;
            }
            current.embedBackEdge(shortCircuit, prev);
            current.substitute(prev, componentRoot);
        }

        /**
         * The walkdown procedure from the original paper. Either embeds all of the back edges in the
         * subtree rooted at the child of the {@code componentRoot} or identifies the back edges which
         * can be used to extract a Kuratowski subdivision. Iteratively traverses the tree of the
         * biconnected component and descends only to the pertinent components. This procedure is also
         * responsible for embedding short-circuit edges to make the algorithm run in linear time in the
         * worst case.
         *
         * @param componentRoot the root of the component to start the walkdown from
         */
        private void walkDown(final Node componentRoot) {
            for (boolean componentEntryDir : sides()) {
                if (!mergeStack.isEmpty())
                    break;
                boolean currentComponentEntryDir = componentEntryDir;
                OuterFaceIter circulator = componentRoot.iterator(currentComponentEntryDir);
                for (Node current = circulator.next(); current != componentRoot;) {
                    if (current.hasBackEdgeWrtTo(componentRoot)) {
                        Node childPrev = circulator.getPrev();
                        while (!mergeStack.isEmpty())
                            mergeBiconnectedComponent();
                        circulator = embedBackEdge(componentRoot, componentEntryDir, current.edgeToEmbed, childPrev);
                    }
                    if (!current.pertinentRoots.isEmpty()) {
                        boolean parentComponentEntryDir = currentComponentEntryDir;
                        final Node root = current.pertinentRoots.getFirst();

                        OuterFaceIter ccwCirculator = getActiveSuccessorOnOuterFace(root, componentRoot, Side1);
                        Node ccwActiveNode = ccwCirculator.getCurrent();
                        OuterFaceIter cwCirculator = getActiveSuccessorOnOuterFace(root, componentRoot, Side2);
                        Node cwActiveNode = cwCirculator.getCurrent();

                        if (ccwActiveNode.isInternallyActiveWrtTo(componentRoot)) {
                            currentComponentEntryDir = Side1;
                        } else if (cwActiveNode.isInternallyActiveWrtTo(componentRoot)) {
                            currentComponentEntryDir = Side2;
                        } else if (ccwActiveNode.isPertinentWrtTo(componentRoot)) {
                            currentComponentEntryDir = Side1;
                        } else {
                            currentComponentEntryDir = Side2;
                        }

                        if (currentComponentEntryDir == Side1) {
                            mergeStack
                                    .push(new MergeInfo(
                                            current, circulator.peekNext(), root, root.outerFaceNeighbor2,
                                            parentComponentEntryDir, currentComponentEntryDir));
                            current = ccwActiveNode;
                            circulator = ccwCirculator;

                            if (!cwActiveNode.hasRootNeighbor())
                                embedShortCircuit(root, Side2, cwCirculator);
                        } else {
                            mergeStack
                                    .push(new MergeInfo(
                                            current, circulator.peekNext(), root, root.outerFaceNeighbor1,
                                            parentComponentEntryDir, currentComponentEntryDir));
                            current = cwActiveNode;
                            circulator = cwCirculator;

                            if (!ccwActiveNode.hasRootNeighbor())
                                embedShortCircuit(root, Side1, ccwCirculator);
                        }

                    } else if (!current.isActiveWrtTo(componentRoot)) {
                        current = circulator.next();
                    } else {
                        // current vertex is externally active
                        if (!current.hasRootNeighbor() && mergeStack.isEmpty())
                            embedShortCircuit(componentRoot, componentEntryDir, circulator);
                        break;
                    }
                }
            }
        }

        /**
         * The walkup procedure from the original paper. Identifies the pertinent subgraph of the graph
         * by going up the dfs tree from the {@code start} node to the {@code end} node using the edge
         * {@code edge}
         *
         * @param start the node to start the walkup from
         * @param end   the node currently processed by the main loop of the algorithm
         * @param edge  a back edge to embed
         */
        private static void walkUp(Node start, Node end, Edge edge) {
            final int visited = end.dfsIndex;

            start.backEdgeFlag = visited;
            start.edgeToEmbed = edge;
            start.visited = visited;

            for (OuterFaceIter xIter = start.iterator(Side1), yIter = start.iterator(Side2);;) {
                Node x = xIter.next(), y = yIter.next();
                if (x.isVisitedWrtTo(end) || y.isVisitedWrtTo(end))
                    break;
                x.visited = y.visited = visited;

                if (x.isBicompRoot() || y.isBicompRoot()) {
                    Node root = x.isBicompRoot() ? x : y;
                    assert root.isBicompRoot();

                    Node rootChild = root.parentEdge.target;
                    Node newStart = root.parentEdge.source;
                    if (newStart == end)
                        break;
                    if (rootChild.lowpoint < end.dfsIndex) {
                        // the component in externally active
                        root.listNode = newStart.pertinentRoots.addElementLast(root);
                    } else {
                        // the component is internally active
                        root.listNode = newStart.pertinentRoots.addElementFirst(root);
                    }
                    newStart.visited = visited;

                    xIter = newStart.iterator(Side1);
                    yIter = newStart.iterator(Side2);
                }
            }
        }

        /**
         * Either finds and returns a circulator to the node on the boundary of the component, which
         * satisfies the {@code predicate} or returns a circulator to the {@code stop} node.
         *
         * @param  predicate the condition the desired node should satisfy
         * @param  start     the node to start the search from
         * @param  stop      the node to end the search with
         * @param  dir       the direction to start the traversal in
         * @return           a circulator to the node satisfying the {@code predicate} or to the {@code stop} node
         */
        static OuterFaceIter selectOnOuterFace(Predicate<Node> predicate, Node start, Node stop,
                boolean dir) {
            OuterFaceIter circulator = start.iterator(dir);
            for (Node current = circulator.next(); current != stop && !predicate.test(current);)
                current = circulator.next();
            return circulator;
        }

        /**
         * Returns an active node on the outer face in the direction {@code dir} starting from the
         * {@code start} node
         *
         * @param  start the node to start the search from
         * @param  v     an ancestor of the {@code start}
         * @param  dir   the direction of the search
         * @return       a circulator to the found node
         */
        static OuterFaceIter getActiveSuccessorOnOuterFace(Node start, Node v, boolean dir) {
            return selectOnOuterFace(n -> n.isActiveWrtTo(v), start, start, dir);
        }

        /**
         * Recursively cleans up the dfs tree rooted at the {@code dfsTreeRoot} my removing all the
         * short-circuit edges and properly orienting other embedded edges
         *
         * @param dfsTreeRoot the root of the dfs tree to clean up
         */
        static void cleanUpDfs(Node dfsTreeRoot) {
            Stack<Pair<Node, Boolean>> stack = new ObjectArrayList<>();
            stack.push(Pair.of(dfsTreeRoot, Boolean.FALSE));
            while (!stack.isEmpty()) {
                Pair<Node, Boolean> entry = stack.pop();
                Node current = entry.first();
                boolean isInverted = entry.second().booleanValue();
                if (isInverted)
                    current.embedded.invert();
                current.removeShortCircuitEdges();
                for (Node node : current.separatedDfsChildList) {
                    // all the components, that aren't merged, aren't inverted
                    node.parentEdge.isInverted = isInverted;
                }

                for (Edge treeEdge : current.treeEdges)
                    stack.push(Pair.of(treeEdge.target, Boolean.valueOf(isInverted ^ treeEdge.isInverted)));
            }

        }

        /**
         * The information needed to merge two consecutive biconnected components
         */
        static class MergeInfo {
            /**
             * The node current traversal descended from. This node belongs to the parent biconnected
             * component
             */
            final Node parent;
            /**
             * The next node along the traversal of the parent biconnected component
             */
            final Node parentNext;
            /**
             * The virtual root of the child biconnected component
             */
            final Node child;
            /**
             * The previous node along the traversal of the child biconnected component
             */
            final Node childPrev;
            /**
             * The direction used to enter the parent biconnected component.
             * <p>
             * <b>Note:</b> this value doesn't specify the direction from {@code parent} node to the
             * {@code parentNext} node, i.e. {@code parent.outerFaceNeighbors[vIn]} may not be equal to
             * the {@code parentNext}. Instead, this value specifies the direction used to start the
             * traversal from the parent's biconnected component virtual root.
             */
            final boolean vIn;
            /**
             * The direction used to start the traversal of the child biconnected component. Since the
             * {@code child} is the component root, {@code child.outerFaceNeighbors[|1-vOut|]} is equal
             * to the {@code childPrev}
             */
            final boolean vOut;

            /**
             * Creates new instance of the information needed to merge to biconnected components
             *
             * @param parent     the node current traversal descended from
             * @param parentNext the next node along the traversal of the parent component
             * @param child      the virtual root of the child biconnected component
             * @param childPrev  the previous node along the traversal of the child component
             * @param vIn        the direction used to enter the parent biconnected component
             * @param vOut       the direction used to enter the child biconnected component
             */
            MergeInfo(Node parent, Node parentNext, Node child, Node childPrev, boolean vIn, boolean vOut) {
                this.parent = parent;
                this.parentNext = parentNext;
                this.child = child;
                this.childPrev = childPrev;
                this.vIn = vIn;
                this.vOut = vOut;
            }

            /**
             * Returns true if the traversal was inverted when descending to the child biconnected
             * component, false otherwise
             *
             * @return true if the traversal was inverted when descending to the child biconnected
             *         component, false otherwise
             */
            boolean isInverted() {
                return vIn != vOut;
            }

        }

        /**
         * A circulator over the nodes on the boundary of the biconnected component. Traverses the nodes
         * in the cyclic manner, i.e. it doesn't stop when all the nodes are traversed
         */
        static class OuterFaceIter implements Iterator<Node> {
            /**
             * The node this circulator will return next
             */
            private Node current;
            /**
             * The previous node along the traversal of the component boundary. This node is needed
             * because the component boundary nodes aren't connected in an ordered way.
             */
            private Node prev;

            /**
             * Creates a new instance of the circulator over the biconnected component boundary nodes.
             * The {@code prev} node is considered to be just traversed
             *
             * @param current the node this circulator will return next
             * @param prev    the previous node along the traversal
             */
            OuterFaceIter(Node current, Node prev) {
                this.current = current;
                this.prev = prev;
            }

            /**
             * {@inheritDoc}
             * <p>
             * Always returns true since this is a circulator
             */
            @Override
            public boolean hasNext() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Node next() {
                Node t = current;
                current = current.nextOnOuterFace(prev);
                return prev = t;
            }

            Node peekNext() {
                return current;
            }

            /**
             * Returns an edge connecting previously returned node with node, which will be returned
             * next. If either of the mentioned nodes is virtual, the edge will be incident to its real
             * counterpart.
             *
             * @return an edge from the current node to the next node
             */
            Edge edgeToNext() {
                Edge edge = prev.embedded.getFirst();
                Node target = toExistingNode(current);
                Node source = toExistingNode(prev);
                if (edge.getOpposite(source) == target) {
                    return edge;
                } else {
                    return prev.embedded.getLast();
                }
            }

            /**
             * Returns the node this circulator has just traversed
             *
             * @return the node this circulator has just traversed
             */
            Node getCurrent() {
                return prev;
            }

            /**
             * Returns a node adjacent to the current node along the boundary, which is not equal to the
             * next node along the traversal. If the component consist of just one real node, returns
             * the only neighbor the the current node.
             *
             * @return node before the current node along the traversal
             */
            Node getPrev() {
                return prev.nextOnOuterFace(current);
            }

            /**
             * Returns either {@code node} or its real counterpart in the case the {@code node} is a
             * component root.
             *
             * @param  node the input argument
             * @return      the real counter path of the {@code node}
             */
            private static Node toExistingNode(Node node) {
                return node.isBicompRoot() ? node.getParent() : node;
            }
        }

        /**
         * Internal representation of the edges of the input {@code graph}.
         */
        static class Edge {
            /**
             * The counterpart of this edge in the {@code graph}. This value can be -1 if the edge was
             * created as a short-circuit edge.
             */
            private final int graphEdge;
            /**
             * The source node of this edge. For tree edges the {@code source} is lower than the
             * {@code target}, for back edges the {@code target} is lower (having smaller height)
             */
            final Node source;
            /**
             * The target of this edge
             */
            Node target;
            /**
             * Either $+1$ or $-1$ for regular and inverted edges respectively. This value is set to
             * $-1$ to flip a biconnected component in $O(1)$ time. <b>Note:</b> this
             * operation doesn't flip any of the child biconnected components of this biconnected
             * component
             */
            boolean isInverted;
            /**
             * Whether the edge is embedded or not. This value is important for
             */
            boolean embedded;
            /**
             * Whether the edge is real or short-circuit. See the original paper for the definition of
             * the short-circuit edges.
             */
            final boolean shortCircuit;

            /**
             * Creates a new tree edge.
             *
             * @param graphEdge the counterpart of this edge in the {@code graph}
             * @param source    the source node of this edge
             */
            static Edge newTreeEdge(int graphEdge, Node source) {
                assert graphEdge >= 0;
                return new Edge(graphEdge, source, null, false, false);
            }

            /**
             * Creates a new edge. This constructor is used directly for the creation of the back edges
             *
             * @param graphEdge the counterpart of this edge in the {@code graph}
             * @param source    the source node of this edge
             * @param target    the target node of this edge
             */
            static Edge newBackEdge(int graphEdge, Node source, Node target) {
                assert graphEdge >= 0;
                return new Edge(graphEdge, source, target, false, false);
            }

            /**
             * Creates a new short-circuit edge with no counterpart in {@code graph}. The {@code source}
             * of this edge is always a real node on the boundary of some biconnected component, and the
             * {@code target} node is the parent node of the biconnected component the source node
             * belongs to, so the edge resembles a regular back edge except for that it doesn't have a
             * counterpart in the {@code graph}
             *
             * @param source the source of the short-circuit edge
             * @param target the target of the short-circuit edge
             */
            static Edge ofShortCircuit(Node source, Node target) {
                return new Edge(-1, source, target, true, true);
            }

            private Edge(int graphEdge, Node source, Node target, boolean embedded, boolean shortCircuit) {
                this.graphEdge = graphEdge;
                this.source = source;
                this.target = target;
                this.isInverted = false;
                this.embedded = embedded;
                this.shortCircuit = shortCircuit;
            }

            int graphEdge() {
                if (graphEdge < 0)
                    throw new IllegalStateException("This edge is a short-circuit edge");
                return graphEdge;
            }

            /**
             * True if this edge is incident to the node {@code node}, false otherwise
             *
             * @param  node the node to test
             * @return      true if this edge is incident to the node {@code node}, false otherwise
             */
            boolean isIncidentTo(Node node) {
                return source == node || target == node;
            }

            /**
             * Returns the opposite node of the {@code node}
             *
             * @param  node an endpoint of this edge
             * @return      the other endpoint of this edge
             */
            Node getOpposite(Node node) {
                assert isIncidentTo(node); // debug purpose assertion
                return source == node ? target : source;
            }
        }

        /**
         * The internal representation of the vertices of the graph. Contains necessary information to
         * perform dfs traversals, biconnected component boundary traversals, to embed edges, etc.
         */
        static class Node {
            /**
             * The counterpart of this node in the {@code graph}. For the component roots this value is
             * {@code -1}.
             */
            final int graphVertex;
            /**
             * The dfs index of this node in the graph. Every node has a unique dfs index in the range
             * $[0,|V| - 1]$. This value is used to order the nodes for the embedding of the edges
             * incident to them. The index of the first dfs root is $1$
             */
            final int dfsIndex;
            /**
             * The height of the node in the created dfs tree. The root of the tree has height $0$. The
             * smaller this value is, the lower the node is considered to be.
             */
            final int height;
            /**
             * The least <b>dfs index</b> of the nodes' least ancestors in the subtree rooted at this
             * node. If the subtree doesn't contain a node with a back edge, that ends lower that this
             * node, this value is equal to the dfs index of this node.
             */
            int lowpoint;
            /**
             * The least dfs index of the nodes adjacent to this node. If the node doesn't have incident
             * back edges, this value is equal to the dfs index of the node itself.
             */
            int leastAncestor;
            /**
             * Stores some value to indicate whether this node is visited in the current context or not.
             * During the testing phase, if this value if equal to the dfs index of the currently
             * processed node $v$, then this node is visited, otherwise not. During the Kuratowski
             * subdivision extraction phase, the value of $0$ indicates that the node isn't visited,
             * otherwise, the node is considered to be visited.
             */
            int visited;
            /**
             * During the process of embedding of the down edges of the node $v$, this variable is set
             * to the dfs index of $v$ to indicate that this node has a back edge incident to $v$, which
             * needs to be embedded.
             */
            int backEdgeFlag;
            /**
             * The height of this node on the boundary of the biconnected component, which is used to
             * extract the Kuratowski subdivision. The height of the component root is $0$, the heights
             * on the left side are positive, on the right side - negative. This value is used to
             * quickly determine for two nodes on the same boundary branch which one is higher (closer
             * to the component root).
             */
            int boundaryHeight;
            /**
             * Used to mark the boundary nodes of the biconnected
             */
            boolean marked;
            /**
             * Edge to the parent node of this node in the dfs tree. For tree roots this value is
             * {@code null}
             */
            final Edge parentEdge;
            /**
             * If this node has a back edge incident to the currently processed node $v$, then this
             * variable stores this edge
             */
            Edge edgeToEmbed;
            /**
             * The component root the node is created in. For dfs tree roots this value is {@code null}
             */
            final Node initialComponentRoot;
            /**
             * Two neighbors on the outer face of the biconnected component. Their order is completely
             * irrelevant for the algorithm to traverse the outer face.
             */
            Node outerFaceNeighbor1;
            Node outerFaceNeighbor2;
            /**
             * The list containing the dfs children of this node, which are in the different child
             * biconnected component, i.e. their weren't merged in the parent component.
             */
            final DoublyLinkedList<Node> separatedDfsChildList;
            /**
             * The roots of the pertinent components during the processing of the node $v$. These are
             * the components that have pertinent descendant nodes, i.e. nodes incident to the node $v$
             * via a back edge
             */
            final DoublyLinkedList<Node> pertinentRoots;
            /**
             * The list of tree edges incident to this node in the dfs tree. This list doesn't contain a
             * parent edge of this node
             */
            final List<Edge> treeEdges;
            /**
             * The list containing the edges from descendants of this node in the dfs tree. For each
             * such descendant the corresponding edge is a back edge.
             */
            final List<Edge> downEdges;
            /**
             * The list of back edges incident to this node.
             */
            final List<Edge> backEdges;
            /**
             * Stores the list node from the {@code separatedDfsChildList} of the parent node this node
             * is stored in. This enable deleting of this node from the {@code separatedDfsChildList}
             * list in $O(1)$ time
             */
            DoublyLinkedList.ListNode<Node> listNode;
            /**
             * The list of the embedded edges incident to this node in a clockwise or a counterclockwise
             * order. The order is counterclockwise if the product of the signs of the parent edges
             * along the path from the root of the component this node is contained in to this node is
             * equal to $-1$. Otherwise, the order is clockwise.
             */
            final DoublyLinkedList<Edge> embedded;

            /**
             * Creates a new node with the specified parameters
             *
             * @param graphVertex the vertex in the {@code graph} corresponding to this node
             * @param dfsIndex    the dfs index of this node
             * @param parentEdge  the parent edge of this node
             * @param rootVertex  whether this is real or virtual node
             */
            private Node(int graphVertex, int dfsIndex, Edge parentEdge,
                    Node initialComponentRoot, int height) {
                this.graphVertex = graphVertex;
                this.dfsIndex = dfsIndex;
                this.parentEdge = parentEdge;
                this.initialComponentRoot = initialComponentRoot;
                this.height = height;
                this.embedded = new DoublyLinkedList<>();
                if (parentEdge != null)
                    embedded.add(parentEdge);
                this.visited = this.backEdgeFlag = Integer.MAX_VALUE;
                if (!isBicompRoot()) {
                    separatedDfsChildList = new DoublyLinkedList<>();
                    pertinentRoots = new DoublyLinkedList<>();
                    treeEdges = new ObjectArrayList<>();
                    downEdges = new ObjectArrayList<>();
                    backEdges = new ObjectArrayList<>();
                } else {
                    separatedDfsChildList = null;
                    pertinentRoots = null;
                    treeEdges = null;
                    downEdges = null;
                    backEdges = null;
                }
            }

            static Node newDfsRoot(int graphVertex, int dfsIndex) {
                return new Node(graphVertex, dfsIndex, null, null, 0);
            }

            /**
             * Creates a new real node with the specified parameters.
             *
             * @param graphVertex          the counterpart of this node in the {@code graph}
             * @param dfsIndex             the dfs index of this node
             * @param height               the height of this node in the dfs tree
             * @param initialComponentRoot the component root of this node.
             * @param parentEdge           the parent edge of this node
             */
            static Node newInstance(int graphVertex, int dfsIndex, int height, Node initialComponentRoot,
                    Edge parentEdge) {
                return new Node(graphVertex, dfsIndex, Objects.requireNonNull(parentEdge),
                        Objects.requireNonNull(initialComponentRoot),
                        height);
            }

            /**
             * Creates a new component root. Dfs index of the component root is equal to dfs index of
             * its parent.
             *
             * @param dfsIndex   the dfs index of this node
             * @param parentEdge the parent edge of this component root
             */
            static Node newVirtualBicompRoot(int dfsIndex, Edge parentEdge) {
                return new Node(-1, dfsIndex, Objects.requireNonNull(parentEdge), null, 0);
            }

            final boolean isBicompRoot() {
                return initialComponentRoot == null && parentEdge != null;
            }

            /**
             * Checks whether this node is visited in the context of processing the node {@code node}
             *
             * @param  node the node that is currently been processed
             * @return      true if this node is visited, false otherwise
             */
            boolean isVisitedWrtTo(Node node) {
                return node.dfsIndex == visited;
            }

            /**
             * Checks whether this node is pertinent in the context of processing the node {@code node}.
             * During the processing of the node {@code node}, a node is pertinent if it has a back edge
             * to {@code node} or it has a child biconnected component, which has a pertinent node.
             *
             * @param  node the node that is currently been processed
             * @return      true if this node is pertinent, false otherwise
             */
            boolean isPertinentWrtTo(Node node) {
                return backEdgeFlag == node.dfsIndex || !pertinentRoots.isEmpty();
            }

            /**
             * Checks whether this node has a back edge to the {@code node}.
             *
             * @param  node the other endpoint of the edge we're looking for
             * @return      true if the edge between this node and {@code node} exists, false otherwise
             */
            boolean hasBackEdgeWrtTo(Node node) {
                return backEdgeFlag == node.dfsIndex;
            }

            /**
             * Checks whether this node is externally active with respect to the {@code node}. A node is
             * externally active, if it is incident to the edge ending higher than {@code node}, or it
             * has a child biconnected component with a pertinent node
             *
             * @param  node an ancestor of this node
             * @return      true if this node is externally active with respect to the {@code node}, false
             *              otherwise
             */
            boolean isExternallyActiveWrtTo(Node node) {
                return leastAncestor < node.dfsIndex || (!separatedDfsChildList.isEmpty()
                        && separatedDfsChildList.getFirst().lowpoint < node.dfsIndex);
            }

            /**
             * Check whether this node is internally active. A node is internally active if it's
             * pertinent and not externally active
             *
             * @param  node an ancestor of this node
             * @return      true if this node is internally active, false otherwise
             */
            boolean isInternallyActiveWrtTo(Node node) {
                return isPertinentWrtTo(node) && !isExternallyActiveWrtTo(node);
            }

            /**
             * Check whether this node is active. A node is active it is either pertinent or externally
             * active
             *
             * @param  node an ancestor of this node
             * @return      true if this node is active, false otherwise
             */
            boolean isActiveWrtTo(Node node) {
                return isExternallyActiveWrtTo(node) || isPertinentWrtTo(node);
            }

            /**
             * Returns a circulator, that moves in the direction {@code direction}. The next node along
             * the traversal will be {@code this.outerFaceNeighbor[direction]}.
             *
             * @param  direction the direction to move in
             * @return           an iterator over the boundary nodes in the direction {@code direction}
             */
            OuterFaceIter iterator(boolean direction) {
                Node neighbor = direction == Side1 ? outerFaceNeighbor1 : outerFaceNeighbor2;
                return new OuterFaceIter(neighbor, this);
            }

            void removeShortCircuitEdges() {
                embedded.removeIf(e -> e.shortCircuit);
            }

            /**
             * Returns the parent of this node in the dfs tree. Tree parent of the dfs root node is
             * {@code null}
             *
             * @return the parent of this node in the dfs tree
             */
            Node getParent() {
                return parentEdge == null ? null : parentEdge.source;
            }

            /**
             * Substitutes the neighbor {@code node} with a {@code newNeighbor}
             *
             * @param node        an old neighbor
             * @param newNeighbor a new neighbor
             */
            void substitute(Node node, Node newNeighbor) {
                if (outerFaceNeighbor1 == node) {
                    outerFaceNeighbor1 = newNeighbor;
                } else {
                    assert outerFaceNeighbor2 == node;
                    outerFaceNeighbor2 = newNeighbor;
                }
            }

            /**
             * Checks whether this node has a neighbor, which is a root of a biconnected component
             *
             * @return true, if this node has a root node neighbor, false otherwise
             */
            boolean hasRootNeighbor() {
                return outerFaceNeighbor1.isBicompRoot() || outerFaceNeighbor2.isBicompRoot();
            }

            /**
             * Returns a neighbor of this node which is not equal to the {@code prev}
             *
             * @param  prev a neighbor of this node
             * @return      a neighbor, which is not equal to the {@code prev}
             */
            Node nextOnOuterFace(Node prev) {
                if (outerFaceNeighbor1 == prev) {
                    return outerFaceNeighbor2;
                } else {
                    assert outerFaceNeighbor2 == prev;
                    return outerFaceNeighbor1;
                }
            }

            /**
             * Adds {@code edge} to the list of the embedded edges such that the {@code prev} node
             * becomes an inner node.
             *
             * @param edge an edge to embed
             * @param prev the node which should be on the new inner face
             */
            void embedBackEdge(Edge edge, Node prev) {
                assert !embedded.isEmpty();
                if (prev.isBicompRoot())
                    prev = prev.getParent();
                Edge firstEdge = embedded.getFirst();
                if (firstEdge.getOpposite(this) == prev) {
                    // edge on the new inner face is at the beginning of the list
                    embedded.addFirst(edge);
                } else {
                    embedded.addLast(edge);
                }
            }

            /**
             * Merges the embedded edges of the child component root into this node's embedded edges.
             * Note, that the edges in the {@code edges} list are always in the clockwise order. There
             * are 3 parameters which determine how the {@code edges} list is merged: the {@code vIn}
             * direction, the {@code vOut} direction and the orientation of the edges around this node
             * (clockwise or counterclockwise). The edges in the {@code edges} list should have the same
             * orientation. If this list is inverted, the sign of the {@code parentEdge} is set to $-1$.
             *
             * @param edges      the edges from the child component root
             * @param vIn        the direction used to enter the parent component
             * @param vOut       the direction used to enter the child component
             * @param parentNext the next node along the traversal of the parent biconnected component
             * @param parentEdge the parent edge if the child component
             */
            void mergeChildEdges(
                    DoublyLinkedList<Edge> edges, boolean vIn, boolean vOut, Node parentNext, Edge parentEdge) {
                assert !embedded.isEmpty();
                Node firstOpposite = embedded.getFirst().getOpposite(this);
                boolean alongParentTraversal = firstOpposite != parentNext;
                boolean actionAppend = false, invert = false;
                if (vIn == Side1) {
                    if (vOut == Side1) {
                        if (!alongParentTraversal) {
                            invert = actionAppend = true;
                        }
                    } else {
                        if (alongParentTraversal) {
                            invert = true;
                        } else {
                            actionAppend = true;
                        }
                    }
                } else {
                    if (vOut == Side1) {
                        if (!alongParentTraversal) {
                            invert = actionAppend = true;
                        }
                    } else {
                        if (alongParentTraversal) {
                            invert = true;
                        } else {
                            actionAppend = true;
                        }
                    }
                }
                if (invert) {
                    assert !parentEdge.isInverted;
                    parentEdge.isInverted = true;
                    edges.invert();
                }
                if (actionAppend) {
                    embedded.append(edges);
                } else {
                    embedded.prepend(edges);
                }
            }
        }
    }

    private static class Worker2 extends Worker {

        public Worker2(IndexGraph graph) {
            super(graph);
        }

        /**
         * Returns a circulator to the externally active node on the outer face between the {@code start}
         * and {@code end} nodes in the direction {@code dir}.
         *
         * @param  start the node to start the search from
         * @param  stop  the node to end the search with
         * @param  v     an ancestor of the {@code start} and the {@code end}
         * @param  dir   the direction of the search
         * @return       a circulator to the found node
         */
        private static OuterFaceIter getExternallyActiveSuccessorOnOuterFace(
                Node start, Node stop, Node v, boolean dir) {
            return selectOnOuterFace(n -> n.isExternallyActiveWrtTo(v), start, stop, dir);
        }

        /**
         * Returns a component root of component the {@code node} is contained in.
         *
         * @param  node a node in the partially embedded graph
         * @return      a component root of the component the {@code node} belongs to
         */
        private static Node getComponentRoot(Node node) {
            return selectOnOuterFace(n -> n.isBicompRoot(), node, node, Side1).getCurrent();
        }

        /**
         * Adds the edges on the path from the {@code startEdge} up to the node {@code stop} to the set
         * {@code edges}
         *
         * @param edges     the set to add the path edges to
         * @param startEdge the edge to start from
         * @param stop      the last node on the path
         */
        private static void addPathEdges(Set<Edge> edges, Edge startEdge, Node stop) {
            edges.add(startEdge);
            for (Node current = startEdge.source; current != stop; current = current.getParent())
                edges.add(current.parentEdge);
        }

        /**
         * Adds the edges between the {@code start} and the {@code end} to the set {@code edges}
         *
         * @param edges the set to add the path edges to to
         * @param start the node to start from
         * @param stop  the node to end with
         */
        private static void addPathEdges(Set<Edge> edges, Node start, Node stop) {
            if (start != stop)
                addPathEdges(edges, start.parentEdge, stop);
        }

        /**
         * Searches a back edge which target has a height smaller than {@code heightMax}
         *
         * @param  current   the node to start from
         * @param  heightMax an upper bound on the height of the desired back edge
         * @return           the desired back edge or null, if no such edge exist
         */
        private static Edge findBackEdge(Node current, int heightMax) {
            return findBackEdge(current, heightMax, null);
        }

        /**
         * Searches a back edge which target has a height smaller than {@code heightMax}
         *
         * @param  current       the node to start from
         * @param  heightMax     an upper bound on the height of the desired back edge
         * @param  forbiddenEdge an edge the desired edge should not be equal to
         * @return               the desired back edge or null, if no such edge exist
         */
        private static Edge findBackEdge(Node current, int heightMax, Edge forbiddenEdge) {
            return findBackEdge(current, e -> forbiddenEdge != e && e.target.height < heightMax);
        }

        /**
         * Generically searches an edge in the subtree rooted at the {@code current}, which doesn't
         * include the children of the {@code current} that have been merged to the parent biconnected
         * component.
         *
         * @param  current   the node to start the search from
         * @param  predicate the predicate which the desired edge should satisfy
         * @return           an edge which satisfies the {@code predicate}, or null if such an edge doesn't exist
         */
        private static Edge findBackEdge(Node current, Predicate<Edge> predicate) {
            for (Edge edge : current.backEdges)
                if (predicate.test(edge))
                    return edge;
            for (Node node : current.separatedDfsChildList) {
                Edge result = findBackEdgeSubtreeDfs(node, predicate);
                if (result != null)
                    return result;
            }
            return null;
        }

        /**
         * Recursively searches all the subtree root at the node {@code start} to find an edge
         * satisfying the {@code predicate}.
         *
         * @param  start     the node to start the search from.
         * @param  predicate a predicate, which the desired edge should satisfy
         * @return           a desired edge, or null if no such edge exist.
         */
        private static Edge findBackEdgeSubtreeDfs(Node start, Predicate<Edge> predicate) {
            Stack<Node> stack = new ObjectArrayList<>();
            stack.push(start);
            while (!stack.isEmpty()) {
                Node current = stack.pop();

                for (Edge edge : current.backEdges)
                    if (predicate.test(edge))
                        return edge;
                for (Edge edge : current.treeEdges)
                    stack.push(edge.target);
            }
            return null;
        }

        /**
         * Returns the highest of the two input node, i.e. the node with the greater height
         *
         * @param  a a node in the dfs tree
         * @param  b a node in the dfs tree
         * @return   the highest of the two nodes
         */
        private static Node highest(Node a, Node b) {
            return a.height > b.height ? a : b;
        }

        /**
         * Returns the lowest of the two input node, i.e. the node with the least height.
         *
         * @param  a a node in the dfs tree
         * @param  b a node in the dfs tree
         * @return   the lowest of the two nodes
         */
        private static Node lowest(Node a, Node b) {
            return a.height < b.height ? a : b;
        }

        /**
         * Iteratively sets a boundary height for the nodes on the outer face branch ending at the node
         * {@code w}.
         *
         * @param componentRoot the root of the component
         * @param w             the end of the outer face branch
         * @param dir           the direction to start the traversal in
         * @param delta         a value in $\{+1, -1\}$ to set either positive or negative boundary height
         */
        private static void setBoundaryDepth(Node componentRoot, Node w, boolean dir, int delta) {
            OuterFaceIter circulator = componentRoot.iterator(dir);
            int currentHeight = delta;
            for (Node current = circulator.next(); current != w; current = circulator.next()) {
                current.boundaryHeight = currentHeight;
                currentHeight += delta;
            }
        }

        /**
         * Clears the visited variable of all the nodes and component roots
         */
        private void clearVisited() {
            Arrays.stream(nodes).forEach(n -> n.visited = 0);
            virtualBicompRoots.forEach(n -> n.visited = 0);
        }

        /**
         * Represents information needed to search a path within a biconnected component
         */
        private static class SearchInfo {
            /**
             * The current node of the dfs traversal
             */
            Node current;
            /**
             * The edge used to go to the {@code current} vertex
             */
            Edge prevEdge;
            /**
             * Whether dfs is in a forward or a backtracking phase
             */
            boolean backtrack;

            /**
             * Creates a new search info
             *
             * @param current   the current node of the traversal
             * @param prevEdge  the edge used to go to the {@code current} vertex
             * @param backtrack whether dfs is in a forward or a backtracking phase
             */
            SearchInfo(Node current, Edge prevEdge, boolean backtrack) {
                this.current = current;
                this.prevEdge = prevEdge;
                this.backtrack = backtrack;
            }
        }

        /**
         * Generically searches a path from the {@code current} node to the first node satisfying the
         * {@code isFinish} predicate consisting of all the nodes satisfying the {@code canGo}
         * predicate. The key property of this method is that it searches the next edge on the path in
         * the clockwise order starting from the previous edge. The edges of the resulting path are
         * added to the {@code edges}.
         *
         * @param  start     the start node of the traversal
         * @param  startPrev the previous edge of the start node
         * @param  canGo     specifies where the search can go
         * @param  isFinish  specifies what nodes are finish nodes
         * @param  edges     the list containing the resulting path
         * @return           true if the search was successful, false otherwise
         */
        private static boolean findPathDfs(
                Node start, Edge startPrev, Predicate<Node> canGo, Predicate<Node> isFinish,
                List<Edge> edges) {
            Stack<SearchInfo> stack = new ObjectArrayList<>();
            stack.push(new SearchInfo(start, startPrev, false));
            while (!stack.isEmpty()) {
                SearchInfo info = stack.pop();
                if (isFinish.test(info.current)) {
                    edges.add(info.prevEdge);
                    edges.remove(0);
                    return true;
                }
                if (!info.backtrack) {
                    if (info.current.visited != 0)
                        continue;
                    info.current.visited = 1;
                    stack.push(new SearchInfo(info.current, info.prevEdge, true));
                    edges.add(info.prevEdge);
                    /*
                     * The iteration is performed in the reverse order since the infos are pushed on the
                     * stack and therefore will be processed in the again reverse order
                     */
                    Iterator<Edge> iterator =
                            info.current.embedded.reverseCircularIterator(info.prevEdge);
                    while (iterator.hasNext()) {
                        Edge currentEdge = iterator.next();
                        Node opposite = currentEdge.getOpposite(info.current);
                        if ((!canGo.test(opposite) || opposite.visited != 0)
                                && !isFinish.test(opposite)) {
                            continue;
                        }
                        stack.push(new SearchInfo(opposite, currentEdge, false));
                    }
                } else {
                    edges.remove(edges.size() - 1);
                }
            }
            return false;
        }

        /**
         * Finds the highest obstructing path in the component rooted at {@code componentRoot}. See the
         * original paper for the definition of the obstructing path. This method heavily relies on the
         * fact that the method findPathDfs
         * chooses the edges in the clockwise order.
         *
         * @param  componentRoot the root of the component
         * @param  w             the node called {@code w} in the Kuratowski subdivision extraction phase.
         * @return               the edges of the desired path as a list
         */
        private List<Edge> findHighestObstructingPath(Node componentRoot, Node w) {
            clearVisited();
            List<Edge> result = new ObjectArrayList<>();
            OuterFaceIter circulator = componentRoot.iterator(Side1);

            for (Node current = circulator.next(); current != w; current = circulator.next()) {
                if (findPathDfs(
                        current, current.embedded.getFirst(), n -> !n.marked, n -> n.boundaryHeight < 0,
                        result)) {
                    return result;
                }
            }
            throw new IllegalStateException("The method should have found a path, but it didn't");
        }

        /**
         * Finishes the Kuratowski subdivision extraction by constructing the desired subgraph
         *
         * @param  subdivision the edges in the Kuratowski subdivision
         * @return             the Kuratowski subgraph of the {@code graph}
         */
        private IntGraph finish(Set<Edge> subdivision) {
            IntSet edgeSubset = new IntOpenHashSet();
            IntSet vertexSubset = new IntOpenHashSet();
            subdivision.forEach(e -> {
                edgeSubset.add(e.graphEdge());
                vertexSubset.add(e.target.graphVertex);
                vertexSubset.add(e.source.graphVertex);
            });
            return g.subGraphCopy(vertexSubset, edgeSubset);
        }

        /**
         * Adds the edges on the outer face of the component rooted at {@code componentRoot} to the set
         * {@code edges}
         *
         * @param edges         the set to add the edges to
         * @param componentRoot the root of the biconnected component
         */
        private static void addBoundaryEdges(Set<Edge> edges, Node componentRoot) {
            OuterFaceIter circulator = componentRoot.iterator(Side1);
            Node current;
            do {
                Edge edge = circulator.edgeToNext();
                edge.source.marked = edge.target.marked = true;
                edges.add(edge);
                current = circulator.next();
            } while (current != componentRoot);
        }

        /**
         * Orient the connections on the outer face of the component rooted at {@code componentRoot}
         * such that they are ordered
         *
         * @param componentRoot the root of the component to process
         */
        private static void fixBoundaryOrder(Node componentRoot) {
            if (componentRoot.embedded.size() < 2) {
                return;
            }
            Node componentParent = componentRoot.getParent();
            Edge edgeToNext = componentRoot.embedded.getLast(),
                    edgeToPrev = componentRoot.embedded.getFirst();
            Node next = edgeToNext.getOpposite(componentParent),
                    prev = edgeToPrev.getOpposite(componentParent);

            componentRoot.outerFaceNeighbor1 = next;
            componentRoot.outerFaceNeighbor2 = prev;
            next.outerFaceNeighbor2 = componentRoot;
            prev.outerFaceNeighbor1 = componentRoot;
            Node current = componentRoot.outerFaceNeighbor1;
            do {
                edgeToNext = current.embedded.getLast();
                edgeToPrev = current.embedded.getFirst();
                next = edgeToNext.getOpposite(current);
                prev = edgeToPrev.getOpposite(current);
                if (prev != componentParent)
                    current.outerFaceNeighbor2 = prev;
                if (next != componentParent)
                    current.outerFaceNeighbor1 = next;
                current = next;
            } while (current != componentParent);
        }

        /**
         * Removes the edges from the outer face from the {@code start} node to the {@code end} node in
         * the direction {@code dir} from the set {@code edges}
         *
         * @param start the start of the boundary path
         * @param end   the end of the boundary path
         * @param dir   the direction to take from the {@code start} node
         * @param edges the set of edges to modify
         */
        private static void removeUp(Node start, Node end, boolean dir, Set<Edge> edges) {
            if (start == end)
                return;
            OuterFaceIter circulator = start.iterator(dir);
            Node next;
            do {
                Edge edge = circulator.edgeToNext();
                edges.remove(edge);
                next = circulator.next();
            } while (next != end);
        }

        /**
         * Effectively is a method for finding node {@code z} in the notations of the original paper.
         * The search proceeds in the reverse order of the path from the {@code backEdge} to the node
         * {@code w}
         *
         * @param  w        the start of the path down
         * @param  backEdge the last edge on the path
         * @return          the desired node {@code z} or null if the source of the {@code backEdge} is equal to
         *                  {@code w}
         */
        private static Node getNextOnPath(Node w, Edge backEdge) {
            if (backEdge.source == w) {
                return null;
            }
            Node prev = backEdge.source, current = backEdge.source.getParent();
            while (current != w) {
                prev = current;
                current = current.getParent();
            }
            return prev;
        }

        /**
         * Finds a path from some intermediate nodes on the path represented by the list {@code path} to
         * the node {@code v}. The path to {@code v} certainly doesn't exist if the list {@code path}
         * has size 1, because we're looking for a path from some intermediate node
         *
         * @param  path the path between left and right outer face branches
         * @param  v    the parent of the biconnected component
         * @return      the path edges in a list, which can be empty
         */
        private List<Edge> findPathToV(List<Edge> path, Node v) {
            clearVisited();
            int i = 0;
            Edge currentEdge = path.get(i);
            Node current = currentEdge.source.boundaryHeight != 0 ? currentEdge.target : currentEdge.source;
            List<Edge> result = new ObjectArrayList<>();
            while (i < path.size() - 1) {
                if (findPathDfs(current, currentEdge, n -> !n.marked, n -> n == v, result))
                    return result;
                ++i;
                currentEdge = path.get(i);
                current = currentEdge.getOpposite(current);
            }
            return result;
        }

        /**
         * Checks whether the first node {@code a} is strictly higher than nodes {@code b} and {@code c}
         *
         * @param  a a node in the dfs tree
         * @param  b a node in the dfs tree
         * @param  c a node in the dfs tree
         * @return   true if the first node in strictly higher that other node, false otherwise
         */
        private static boolean firstStrictlyHigher(Node a, Node b, Node c) {
            return a.height > b.height && a.height > c.height;
        }

        /**
         * Checks whether the biconnected component rooted at {@code componentRoot} can be used to
         * extract a Kuratowski subdivision. It can be used in the case there is one externally active
         * node on each branch of the outer face and there is a pertinent node on the lower part of the
         * outer face between these two externally active nodes.
         *
         * @param  componentRoot the root of the biconnected component
         * @param  v             an ancestor of the nodes in the biconnected component
         * @return               an un-embedded back edge, which target is {@code v} and which can be used to extract a
         *                       Kuratowski subdivision, or {@code null} is no such edge exist for this biconnected
         *                       component
         */
        private static Edge checkComponentForFailedEdge(Node componentRoot, Node v) {
            OuterFaceIter firstDir =
                    getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, v, Side1);
            Node firstDirNode = firstDir.getCurrent();
            if (firstDirNode == componentRoot)
                return null;
            OuterFaceIter secondDir =
                    getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, v, Side2);
            Node secondDirNode = secondDir.getCurrent();
            if (firstDirNode == secondDirNode)
                return null;
            for (Node current = firstDir.next(); current != secondDirNode; current = firstDir.next())
                if (current.isPertinentWrtTo(v))
                    return findBackEdge(current, e -> e.target == v && !e.embedded);;
            return null;
        }

        /**
         * Finds an un-embedded back edge to {@code v}, which can be used to extract the Kuratowski
         * subdivision. If the merge stack isn't empty, the last biconnected component processed by the
         * walkdown can be used to find such an edge, because walkdown descended to that component
         * (which means that component is pertinent) and couldn't reach a pertinent node. This can only
         * happen by encountering externally active nodes on both branches of the traversal. Otherwise,
         * be have look in all the child biconnected components to find an un-embedded back edge. We're
         * guided by the fact that an edge can not be embedded only in the case both traversals of the
         * walkdown could reach all off the pertinent nodes. This in turn can happen only if both
         * traversals get stuck on externally active nodes.
         * <p>
         * <b>Note:</b> not every un-embedded back edge can be used to extract a Kuratowski subdivision
         *
         * @param  v the vertex which has an un-embedded back edge incident to it
         * @return   the found un-embedded back edge which can be used to extract a Kuratowski subdivision
         */
        private Edge findFailedEdge(Node v) {
            if (mergeStack.isEmpty()) {
                for (Node child : v.separatedDfsChildList) {
                    Node componentRoot = child.initialComponentRoot;
                    Edge result = checkComponentForFailedEdge(componentRoot, v);
                    if (result != null)
                        return result;
                }
                return null; // should not happen in case node v has an incident un-embedded back edge
            } else {
                MergeInfo info = mergeStack.top();
                return checkComponentForFailedEdge(info.child, v);
            }
        }

        /**
         * Lazily extracts a Kuratowski subdivision from the {@code graph}. The process of adding the
         * edges of the subdivision to the resulting set of edges had been made explicit for every case.
         *
         * @return a Kuratowski subgraph of the {@code graph}
         */
        private IntGraph getKuratowskiSubdivision(Node failedV) {
            // remove short-circuit edges and orient all embedded lists clockwise
            Set<Edge> subdivision = new HashSet<>();
            // find the needed un-embedded back edge which can be used to find Kuratowski subgraph
            Edge failedEdge = findFailedEdge(failedV);
            assert failedEdge != null;
            /*
             * We're iteratively moving up traversing the outer faces of the biconnected components
             * to find externally active nodes x and y, which are on different branches of the outer
             * face. The way we're finding the nodes x and y helps us eliminate the case E_1
             * described in the original paper. This can be done because we always find the closest
             * to the node w externally active nodes x and y.
             */
            Node x, y, v = failedEdge.target, w = failedEdge.source, componentRoot;
            for (;;) {
                componentRoot = getComponentRoot(w);
                x = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, v, Side2).getCurrent();
                y = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, v, Side1).getCurrent();
                if (x.isBicompRoot()) {
                    w = x.getParent();
                } else if (y.isBicompRoot()) {
                    w = y.getParent();
                } else {
                    componentRoot = getComponentRoot(w);
                    break;
                }
            }
            Edge xBackEdge = findBackEdge(x, v.height);
            Edge yBackEdge = findBackEdge(y, v.height);
            Node backLower = lowest(xBackEdge.target, yBackEdge.target);
            Node backHigher = highest(xBackEdge.target, yBackEdge.target);
            addPathEdges(subdivision, xBackEdge, x);
            addPathEdges(subdivision, yBackEdge, y);
            addBoundaryEdges(subdivision, componentRoot);

            if (componentRoot.getParent() != v) {
                // case A, v isn't a parent of the component we've found
                addPathEdges(subdivision, componentRoot.getParent(), backLower);
                addPathEdges(subdivision, failedEdge, w);
                return finish(subdivision);
            }
            // node z will be null only if the tree path from w to failedEdge is failedEdge itself
            Node z = getNextOnPath(w, failedEdge);
            Edge backEdge = null;
            if (z != null)
                backEdge = findBackEdgeSubtreeDfs(z, e -> e.target.height < v.height && e != failedEdge);
            if (backEdge != null) {
                // case B
                addPathEdges(subdivision, backEdge, w);
                addPathEdges(subdivision, failedEdge, w);
                Node highest = highest(xBackEdge.target, highest(yBackEdge.target, backEdge.target));
                Node lowest = lowest(xBackEdge.target, lowest(yBackEdge.target, backEdge.target));
                addPathEdges(subdivision, highest, lowest);
                return finish(subdivision);
            }
            /*
             * If we failed to either case A or B, we have to find a highest obstructing path and
             * then deal with cases C - E
             */
            setBoundaryDepth(componentRoot, w, Side1, 1);
            setBoundaryDepth(componentRoot, w, Side2, -1);

            assert x.boundaryHeight > 0;
            List<Edge> path = findHighestObstructingPath(componentRoot, w);
            assert !path.isEmpty();

            Edge firstEdge = path.get(0);
            Edge lastEdge = path.get(path.size() - 1);
            Node firstNode = firstEdge.source.boundaryHeight > 0 ? firstEdge.source : firstEdge.target;
            Node lastNode = lastEdge.source.boundaryHeight < 0 ? lastEdge.source : lastEdge.target;
            if (firstNode.boundaryHeight < x.boundaryHeight || lastNode.boundaryHeight > y.boundaryHeight) {
                // case C, either the first node or the last node of the path is higher than x or y
                // respectively
                Node removeStart;
                if (lastNode.boundaryHeight > y.boundaryHeight) {
                    removeStart = firstNode.boundaryHeight < x.boundaryHeight ? firstNode : x;
                    removeUp(removeStart, componentRoot, Side2, subdivision);
                } else {
                    removeUp(y, componentRoot, Side1, subdivision);
                }
                addPathEdges(subdivision, failedEdge, w);
                subdivision.addAll(path);
                addPathEdges(subdivision, v, backLower);
                return finish(subdivision);
            }
            path.forEach(e -> e.source.marked = e.target.marked = true);
            List<Edge> pathToV = findPathToV(path, v);
            if (!pathToV.isEmpty()) {
                // case D, we have a path to the node v
                removeUp(x, componentRoot, Side2, subdivision);
                removeUp(y, componentRoot, Side1, subdivision);
                subdivision.addAll(path);
                subdivision.addAll(pathToV);
                addPathEdges(subdivision, v, backLower);
                addPathEdges(subdivision, failedEdge, w);
                return finish(subdivision);
            }
            Edge externallyActive = findBackEdge(w, v.height, failedEdge);
            assert externallyActive != null;
            addPathEdges(subdivision, externallyActive, w);
            if (firstStrictlyHigher(externallyActive.target, xBackEdge.target, yBackEdge.target)) {
                // case E_2, equivalent to A
                addPathEdges(subdivision, componentRoot.getParent(), backLower);
            } else if (firstStrictlyHigher(
                    xBackEdge.target, yBackEdge.target, externallyActive.target)) {
                // case E_2, u_x is higher
                removeUp(componentRoot, x, Side1, subdivision);
                removeUp(w, lastNode, Side1, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(subdivision, v,
                        lowest(backLower, externallyActive.target));
            } else if (firstStrictlyHigher(
                    yBackEdge.target, xBackEdge.target, externallyActive.target)) {
                // case E_2, u_y is higher
                removeUp(y, componentRoot, Side1, subdivision);
                removeUp(firstNode, w, Side1, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(subdivision, v,
                        lowest(backLower, externallyActive.target));
            } else if (firstNode.boundaryHeight > x.boundaryHeight) {
                // case E_4, p_x is lower than x
                removeUp(w, lastNode, Side1, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(
                        subdivision,
                        highest(backHigher, externallyActive.target),
                        lowest(backLower, externallyActive.target));
            } else if (lastNode.boundaryHeight < y.boundaryHeight) {
                // case E_4, p_y is lower than y
                removeUp(firstNode, w, Side1, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(
                        subdivision,
                        highest(backHigher, externallyActive.target),
                        lowest(backLower, externallyActive.target));
            } else {
                // case E, extracting K_5
                subdivision.addAll(path);
                addPathEdges(subdivision, v,
                        lowest(backLower, externallyActive.target));
                addPathEdges(subdivision, failedEdge, w);
            }
            return finish(subdivision);
        }

    }

}
