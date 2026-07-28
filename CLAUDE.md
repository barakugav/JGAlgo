# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

JGAlgo — a high-performance Java (17+) library of graph algorithms and data structures. Maven multi-module project (`com.jgalgo`). Performance is the core design driver: everything is built on primitives (not boxed objects), uses [fastutil](https://fastutil.di.unimi.it/) collections, plain arrays instead of hash maps where possible, and reuses allocations across algorithm invocations.

## Modules

- **jgalgo-core** — the library: graph data structures, algorithms, generators, internal DS. (~95% of the code)
- **jgalgo-io** — graph readers/writers: GraphML, GML, DIMACS, LEDA, GEXF, Graph6/Sparse6/Digraph6.
- **jgalgo-adapt-guava** / **jgalgo-adapt-jgrapht** — adapters bridging JGAlgo graphs to/from Guava and JGraphT.
- **jgalgo-bench** — JMH benchmarks (excluded from most quality gates).
- **jgalgo-example** — usage examples.

## Common commands

Run from the repo root. Do not use `rtk proxy`.

```bash
# Build without tests (fast; also runs code generation, see below)
mvn package -Dmaven.test.skip

# Run all tests (surefire runs test classes in parallel, forkCount = num cores)
mvn test

# Tests + JaCoCo coverage report
mvn test jacoco:report

# One module only
mvn test -pl jgalgo-core

# A single test class / method (offline, module-scoped is much faster)
mvn test -pl jgalgo-core -Dtest=ShortestPathSingleSourceDijkstraTest
mvn test -pl jgalgo-core -Dtest=ShortestPathSingleSourceDijkstraTest#testRandGraphDirectedPositive

# Static analysis (SpotBugs) — bench module is excluded
mvn compile spotbugs:check -pl -jgalgo-bench

# Style check (fails on warnings)
mvn compile checkstyle:check

# Aggregate Javadoc
mvn javadoc:aggregate

# Full pre-commit gate: clean+build, tests+coverage, spotbugs, checkstyle, javadoc
python3 etc/precommit.py            # has --skip-rebuild/--skip-tests/--skip-static/--skip-style/--skip-javadoc
```

## Code generation (important)

`jgalgo-core` contains **primitive-type-specialized** sources that are generated, not hand-written:

- `jgalgo-core/gensources.py` renders `jgalgo-core/template/*.java.template` into `jgalgo-core/src-generated/` (git-ignored, added to the compile path by build-helper-maven-plugin). Maven runs it automatically in the `generate-sources` phase and cleans it on `mvn clean`.
- Generated families: `Weights*`/`IWeights*` containers, referenceable heaps, pairing/binomial/Fibonacci heaps, and BST/red-black/splay trees — each specialized for combinations of key/value types (Int, Long, Double, Obj, ...).
- Templates use a **custom preprocessor**: `#if/#elif/#else/#endif` lines whose conditions are evaluated as Python expressions, plus constant substitution (`KEY_TYPE_NAME`, `PRIMITIVE_KEY_TYPE`, `FASTUTIL_KEY_TYPE`, ...) and function macros (`COMPARE_KEY_DEFAULT(a,b)`, `KEY_PRIMITIVE_TO_BOXED(x)`, ...).
- Regeneration is **hash-gated**: gensources.py only regenerates a template when its hash (in `src-generated/.gen/hashes.json`) changed. Force a clean regen with `python3 jgalgo-core/gensources.py --clean` then rebuild.

**Never edit files under `src-generated/`** — change the corresponding `.template` file. If a generated class looks wrong, its source of truth is `jgalgo-core/template/`.

## The graph type hierarchy (central concept)

Three tiers of graph, increasingly fast and increasingly constrained:

- **`Graph<V,E>`** — generic; vertices/edges are arbitrary hashable IDs.
- **`IntGraph`** (extends `Graph<Integer,Integer>`) — IDs are primitive `int`. Comes with `I`-prefixed companion types that avoid boxing: `IWeights`, `IWeightFunction`, `IPath`, `IEdgeSet`, and per-algorithm `IResult`.
- **`IndexGraph`** (extends `IntGraph`) — IDs are always contiguous `0..n-1` (vertices) and `0..m-1` (edges). Fastest, but IDs may be renamed when elements are removed. Subscribe to renames via `addVertexRemoveListener`/`addEdgeRemoveListener`.

Any `Graph` exposes `indexGraph()` plus `indexGraphVerticesMap()`/`indexGraphEdgesMap()` (`IndexIdMap`) to translate between user IDs and indices.

**Algorithms operate on `IndexGraph` internally.** The standard flow (implemented in the `*Abstract` base classes) is: accept a `Graph<V,E>` → get its `IndexGraph` and `IndexIdMap`s → run the heavy logic on indices → map the result back to user IDs.

## Algorithm API pattern (used across every `com.jgalgo.alg.*` subpackage)

Each algorithm follows the same shape, e.g. `ShortestPathSingleSource`:

- A public **interface** with a `computeXxx(Graph, ...)` method returning a `Result<V,E>` (plus an int-specialized `IResult` subinterface with primitive-typed overloads that `@Deprecated`-shadow the boxed ones).
- Static **`newInstance()`** → default implementation; static **`builder()`** → a `Builder` that picks a concrete impl from options (e.g. `negativeWeights`, `dag`, `cardinality`, `intWeights`). `newInstance()` is just `builder().build()`.
- An abstract base class **`XxxAbstract`** implementing the generic `computeXxx` by delegating to an `IndexGraph` overload and handling the ID↔index mapping.
- Concrete impls named by algorithm/author, extending the abstract base — e.g. `ShortestPathSingleSourceDijkstra`, `...BellmanFord`, `...Goldberg`, `...Dial`, `...Dag`, `...Cardinality`.

To add an algorithm variant: write an impl extending the `*Abstract` base, then wire it into the interface's `builder()`.

## jgalgo-core package map

- `com.jgalgo.graph` — graph interfaces + implementations (Array / Linked / Matrix / Hashmap / CSR backends), weights, factories, builders, index maps, views.
- `com.jgalgo.alg.*` — algorithms grouped by topic: `shortestpath`, `connect`, `flow`, `tree`, `match`, `span`, `cycle`, `cover`, `clique`, `color`, `closure`, `cores`, `dag`, `distancemeasures`, `euler`, `hamilton`, `isomorphism`, `traversal`, `bipartite`. `common` holds shared result types (`Path`/`IPath`, `VertexPartition`, `VertexBiPartition`, ...). `unstable` is explicitly non-stable API.
- `com.jgalgo.gen` — graph generators (Gnp, Gnm, Barabási–Albert, complete, bipartite, uniform tree, recursive-matrix, set operations).
- `com.jgalgo.internal.ds` — internal data structures (heaps, trees, union-find). `com.jgalgo.internal.util` — helpers. All of `com.jgalgo.internal.*` is excluded from Javadoc.
- `com.jgalgo.JGAlgoConfig` — global library config (e.g. `setParallelByDefault`).

## Testing conventions

- Tests extend `com.jgalgo.internal.util.TestBase` (JUnit 5 / Jupiter).
- Tests are randomized but **deterministic**: each `@Test` hardcodes a `final long seed = 0x...L;` and passes it into a shared `XxxTestUtils` helper. Helpers use `SeedGenerator` (derives per-phase seeds) and `PhasedTester` (runs the test over multiple phases with growing graph sizes / repetitions).
- Cross-implementation test logic lives in `*TestUtils` classes and is reused by each concrete impl's test class — so a new impl usually just adds a small test class calling the existing utils with fresh seeds.

## Style

- Checkstyle (`etc/checkstyle.xml`) is enforced with `violationSeverity=warning` — warnings fail the build. Key rules: max line length **120**, **tabs** for indentation (width 4), no star imports, custom import ordering, Javadoc required on public types and methods, one top-level class per file.
- Every source file starts with the Apache 2.0 license header.
- Javadoc renders LaTeX via MathJax; write math as `\( ... \)`.
- Editor formatter config: `etc/eclipse-java-style.xml` (also consumed by the VSCode Java extension and by gensources.py to format generated files via Eclipse when available).
