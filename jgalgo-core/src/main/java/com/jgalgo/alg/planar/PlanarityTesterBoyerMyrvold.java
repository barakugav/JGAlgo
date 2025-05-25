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

import static com.jgalgo.internal.ds.LinkedList.head;
import static com.jgalgo.internal.ds.LinkedList.isNone;
import static com.jgalgo.internal.ds.LinkedList.tail;
import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.ds.LinkedList;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntPair;
import com.jgalgo.internal.util.IterTools.Iter;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlanarityTesterBoyerMyrvold extends PlanarityTesterAbstract {

    @Override
    protected PlanarityTester.Result<Integer, Integer> computePlanarity(IndexGraph g, boolean addEdgesWeights) {
        // TODO: support directed graphs
        Assertions.onlyUndirected(g);
        // TODO: support graphs with parallel edges
        Assertions.noParallelEdges(g, "graphs with parallel edges are not supported");
        // TODO: support graphs with self edges
        Assertions.noSelfEdges(g, "graphs with self edges are not supported");

        Worker2 worker = new Worker2(g);
        var planarityResult = worker.computePlanarity();

        if (planarityResult.isOk()) {
            return Result.planar(() -> worker.createEmbeddings(addEdgesWeights));
        } else {
            return Result.nonPlanar(() -> {
                return Optional.of(worker.getKuratowskiSubdivision(planarityResult.unwrapError()));
            });
        }
    }

    private static class Worker {
        final IndexGraph g;

        // The nodes of the algorithms.
        //
        // There is a Node object for each real vertex in the graph, and a virtual node for any non DFS root vertex.
        // Each such virtual node is a root of a biconnected component. After preprocessing, each biconnected component
        // is composed of a single real node and a virtual node matching its real parent.
        // Real nodes and their matching virtual nodes share the same dfs index.
        // Each node (real or virtual) at every step of the algorithm is contained in a biconnected component and has a
        // single virtual bi-comp root, which is the root of the biconnected component this node is contained in.
        final Node[] nodes;
        int nodesNum;

        // The edges of the algorithm.
        //
        // There is an Edge object for each edge in the graph, and a virtual edge for each short circuit edge.
        // Short circuit edges are used to connect nodes on the outer edges to make the traversal of the outer face
        // faster the next time we need to traverse it. When we add a short circuit edge, some nodes become inner
        // vertices (not on the outer face anymore), therefore we can do it only when the nodes are not externally
        // active.
        final Edge[] edges;
        int edgesNum;

        // A data structure representing multiple linked lists, each one of them is a list of DFS children of a node.
        // When two bi-comps are merged, the root of the child bi-comp is removed from the parent's list of children.
        //
        // Each node is stored by its Node::idx.
        final LinkedList.Doubly childrenLists;

        // A data structure representing multiple linked lists, each one of them is a list of pertinent roots of a node.
        // For each node, the list contains virtual nodes matching this node, which are the roots in the child bi-comps
        // of this node.
        //
        // Each node is stored by its Node::idx.
        final LinkedList.Doubly pertinentRootsLists;

        // A data structure representing multiple linked lists, each one of them is a list of embedded (ordered) edges
        // of a node.
        //
        // Each edge appear in two lists (one for each of its endpoints), therefore there is no 1:1 mapping between
        // each Edge to a single index in the data structure. For that, we keep a counter of the number of elements
        // in the data structure and we 'allocate' a new index by incrementing the counter each time we add an edge to a
        // list. In addition, we keep a mapping from allocated indices to edge indices in the {@code embedded2edge}
        // array.
        final LinkedList.Doubly embeddedLists;
        final int[] embedded2edge;
        int embedded2edgeSize;

        // After preprocessing, we iterate over the dfs tree nodes in reverse order of their dfs index, performing a
        // walk up and walk down for each node. This field is the current dfs index of the node we are processing.
        //
        // All the terminology of active (regular, externally or internally), pertinent, etc. are with respect to the
        // current processed node, and methods checking these properties compare to the current dfs index.
        int currentDfsIdx;

        final Stack<BiCompMerge> biCompMergeStack;

        Worker(IndexGraph g) {
            final int n = g.vertices().size();
            final int m = g.edges().size();
            this.g = g;

            final int maxNodesNum = n + n; // we may add n virtual nodes
            final int maxEdgesNum = m + n; // we may add n short circuit edges

            this.nodes = new Node[maxNodesNum];
            this.nodesNum = 0;
            this.edges = new Edge[maxEdgesNum];
            this.edgesNum = 0;

            this.childrenLists = new LinkedList.Doubly(maxNodesNum);
            this.pertinentRootsLists = new LinkedList.Doubly(maxNodesNum);

            // each edge can be added in the lists of both of its endpoints
            final int embeddedListsMaxLen = maxEdgesNum * 2;
            this.embeddedLists = new LinkedList.Doubly(embeddedListsMaxLen);
            this.embedded2edge = new int[embeddedListsMaxLen];
            this.embedded2edgeSize = 0;

            this.biCompMergeStack = new ObjectArrayList<>();
        }

        void checkMe() {
            // for (Node node : nodes()) {
            // boolean ok = (node.outerFaceNeighbor1 == null || node == node.outerFaceNeighbor1.outerFaceNeighbor1
            // || node == node.outerFaceNeighbor1.outerFaceNeighbor2)
            // && (node.outerFaceNeighbor2 == null || node == node.outerFaceNeighbor2.outerFaceNeighbor1
            // || node == node.outerFaceNeighbor2.outerFaceNeighbor2);
            // if (!ok)
            // throw new IllegalStateException("Node " + node.idx + " has invalid outer face neighbors");
            // }
        }

        com.jgalgo.internal.util.Result<Void, Node> computePlanarity() {
            preprocess();
            checkMe();

            final int n = g.vertices().size();
            int[] dfsIdxToNode = new int[n];
            for (Node node : realNodes())
                dfsIdxToNode[node.dfsIdx] = node.idx;

            for (currentDfsIdx = dfsIdxToNode.length - 1; currentDfsIdx >= 0; currentDfsIdx--) {
                Node v = nodes[dfsIdxToNode[currentDfsIdx]];
                assert v.dfsIdx == currentDfsIdx;

                checkMe();
                if (v.downEdges.isEmpty())
                    continue;
                for (int downEdge : v.downEdges)
                    walkUp(edges[downEdge]);
                checkMe();

                int embeddedEdgesNum = 0;
                for (int treeEdgeIdx : v.treeEdges) {
                    Edge treeEdge = edges[treeEdgeIdx];
                    assert treeEdge.target.bicompRoot.dfsIdx == currentDfsIdx;
                    checkMe();
                    embeddedEdgesNum += walkDown(treeEdge.target.bicompRoot);
                    if (!biCompMergeStack.isEmpty())
                        break;
                    checkMe();
                }
                checkMe();
                if (embeddedEdgesNum != v.downEdges.size())
                    // Some edges were not embedded successfully
                    return com.jgalgo.internal.util.Result.error(v);
                checkMe();
            }
            checkMe();
            return com.jgalgo.internal.util.Result.ok(null);
        }

        private void preprocess() {
            // regular nodes matching the real graph vertices are connected via tree and back edges.
            // virtual bi-connected component roots are created for each non-dfs-root node, with the DFS index
            // of the parent node, with the tree edge from the parent to the current node.
            //
            // - For a regular node u with parent v, the tree edge is (v,u).
            // - For a regular node u and ancestor v, and the back edge is (u,v).
            // - For a virtual node that is created for u, that actually represent a duplication of the parent v, the
            // tree edge is (v, u).
            //
            // Given a regular node u, u.parentEdge.source is the parent, and the target is u
            // Given a regular node v, v.backEdges[:].source is the child, and the target is v
            // Given a virtual node w that is created for u, that actually represent a duplication of the parent v,
            // u.parentEdge.source is v, and the target is u (none of them is w)

            final int n = g.vertices().size();
            Node[] v2node = new Node[n];
            Stack<IEdgeIter> stack = new ObjectArrayList<>();
            int dfsIndex = 0;
            for (final int root : range(n)) {
                if (v2node[root] != null)
                    continue;

                Node rootNode = v2node[root] = newDfsRoot(root, dfsIndex++);
                rootNode.outerFaceNeighbor1 = rootNode.outerFaceNeighbor2 = rootNode;
                stack.push(g.outEdges(root).iterator());

                dfs: for (Node u = v2node[root];;) {
                    final Node parent = u.parent();
                    for (IEdgeIter eit = stack.top(); eit.hasNext();) {
                        int e = eit.nextInt();
                        int v = eit.targetInt();
                        if (v2node[v] == null) {
                            // Unvisited vertex, e is a tree edge
                            Edge treeEdge = newTreeEdge(e, u);
                            Node componentRoot = newVirtualBicompRoot(u.dfsIdx, treeEdge);
                            Node node = newRealNode(v, dfsIndex++, componentRoot, treeEdge);
                            addToEmbeddedFirst(componentRoot, treeEdge);
                            addToEmbeddedFirst(node, treeEdge);
                            treeEdge.target = node;
                            u.treeEdges.add(treeEdge.idx);
                            node.outerFaceNeighbor1 = node.outerFaceNeighbor2 = componentRoot;
                            componentRoot.outerFaceNeighbor1 = componentRoot.outerFaceNeighbor2 = node;

                            stack.push(g.outEdges(v).iterator());
                            u = v2node[v] = node;
                            continue dfs;
                        } else {
                            // Visited vertex, e is a back edge
                            Node ancestor = v2node[v];
                            if (parent == ancestor)
                                continue; // Not a back edge, reverse of tree edge from parent to u
                            if (u.dfsIdx < ancestor.dfsIdx)
                                continue; // The reverse edge was already added as a back edge, down edge of u
                            Edge backEdge = newBackEdge(e, u, ancestor);
                            ancestor.downEdges.add(backEdge.idx);
                            u.backEdges.add(backEdge.idx);
                        }
                    }

                    u.directLowpoint = u.backEdges
                            .intStream()
                            .map(backEdge -> edges[backEdge].target.dfsIdx)
                            .min()
                            .orElse(u.dfsIdx);
                    int lowpoint = u.treeEdges
                            .intStream()
                            .map(treeEdge -> edges[treeEdge].target.lowpoint)
                            .min()
                            .orElse(u.dfsIdx);
                    u.lowpoint = Math.min(lowpoint, u.directLowpoint);

                    // backtrack DFS one vertex up
                    stack.pop();
                    if (stack.isEmpty())
                        break;
                    u = parent;
                }
            }

            // Add the DFS children of each node in order sorted by their lowpoint
            int[] lowpointOffsets = new int[n];
            for (Node node : v2node)
                lowpointOffsets[node.lowpoint]++;
            for (int s = 0, i = 0; i < lowpointOffsets.length; i++) {
                int count = lowpointOffsets[i];
                lowpointOffsets[i] = s;
                s += count;
            }
            Node[] nodesSortedByLowpoint = v2node; // reuse array
            for (Node node : realNodes())
                nodesSortedByLowpoint[lowpointOffsets[node.lowpoint]++] = node;
            for (Node node : nodesSortedByLowpoint) {
                if (node.parentEdge != null) {
                    Node parentNode = node.parentEdge.source;
                    parentNode.children = childrenLists.addLast(parentNode.children, node.idx);
                }
            }
        }

        private Node newDfsRoot(int graphVertex, int dfsIndex) {
            assert graphVertex >= 0;
            return newNodeImpl(graphVertex, dfsIndex, null, null);
        }

        private Node newRealNode(int graphVertex, int dfsIndex, Node bicompRoot, Edge parentEdge) {
            assert graphVertex >= 0;
            return newNodeImpl(graphVertex, dfsIndex, Objects.requireNonNull(parentEdge),
                    Objects.requireNonNull(bicompRoot));
        }

        private Node newVirtualBicompRoot(int dfsIndex, Edge parentEdge) {
            return newNodeImpl(-1, dfsIndex, Objects.requireNonNull(parentEdge), null);
        }

        private Node newNodeImpl(int graphVertex, int dfsIndex, Edge parentEdge, Node bicompRoot) {
            int nodeIdx = nodesNum++;
            return nodes[nodeIdx] = new Node(nodeIdx, graphVertex, dfsIndex, parentEdge, bicompRoot);
        }

        private void walkUp(final Edge downEdge) {
            final Node source = downEdge.source;
            final Node target = downEdge.target;
            final int visited = target.dfsIdx;

            assert source.activeBackEdge < 0;
            source.activeBackEdge = downEdge.idx;
            source.visited = visited;

            for (Iterator<Node> leftIter = outerFace(source, Side1), rightIter = outerFace(source, Side2);;) {
                Node left = leftIter.next(), right = rightIter.next();
                if (left.visited == visited || right.visited == visited)
                    break;
                left.visited = right.visited = visited;

                if (left.isBicompRoot() || right.isBicompRoot()) {
                    Node root = left.isBicompRoot() ? left : right;
                    assert root.isBicompRoot();
                    // 'root' is a virtual node, the root of the current bi-comp.

                    // continue up by jumping to the real node, matching the virtual root, in the parent bi-comp.
                    Node current = root.realNodeInParentBiComp();
                    if (current == target)
                        break;
                    current.visited = visited;
                    if (root.parentEdge.target.lowpoint < target.dfsIdx) { // externally active
                        current.pertinentRoots = pertinentRootsLists.addLast(current.pertinentRoots, root.idx);
                    } else { // internally active
                        assert root.parentEdge.target.lowpoint == target.dfsIdx;
                        current.pertinentRoots = pertinentRootsLists.addFirst(current.pertinentRoots, root.idx);
                    }

                    // Continue iterating over the two sides of the upper bicomp
                    leftIter = outerFace(current, Side1);
                    rightIter = outerFace(current, Side2);
                }
            }
        }

        /**
         * The walkdown procedure from the original paper. Either embeds all of the back edges in the
         * subtree rooted at the child of the {@code bicompRoot} or identifies the back edges which
         * can be used to extract a Kuratowski subdivision. Iteratively traverses the tree of the
         * biconnected component and descends only to the pertinent components. This procedure is also
         * responsible for embedding short-circuit edges to make the algorithm run in linear time in the
         * worst case.
         *
         * @param  bicompRoot the root of the component to start the walkdown from
         * @return            the number of successfully embedded edges
         */
        private int walkDown(final Node bicompRoot) {
            int embeddedEdgesNum = 0;
            sideLoop: for (final boolean initialDirection : sides()) {
                boolean side = initialDirection;
                OuterFaceIter outerFace = outerFace(bicompRoot, side);
                outFaceLoop: for (Node current = outerFace.next(); current != bicompRoot;) {
                    checkMe();
                    if (current.activeBackEdge >= 0) {
                        // At this point we walked down from the top vertex along the bi-comps util we reached a vertex
                        // with an edge to the top vertex, which we should embed. When we add the back edge, all the
                        // vertices along the path we walked down become inner vertices, and the separated bi-comps
                        // are merged into one.
                        // The prev vertex on the outer face will be an inner vertex after we embed the edge
                        Node innerNeighbor;
                        boolean innerDirection;
                        // = current.otherOuterFaceNeighbor(outerFace.peekNext());

                        if (current.outerFaceNeighbor1 == outerFace.peekNext()) {
                            innerNeighbor = current.outerFaceNeighbor2;
                            innerDirection = Side2;
                        } else {
                            assert current.outerFaceNeighbor2 == outerFace.peekNext();
                            innerNeighbor = current.outerFaceNeighbor1;
                            innerDirection = Side1;
                        }

                        checkMe();
                        while (!biCompMergeStack.isEmpty())
                            mergeBicomps(biCompMergeStack.pop());

                        embedEdge(bicompRoot, initialDirection, edges[current.activeBackEdge], innerNeighbor,
                                innerDirection);
                        checkMe();
                        current.activeBackEdge = -1;
                        embeddedEdgesNum++;

                        outerFace = new OuterFaceIter(current.otherOuterFaceNeighbor(bicompRoot), current);
                    }
                    checkMe();

                    // Continue down the tree to a child bi-comp if its pertinent (has a back edge in its sub-tree)
                    if (!isNone(head(current.pertinentRoots))) {
                        // 'childRoot' is a virtual node, matching current, a root of a bi-comp.
                        final Node childRoot = nodes[head(current.pertinentRoots)];

                        OuterFaceIter outerFace1 = outerFace(childRoot, Side1);
                        OuterFaceIter outerFace2 = outerFace(childRoot, Side2);
                        // TODO: we should search both sides simultaneously, for linear time
                        Node activeNode1 = outerFace1.nextUntil(this::isActive);
                        Node activeNode2 = outerFace2.nextUntil(this::isActive);

                        if (isInternallyActive(activeNode1)) {
                            side = Side1;
                        } else if (isInternallyActive(activeNode2)) {
                            side = Side2;
                        } else if (activeNode1.isPertinent()) {
                            side = Side1;
                        } else {
                            side = Side2;
                        }

                        // The childPrev is a neighbor of the child that we want to keep on the outer face
                        Node childPrev;
                        if (side == Side1) {
                            childPrev = childRoot.outerFaceNeighbor2;
                        } else {
                            assert side == Side2;
                            childPrev = childRoot.outerFaceNeighbor1;
                        }
                        Node parent = current, parentNext = outerFace.peekNext();
                        biCompMergeStack
                                .push(new BiCompMerge(
                                        parent, parentNext,
                                        childRoot, childPrev,
                                        side));

                        Node otherActiveNode;
                        OuterFaceIter otherOuterFace;
                        if (side == Side1) {
                            current = activeNode1;
                            outerFace = outerFace1;
                            otherActiveNode = activeNode2;
                            otherOuterFace = outerFace2;
                        } else {
                            assert side == Side2;
                            current = activeNode2;
                            outerFace = outerFace2;
                            otherActiveNode = activeNode1;
                            otherOuterFace = outerFace1;
                        }
                        if (!otherActiveNode.hasRootNeighbor())
                            addShortCircuitEdge(otherActiveNode, childRoot, !side, otherOuterFace.prev());
                        checkMe();
                        continue outFaceLoop;
                    }
                    checkMe();

                    if (isActive(current)) {
                        // current vertex is externally active, cant continue in this side.
                        if (!biCompMergeStack.isEmpty())
                            break sideLoop; // abort
                        if (!current.hasRootNeighbor())
                            addShortCircuitEdge(current, bicompRoot, initialDirection, outerFace.prev());
                        checkMe();
                        continue sideLoop;
                    }
                    checkMe();

                    // continue in the current side
                    current = outerFace.next();
                }
            }
            return embeddedEdgesNum;
        }

        private void mergeBicomps(BiCompMerge merge) {
            Node parent = merge.parent;
            Node virtualRoot = merge.child;
            Node virtualRootChild = virtualRoot.parentEdge.target;
            assert merge.parent.dfsIdx == merge.child.dfsIdx;

            parent.pertinentRoots = pertinentRootsLists.remove(parent.pertinentRoots, virtualRoot.idx);
            parent.children = childrenLists.remove(parent.children, virtualRootChild.idx);

            // merge embedded edges lists of the virtual root and the parent (real node)
            boolean parentEdgesDirection =
                    edges[embedded2edge[head(parent.embeddedEdges)]].otherEndpoint(parent) == merge.parentNext;
            if ((merge.childDirection == Side1) ^ !parentEdgesDirection) {
                assert !virtualRoot.parentEdge.isInverted;
                virtualRoot.parentEdge.isInverted = true;
                // This doesn't take more than O(E) overall as we only reverse the list once per virtual node
                virtualRoot.embeddedEdges = embeddedLists.reverse(virtualRoot.embeddedEdges);
            }
            if (parentEdgesDirection) {
                parent.embeddedEdges = embeddedLists.concat(parent.embeddedEdges, virtualRoot.embeddedEdges);
            } else {
                parent.embeddedEdges = embeddedLists.concat(virtualRoot.embeddedEdges, parent.embeddedEdges);
            }
            virtualRoot.embeddedEdges = LinkedList.HeadTailNone;

            // checkMe();
            parent.replaceNeighborOpposite(merge.parentNext, merge.childPrev);
            if (merge.childPrev.outerFaceNeighbor1 == virtualRoot)
                merge.childPrev.outerFaceNeighbor1 = parent;
            if (merge.childPrev.outerFaceNeighbor2 == virtualRoot)
                merge.childPrev.outerFaceNeighbor2 = parent;
            virtualRoot.outerFaceNeighbor1 = virtualRoot.outerFaceNeighbor2 = null;
            // checkMe();
        }

        private void embedEdge(Node root, boolean direction, Edge edge, Node innerNeighbor, boolean innerDirection) {
            assert root.isBicompRoot();
            Node source = edge.source;
            if (direction == Side1) {
                addToEmbeddedLast(root, edge);
                root.outerFaceNeighbor1 = source;
            } else {
                addToEmbeddedFirst(root, edge);
                root.outerFaceNeighbor2 = source;
            }

            Edge firstEdge = edges[embedded2edge[head(source.embeddedEdges)]];
            if (firstEdge.otherEndpoint(source).dfsIdx == innerNeighbor.dfsIdx) {
                addToEmbeddedFirst(source, edge);
            } else {
                addToEmbeddedLast(source, edge);
            }

            if (innerDirection == Side1) {
                source.outerFaceNeighbor1 = root;
            } else {
                assert innerDirection == Side2;
                source.outerFaceNeighbor2 = root;
            }
            checkMe();
        }

        private void addShortCircuitEdge(Node source, Node targetBicompRoot, boolean direction, Node innerNeighbor) {
            Edge shortCircuit = newShortCircuitEdge(source, targetBicompRoot.parent());
            boolean innerDirection = innerNeighbor == source.outerFaceNeighbor1 ? Side1 : Side2;
            embedEdge(targetBicompRoot, direction, shortCircuit, innerNeighbor, innerDirection);
        }

        Node node(int idx) {
            assert idx < nodesNum;
            return nodes[idx];
        }

        Iter<Node> nodes() {
            return range(nodesNum).iterator().mapObj(this::node);
        }

        Iter<Node> realNodes() {
            return nodes().filter(Node::isRealNode);
        }

        Iter<Node> virtualBicompRoots() {
            return nodes().filter(Node::isBicompRoot);
        }

        Iter<Node> children(Node node) {
            return childrenLists.iter(node.children).mapObj(this::node);
        }

        void addToEmbeddedFirst(Node node, Edge edge) {
            int idx = embedded2edgeSize++;
            embedded2edge[idx] = edge.idx;
            node.embeddedEdges = embeddedLists.addFirst(node.embeddedEdges, idx);
        }

        void addToEmbeddedLast(Node node, Edge edge) {
            int idx = embedded2edgeSize++;
            embedded2edge[idx] = edge.idx;
            node.embeddedEdges = embeddedLists.addLast(node.embeddedEdges, idx);
        }

        IPlanarEmbedding createEmbeddings(boolean addEdgesWeights) {
            cleanUp();

            IWeightsLong weights;
            Long defaultWeight = Long.valueOf(IntPair.of(-1, -1));
            if (addEdgesWeights) {
                weights = g.addEdgesWeights(PlanarEmbedding.EdgeOrderWeightKey, long.class, defaultWeight);
            } else {
                weights = Weights.createExternalEdgesWeights(g, long.class, defaultWeight);
            }

            for (Node node : realNodes()) {
                for (Node child : children(node)) {
                    Node virtualRoot = child.bicompRoot;
                    node.embeddedEdges = embeddedLists.concat(node.embeddedEdges, virtualRoot.embeddedEdges);
                    virtualRoot.embeddedEdges = LinkedList.HeadTailNone;
                }

                for (var it : embeddedLists.iter(node.embeddedEdges).enumerate()) {
                    int idx = it.firstInt();
                    int edge = edges[embedded2edge[it.secondInt()]].graphEdge();
                    long edgeIndices = weights.get(edge);
                    if (g.edgeSource(edge) == node.graphVertex)
                        edgeIndices = IntPair.setFirst(edgeIndices, idx);
                    if (g.edgeTarget(edge) == node.graphVertex)
                        edgeIndices = IntPair.setSecond(edgeIndices, idx);
                    weights.set(edge, edgeIndices);
                }
            }

            return new PlanarEmbeddings.IndexEmbedding(g, weights);
        }

        void cleanUp() {
            Bitmap isNodeInverted = new Bitmap(nodes.length);
            Stack<Node> stack = new ObjectArrayList<>();
            for (final Node dfsTreeRoot : nodes().filter(n -> n.parentEdge == null)) {
                for (Node current = dfsTreeRoot;;) {
                    cleanShortCircuitEdges(current);

                    boolean isInverted = isNodeInverted.get(current.idx);
                    if (isInverted)
                        current.embeddedEdges = embeddedLists.reverse(current.embeddedEdges);
                    for (int treeEdgeIdx : current.treeEdges) {
                        Edge treeEdge = edges[treeEdgeIdx];
                        stack.push(treeEdge.target);
                        isNodeInverted.set(treeEdge.target.idx, isInverted ^ treeEdge.isInverted);
                    }
                    for (Node node : children(current))
                        node.parentEdge.isInverted = isInverted;

                    if (stack.isEmpty())
                        break;
                    current = stack.pop();
                }
            }
        }

        void cleanShortCircuitEdges(Node node) {
            node.embeddedEdges =
                    embeddedLists.removeIf(node.embeddedEdges, eIdx -> !edges[embedded2edge[eIdx]].isRealEdge());
            assert isNone(head(node.embeddedEdges)) || !embeddedLists.hasPrev(head(node.embeddedEdges));
        }

        static class Node {
            final int idx;

            // Index of graph vertex in the original graph or -1 for a virtual node
            final int graphVertex;

            // Edge from parent to this node.
            // Null from DFS roots.
            final Edge parentEdge;
            // Edges from this node to its children.
            final IntList treeEdges;
            // Edges from descendants of this node in the dfs tree to this node.
            final IntList downEdges;
            // Edges from this node to its ancestors in the dfs tree.
            final IntList backEdges;

            // List of children in the dfs tree.
            //
            // When two bi-comps are merged, the root of the child bi-comp is removed from the parent's list of
            // children.
            //
            // This field is a {@code long} representing the head and tail in the {@code childrenLists} data structure.
            long children;

            // DFS index, also called DFI in the paper.
            final int dfsIdx;

            // The lowest DFS index of an ancestor of v reachable through a back-edge from a descendant of v, or v's
            // own DFS index if no such ancestor exists.
            int lowpoint;

            // The lowest DFS index of a node reachable through a back-edge from *this* node.
            // The paper suggest using the regular lowpoint to detect whether a node is externally active or not, but
            // this seems incorrect and this value should be used instead.
            int directLowpoint;

            // The root of the biconnected component this node is contained in.
            // Note that this field is not updated and it is valid only for nodes that were not merged into their parent
            // bi-comp.
            final Node bicompRoot;

            // The back edge (index) from this node to the currently processed node, or {@code null} if no such edge
            // exists.
            int activeBackEdge;

            // The neighbors of this node on the outer face of this node's bi-comp.
            Node outerFaceNeighbor1;
            Node outerFaceNeighbor2;

            // List of pertinent roots which are children of this node in the dfs tree.
            // This list contains virtual nodes matching this node. The virtual nodes are the roots in the child
            // bi-comps of this node.
            //
            // This field is a {@code long} representing the head and tail in the {@code pertinentRootsLists} data
            // structure.
            long pertinentRoots;

            // An order list of the embedded edges incident to this node.
            //
            // The list contains all the "embedded" edges, which are edges which their order with respect to one another
            // was fixed. The order might be reversed, and it is determined by xor-ing the isInverted field of all
            // the edges in the path from this node to this node's bicomp root.
            //
            // This field is a {@code long} representing the head and tail in the {@code embeddedLists} data structure.
            long embeddedEdges;

            // An auxiliary field used to mark this node for a step in the algorithm.
            // This value can be remain dirty and not clear, as long use a different value for each step.
            int visited;

            private Node(int nodeIdx, int graphVertex, int dfsIndex, Edge parentEdge, Node bicompRoot) {
                this.idx = nodeIdx;
                this.graphVertex = graphVertex;
                this.dfsIdx = dfsIndex;
                this.parentEdge = parentEdge;
                this.bicompRoot = bicompRoot;
                this.embeddedEdges = LinkedList.HeadTailNone;
                this.visited = Integer.MAX_VALUE;
                this.children = LinkedList.HeadTailNone;
                this.pertinentRoots = LinkedList.HeadTailNone;
                this.activeBackEdge = -1;
                if (isRealNode()) {
                    treeEdges = new IntArrayList();
                    downEdges = new IntArrayList();
                    backEdges = new IntArrayList();
                } else {
                    treeEdges = null;
                    downEdges = null;
                    backEdges = null;
                }
            }

            final boolean isBicompRoot() {
                return graphVertex < 0;
            }

            final boolean isRealNode() {
                return graphVertex >= 0;
            }

            Node parent() {
                return parentEdge == null ? null : parentEdge.source;
            }

            // A node is pertinent if it has a back edge to the "current visited vertex" or it has a
            // child biconnected component which has a pertinent node.
            boolean isPertinent() {
                return activeBackEdge >= 0 || !isNone(head(pertinentRoots));
            }

            void replaceNeighborOpposite(Node oppositeNeighbor, Node newNeighbor) {
                if (outerFaceNeighbor1 == oppositeNeighbor) {
                    // assert outerFaceNeighbor2 != oppositeNeighbor; // TODO
                    outerFaceNeighbor2 = newNeighbor;
                } else {
                    assert outerFaceNeighbor2 == oppositeNeighbor;
                    outerFaceNeighbor1 = newNeighbor;
                }
            }

            boolean hasRootNeighbor() {
                return outerFaceNeighbor1.isBicompRoot() || outerFaceNeighbor2.isBicompRoot();
            }

            private Node realNodeInParentBiComp() {
                assert !isRealNode() && isBicompRoot();
                Node real = parentEdge.source;
                assert dfsIdx == real.dfsIdx;
                return real;
            }

            Node otherOuterFaceNeighbor(Node neighbor) {
                if (outerFaceNeighbor1 == neighbor) {
                    return outerFaceNeighbor2;
                } else {
                    assert outerFaceNeighbor2 == neighbor;
                    return outerFaceNeighbor1;
                }
            }

            // @Override
            // public String toString() {
            // if (isRealNode()) {
            // return String.valueOf(graphVertex);
            // } else {
            // return String.valueOf(realNodeInParentBiComp().graphVertex) + "v";
            // }
            // }
        }

        // A node is externally active if it is incident to the edge ending higher than {@code node}, or it
        // has a child biconnected component with a pertinent node
        boolean isExternallyActive(Node node) {
            // Checking node.lowpoint < currentDfsIdx is simpler than this function implementation, and actually correct
            // right after preprocessing, but we may remove some children from the children list of this node, as the
            // list only maintain the children that are not yet merged to the bi-comp of the node, therefore we must
            // check the edges of this node, and the edge of the existing children 'manually'.

            if (node.directLowpoint < currentDfsIdx)
                return true;
            // children are sorted by lowpoint, so its enough to check the first
            int firstChild = head(node.children);
            return !isNone(firstChild) && nodes[firstChild].lowpoint < currentDfsIdx;
        }

        // A node is internally active if it's pertinent and not externally active
        boolean isInternallyActive(Node node) {
            return node.isPertinent() && !isExternallyActive(node);
        }

        // A node is active it is either pertinent or externally active
        boolean isActive(Node node) {
            return isExternallyActive(node) || node.isPertinent();
        }

        OuterFaceIter outerFace(Node node, boolean direction) {
            Node neighbor = direction == Side1 ? node.outerFaceNeighbor1 : node.outerFaceNeighbor2;
            return new OuterFaceIter(neighbor, node);
        }

        static class Edge {
            final int idx;

            // Index of the edge in the original graph, or -1 for a short-circuit edge
            private final int graphEdge;

            final Node source;
            Node target;

            // If true, indicate the embedded edges of the nodes contained in the bi-comp of this edge that this edge
            // exist in their support are inverted. The xor of all the edges in the path from a node to its bicomp
            // root indicate whether or not the embedded edges of the node are inverted. This is used to flip the
            // order of edges in constant time.
            boolean isInverted;

            private Edge(int idx, int graphEdge, Node source, Node target) {
                this.idx = idx;
                this.graphEdge = graphEdge;
                this.source = source;
                this.target = target;
                this.isInverted = false;
            }

            int graphEdge() {
                assert isRealEdge() : "This edge is not real";
                return graphEdge;
            }

            Node otherEndpoint(Node node) {
                if (source == node) {
                    return target;
                } else {
                    assert target == node;
                    return source;
                }
            }

            boolean isRealEdge() {
                return graphEdge >= 0;
            }

            // @Override
            // public String toString() {
            // return Integer.toString(graphEdge);
            // }
        }

        Edge newEdgeImpl(int graphEdge, Node source, Node target) {
            int edgeIdx = edgesNum++;
            return edges[edgeIdx] = new Edge(edgeIdx, graphEdge, source, target);
        }

        Edge newTreeEdge(int graphEdge, Node source) {
            assert graphEdge >= 0;
            return newEdgeImpl(graphEdge, source, null);
        }

        Edge newBackEdge(int graphEdge, Node source, Node target) {
            assert graphEdge >= 0;
            return newEdgeImpl(graphEdge, source, target);
        }

        Edge newShortCircuitEdge(Node source, Node target) {
            return newEdgeImpl(-1, source, target);
        }

        class OuterFaceIter implements Iterator<Node> {
            private Node next;
            private Node current;

            OuterFaceIter(Node next, Node prev) {
                this.next = next;
                this.current = prev;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Node next() {
                Node ret = next;
                next = next.otherOuterFaceNeighbor(current);
                return current = ret;
            }

            Node peekNext() {
                return next;
            }

            // Last element returned by next()
            Node current() {
                return current;
            }

            // The node returned *before* the last call to next()
            Node prev() {
                return current.otherOuterFaceNeighbor(next);
            }

            Node nextUntil(Predicate<Node> predicate) {
                for (final Node begin = current();;) {
                    Node next = next();
                    if (predicate.test(next))
                        return next;
                    if (next == begin)
                        return null;
                }
            }
        }

        static class BiCompMerge {
            // A node in the parent biconnected component, always real.
            final Node parent;
            /**
             * The next node along the traversal of the parent biconnected component
             */
            final Node parentNext;
            // The bi-comp root of the child bi-comp, which is a virtual node matching the real parent node.
            final Node child;
            /**
             * The previous node along the traversal of the child biconnected component
             */
            final Node childPrev;
            /**
             * The direction used to start the traversal of the child biconnected component. Since the
             * {@code child} is the component root, {@code child.outerFaceNeighbors[|1-vOut|]} is equal
             * to the {@code childPrev}
             */
            final boolean childDirection;

            /**
             * Creates new instance of the information needed to merge to biconnected components
             *
             * @param parent         the node current traversal descended from
             * @param parentNext     the next node along the traversal of the parent component
             * @param child          the virtual root of the child biconnected component
             * @param childPrev      the previous node along the traversal of the child component
             * @param childDirection the direction used to enter the child biconnected component
             */
            BiCompMerge(Node parent, Node parentNext, Node child, Node childPrev, boolean childDirection) {
                assert parent.dfsIdx == child.dfsIdx;
                this.parent = parent;
                this.parentNext = parentNext;
                this.child = child;
                this.childPrev = childPrev;
                this.childDirection = childDirection;
            }
        }
    }

    private static final boolean Side1 = false;
    private static final boolean Side2 = true;

    private static boolean[] sides() {
        return new boolean[] { Side1, Side2 };
    }

    private static class Worker2 extends Worker {

        // The height of the node in the created dfs tree. The root of the tree has height $0$. The
        // smaller this value is, the lower the node is considered to be.
        private final int[] heights;

        /**
         * The height of this node on the boundary of the biconnected component, which is used to
         * extract the Kuratowski subdivision. The height of the component root is $0$, the heights
         * on the left side are positive, on the right side - negative. This value is used to
         * quickly determine for two nodes on the same boundary branch which one is higher (closer
         * to the component root).
         */
        private final int[] boundaryHeights;
        /**
         * Used to mark the boundary nodes of the biconnected
         */
        private final Bitmap marked;

        public Worker2(IndexGraph graph) {
            super(graph);
            heights = new int[this.nodes.length];
            boundaryHeights = new int[this.nodes.length];
            marked = new Bitmap(this.nodes.length);
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
        OuterFaceIter findOnOuterFace(Node start, Node stop, boolean dir, Predicate<Node> predicate) {
            OuterFaceIter circulator = outerFace(start, dir);
            for (Node v = circulator.next(); v != stop && !predicate.test(v); v = circulator.next());
            return circulator;
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
        private OuterFaceIter getExternallyActiveSuccessorOnOuterFace(
                Node start, Node stop, boolean dir) {
            return findOnOuterFace(start, stop, dir, this::isExternallyActive);
        }

        /**
         * Returns a component root of component the {@code node} is contained in.
         *
         * @param  node a node in the partially embedded graph
         * @return      a component root of the component the {@code node} belongs to
         */
        private Node getComponentRoot(Node node) {
            return findOnOuterFace(node, node, Side1, Node::isBicompRoot).current();
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
            for (Node current = startEdge.source; current != stop; current = current.parent())
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
        private Edge findBackEdge(Node current, int heightMax) {
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
        private Edge findBackEdge(Node current, int heightMax, Edge forbiddenEdge) {
            return findBackEdge(current, e -> forbiddenEdge != e && heights[e.target.idx] < heightMax);
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
        private Edge findBackEdge(Node current, Predicate<Edge> predicate) {
            for (int edgeIdx : current.backEdges) {
                Edge edge = edges[edgeIdx];
                if (predicate.test(edge))
                    return edge;
            }
            for (Node node : children(current)) {
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
        private Edge findBackEdgeSubtreeDfs(Node start, Predicate<Edge> predicate) {
            Stack<Node> stack = new ObjectArrayList<>();
            stack.push(start);
            while (!stack.isEmpty()) {
                Node current = stack.pop();

                for (int edgeIdx : current.backEdges) {
                    Edge edge = edges[edgeIdx];
                    if (predicate.test(edge))
                        return edge;
                }
                for (int edge : current.treeEdges)
                    stack.push(edges[edge].target);
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
        private Node highest(Node a, Node b) {
            return heights[a.idx] > heights[b.idx] ? a : b;
        }

        /**
         * Returns the lowest of the two input node, i.e. the node with the least height.
         *
         * @param  a a node in the dfs tree
         * @param  b a node in the dfs tree
         * @return   the lowest of the two nodes
         */
        private Node lowest(Node a, Node b) {
            return heights[a.idx] < heights[b.idx] ? a : b;
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
        private void setBoundaryDepth(Node componentRoot, Node w, boolean dir, int delta) {
            OuterFaceIter circulator = outerFace(componentRoot, dir);
            int currentHeight = delta;
            for (Node current = circulator.next(); current != w; current = circulator.next()) {
                boundaryHeights[current.idx] = currentHeight;
                currentHeight += delta;
            }
        }

        /**
         * Clears the visited variable of all the nodes and component roots
         */
        private void clearVisited() {
            Arrays.stream(nodes, 0, nodesNum).forEach(n -> n.visited = 0);
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
        private boolean findPathDfs(
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
                    int begin = embeddedLists
                            .iter(info.current.embeddedEdges)
                            .filter(x -> embedded2edge[x] == info.prevEdge.idx)
                            .nextInt();
                    for (Edge currentEdge : embeddedLists
                            .iterRev(begin)
                            .chain(embeddedLists.iterRev(info.current.embeddedEdges).fuse(x -> x == begin))
                            .mapObj(i -> this.edges[embedded2edge[i]])) {
                        Node opposite = currentEdge.otherEndpoint(info.current);
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
            OuterFaceIter circulator = outerFace(componentRoot, Side1);

            for (Node current = circulator.next(); current != w; current = circulator.next()) {
                if (findPathDfs(
                        current, edges[embedded2edge[head(current.embeddedEdges)]], n -> !marked.get(n.idx),
                        n -> boundaryHeights[n.idx] < 0,
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
        private void addBoundaryEdges(Set<Edge> edges, Node componentRoot) {
            OuterFaceIter circulator = outerFace(componentRoot, Side1);
            Node current;
            do {
                Edge edge = nextEdge(circulator);
                marked.set(edge.source.idx);
                marked.set(edge.target.idx);
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
        private void fixBoundaryOrder(Node componentRoot) {
            if (head(componentRoot.embeddedEdges) == tail(componentRoot.embeddedEdges)) {
                // there are <2 embedded edges
                return;
            }
            Node componentParent = componentRoot.parent();
            Edge edgeToNext = edges[embedded2edge[tail(componentRoot.embeddedEdges)]];
            Edge edgeToPrev = edges[embedded2edge[head(componentRoot.embeddedEdges)]];
            Node next = edgeToNext.otherEndpoint(componentParent);
            Node prev = edgeToPrev.otherEndpoint(componentParent);

            assert next == edgeToNext.otherEndpoint(componentParent);
            assert prev == edgeToPrev.otherEndpoint(componentParent);

            componentRoot.outerFaceNeighbor1 = next;
            componentRoot.outerFaceNeighbor2 = prev;
            next.outerFaceNeighbor2 = componentRoot;
            prev.outerFaceNeighbor1 = componentRoot;
            Node current = componentRoot.outerFaceNeighbor1;
            do {
                edgeToNext = edges[embedded2edge[tail(current.embeddedEdges)]];
                edgeToPrev = edges[embedded2edge[head(current.embeddedEdges)]];
                next = edgeToNext.otherEndpoint(current);
                prev = edgeToPrev.otherEndpoint(current);
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
        private void removeUp(Node start, Node end, boolean dir, Set<Edge> edges) {
            if (start == end)
                return;
            OuterFaceIter circulator = outerFace(start, dir);
            Node next;
            do {
                Edge edge = nextEdge(circulator);
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
            Node prev = backEdge.source, current = backEdge.source.parent();
            while (current != w) {
                prev = current;
                current = current.parent();
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
            Node current = boundaryHeights[currentEdge.source.idx] != 0 ? currentEdge.target : currentEdge.source;
            List<Edge> result = new ObjectArrayList<>();
            while (i < path.size() - 1) {
                if (findPathDfs(current, currentEdge, n -> !marked.get(n.idx), n -> n == v, result))
                    return result;
                ++i;
                currentEdge = path.get(i);
                current = currentEdge.otherEndpoint(current);
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
        private boolean firstStrictlyHigher(Node a, Node b, Node c) {
            return heights[a.idx] > heights[b.idx] && heights[a.idx] > heights[c.idx];
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
        private Edge checkComponentForFailedEdge(Node componentRoot, Node v, IntSet embeddedEdges) {
            OuterFaceIter firstDir =
                    getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, Side1);
            Node firstDirNode = firstDir.current();
            if (firstDirNode == componentRoot)
                return null;
            OuterFaceIter secondDir =
                    getExternallyActiveSuccessorOnOuterFace(componentRoot, componentRoot, Side2);
            Node secondDirNode = secondDir.current();
            if (firstDirNode == secondDirNode)
                return null;
            for (Node current = firstDir.next(); current != secondDirNode; current = firstDir.next())
                if (current.isPertinent())
                    return findBackEdge(current, e -> e.target == v && !embeddedEdges.contains(e.idx));
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
         * @param  failedV the vertex which has an un-embedded back edge incident to it
         * @return         the found un-embedded back edge which can be used to extract a Kuratowski subdivision
         */
        private Edge findFailedEdge(Node failedV) {
            final IntSet embeddedEdges = new IntOpenHashSet();
            for (Node node : realNodes())
                for (int i : embeddedLists.iter(node.embeddedEdges))
                    embeddedEdges.add(embedded2edge[i]);

            if (biCompMergeStack.isEmpty()) {
                for (Node child : children(failedV)) {
                    Node componentRoot = child.bicompRoot;
                    Edge result = checkComponentForFailedEdge(componentRoot, failedV, embeddedEdges);
                    if (result != null)
                        return result;
                }
                return null; // should not happen in case node v has an incident un-embedded back edge
            } else {
                BiCompMerge info = biCompMergeStack.top();
                return checkComponentForFailedEdge(info.child, failedV, embeddedEdges);
            }
        }

        private void computeHeights() {
            Arrays.fill(heights, -1);
            IntStack stack = new IntArrayList();
            for (Node node0 : realNodes()) {
                if (heights[node0.idx] >= 0)
                    continue;
                stack.push(node0.idx);
                while (!stack.isEmpty()) {
                    int idx = stack.topInt();
                    Node node = nodes[idx];
                    Node parent = node.parent();
                    if (parent == null) {
                        heights[idx] = 0;
                        stack.popInt();
                    } else if (heights[parent.idx] >= 0) {
                        heights[idx] = heights[parent.idx] + 1;
                        stack.popInt();
                    } else {
                        stack.push(parent.idx);
                    }
                }
            }
        }

        /**
         * Lazily extracts a Kuratowski subdivision from the {@code graph}. The process of adding the
         * edges of the subdivision to the resulting set of edges had been made explicit for every case.
         *
         * @return a Kuratowski subgraph of the {@code graph}
         */
        private IntGraph getKuratowskiSubdivision(Node failedV) {
            cleanUp();
            for (Node bicompRoot : virtualBicompRoots()) {
                if (bicompRoot.outerFaceNeighbor1 == null)
                    continue; // merged with parent bicomp
                cleanShortCircuitEdges(bicompRoot);
                fixBoundaryOrder(bicompRoot);
            }
            computeHeights();

            // remove short-circuit edges and orient all embedded lists clockwise
            Set<Edge> subdivision = new HashSet<>();
            // find the needed un-embedded back edge which can be used to find Kuratowski subgraph
            Edge failedEdge = findFailedEdge(failedV);
            assert failedEdge != null;
            assert failedEdge.target == failedV;

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
                x = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, Side2).current();
                y = getExternallyActiveSuccessorOnOuterFace(w, componentRoot, Side1).current();
                if (x.isBicompRoot()) {
                    w = x.parent();
                } else if (y.isBicompRoot()) {
                    w = y.parent();
                } else {
                    componentRoot = getComponentRoot(w);
                    break;
                }
            }
            Edge xBackEdge = findBackEdge(x, heights[v.idx]);
            Edge yBackEdge = findBackEdge(y, heights[v.idx]);
            Node backLower = lowest(xBackEdge.target, yBackEdge.target);
            Node backHigher = highest(xBackEdge.target, yBackEdge.target);
            addPathEdges(subdivision, xBackEdge, x);
            addPathEdges(subdivision, yBackEdge, y);
            addBoundaryEdges(subdivision, componentRoot);

            if (componentRoot.parent() != v) {
                // case A, v isn't a parent of the component we've found
                addPathEdges(subdivision, componentRoot.parent(), backLower);
                addPathEdges(subdivision, failedEdge, w);
                return finish(subdivision);
            }
            // node z will be null only if the tree path from w to failedEdge is failedEdge itself
            Node z = getNextOnPath(w, failedEdge);
            Edge backEdge = null;
            if (z != null)
                backEdge = findBackEdgeSubtreeDfs(z, e -> heights[e.target.idx] < heights[v.idx] && e != failedEdge);
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

            assert boundaryHeights[x.idx] > 0;
            List<Edge> path = findHighestObstructingPath(componentRoot, w);
            assert !path.isEmpty();

            Edge firstEdge = path.get(0);
            Edge lastEdge = path.get(path.size() - 1);
            Node firstNode = boundaryHeights[firstEdge.source.idx] > 0 ? firstEdge.source : firstEdge.target;
            Node lastNode = boundaryHeights[lastEdge.source.idx] < 0 ? lastEdge.source : lastEdge.target;
            if (boundaryHeights[firstNode.idx] < boundaryHeights[x.idx]
                    || boundaryHeights[lastNode.idx] > boundaryHeights[y.idx]) {
                // case C, either the first node or the last node of the path is higher than x or y
                // respectively
                Node removeStart;
                if (boundaryHeights[lastNode.idx] > boundaryHeights[y.idx]) {
                    removeStart = boundaryHeights[firstNode.idx] < boundaryHeights[x.idx] ? firstNode : x;
                    removeUp(removeStart, componentRoot, Side2, subdivision);
                } else {
                    removeUp(y, componentRoot, Side1, subdivision);
                }
                addPathEdges(subdivision, failedEdge, w);
                subdivision.addAll(path);
                addPathEdges(subdivision, v, backLower);
                return finish(subdivision);
            }
            for (Edge e : path) {
                marked.set(e.source.idx);
                marked.set(e.target.idx);
            }
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
            Edge externallyActive = findBackEdge(w, heights[v.idx], failedEdge);
            assert externallyActive != null;
            addPathEdges(subdivision, externallyActive, w);
            if (firstStrictlyHigher(externallyActive.target, xBackEdge.target, yBackEdge.target)) {
                // case E_2, equivalent to A
                addPathEdges(subdivision, componentRoot.parent(), backLower);
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
            } else if (boundaryHeights[firstNode.idx] > boundaryHeights[x.idx]) {
                // case E_4, p_x is lower than x
                removeUp(w, lastNode, Side1, subdivision);
                subdivision.addAll(path);
                addPathEdges(subdivision, failedEdge, w);
                addPathEdges(
                        subdivision,
                        highest(backHigher, externallyActive.target),
                        lowest(backLower, externallyActive.target));
            } else if (boundaryHeights[lastNode.idx] < boundaryHeights[y.idx]) {
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

        /**
         * Returns an edge connecting previously returned node with node, which will be returned
         * next. If either of the mentioned nodes is virtual, the edge will be incident to its real
         * counterpart.
         *
         * @return an edge from the current node to the next node
         */
        // The edge connecting the last node returned by next() and the next node.
        Edge nextEdge(OuterFaceIter iter) {

            // // This node or the real node in the parent bi-comp if this node is a virtual bicomp root
            Function<Node, Node> realNode = n -> n.isRealNode() ? n : n.realNodeInParentBiComp();

            Edge edge = edges[embedded2edge[head(iter.current.embeddedEdges)]];
            Node target = realNode.apply(iter.next);
            Node source = realNode.apply(iter.current);
            if (edge.otherEndpoint(source) == target) {
                return edge;
            } else {
                return edges[embedded2edge[tail(iter.current.embeddedEdges)]];
            }
        }

    }

}
