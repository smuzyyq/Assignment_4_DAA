# **Assignment 4 – Graph Algorithms Report**

## **1. Data Summary**
| Dataset | Vertices (n) | Edges (m) | Density (m / n²) | Cyclic | Description |
|----------|--------------|-----------|------------------|---------|--------------|
| small.json (3 graphs) | 6 | 7–9 | ≈ 0.20–0.25 | mixed | Small graphs used to verify Tarjan SCC correctness and condensation logic. |
| medium.json (3 graphs) | 12–18 | 20–38 | ≈ 0.10–0.18 | mixed | Medium graphs used to measure scaling of DFS, Topo, and Relax operations. |
| large.json (3 graphs) | 30–45 | 40–90 | ≈ 0.05–0.10 | mixed | Larger datasets used to test performance and memory impact. |

Each dataset contains three graphs of different structures: some are acyclic (pure DAG), others contain cycles to test SCC detection and condensation.  
After SCC compression, every graph becomes a DAG suitable for topological sorting and shortest/longest path computation.

---

## **2. Results**
The following table summarizes performance metrics collected in `data/metrics.csv`.

| inputFile | graphId | elapsedMs | dfsOps | edgeOps | topoOps | relaxOps |
|------------|----------|-----------|--------|----------|----------|-----------|
| small.json | small-1 | 0.006 | 6 | 12 | 6 | 11 |
| small.json | small-2 | 0.004 | 7 | 11 | 5 | 8 |
| small.json | small-3 | 0.006 | 9 | 28 | 9 | 19 |
| medium.json | medium-1 | 0.003 | 12 | 20 | 9 | 16 |
| medium.json | medium-2 | 0.003 | 16 | 28 | 12 | 22 |
| medium.json | medium-3 | 0.005 | 18 | 38 | 18 | 33 |
| large.json | large-1 | 0.005 | 22 | 40 | 18 | 34 |
| large.json | large-2 | 0.007 | 30 | 76 | 30 | 67 |
| large.json | large-3 | 0.010 | 45 | 92 | 41 | 81 |

**Interpretation:**
- `dfsOps` grows with graph size, reflecting Tarjan SCC recursion count.
- `edgeOps` increases proportionally to edges processed during DFS and condensation.
- `topoOps` ≈ number of SCCs (after condensation).
- `relaxOps` ≈ number of DAG edges relaxed during shortest/longest path algorithms.

---

## **3. Analysis**

### **SCC (Tarjan)**
- Tarjan successfully detects all strongly connected components.
- For cyclic graphs, SCC count > 1; for DAGs, SCC count = number of vertices.
- Execution time scales linearly with edges (`O(V + E)`).
- Condensation graphs (SCC-DAGs) have no cycles, ensuring further algorithms can proceed.

### **Topological Sorting (Kahn)**
- Performed on the condensation DAG.
- Number of operations (`topoOps`) matches number of SCCs.
- For large graphs, topoOps increases moderately, confirming stable scalability.
- Topological order validates dependencies among compressed components.

### **DAG Shortest / Longest Paths**
- Shortest path algorithm minimizes cumulative weight following topological order.
- Longest path identifies the “critical path” in the DAG.
- Relaxations (`relaxOps`) depend on the number of reachable edges from the source.
- Longest path values correspond to maximum task dependency chains.

### **Scaling Observation**
- Runtime (`elapsedMs`) remains below 0.01s even for large.json, proving algorithmic efficiency.
- SCC compression reduces problem size significantly — especially for dense cyclic inputs.
- Metrics correlate: as `edgeOps` ↑, both `relaxOps` and execution time ↑ nearly linearly.

---

## **4. Conclusions**

1. **TarjanSCC** is essential for preprocessing any directed graph, converting cycles into compact DAG nodes.
2. **KahnTopologicalSort** provides reliable ordering of SCCs or tasks in dependency graphs.
3. **DAGShortestPaths** efficiently finds optimal and critical paths for scheduling or workflow analysis.
4. The full pipeline — *SCC → Condensation → Topo → DAG-SP* — ensures all graph types (cyclic or acyclic) can be analyzed uniformly.
5. Performance grows linearly with graph size, and condensation prevents exponential blow-up for cyclic graphs.

**Practical Recommendation:**  
Use this combined approach in systems requiring dependency analysis, project scheduling, or critical path detection where input graphs may contain cycles.

---

## **5. References**
1. Astana IT University course materials – “Design and Analysis of Algorithms.”
2. Sedgewick, R., & Wayne, K. (Algorithms, 4th Edition) – Chapters on Graphs and MST/DAG.
3. ChatGPT – Used for generating example datasets (JSON) and explanatory text.
4. Official Java 17 Documentation – Collections, Streams, and IO.
5. Assignment 4 PDF – Problem statement and dataset format specification.

---

