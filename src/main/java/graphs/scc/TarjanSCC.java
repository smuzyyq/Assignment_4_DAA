package graphs.scc;

import java.util.*;
import metrics.MetricsTracker;

/**
 * Tarjan's algorithm for finding Strongly Connected Components (SCC) in a directed graph.
 * Now also builds:
 *  - componentId[v]  -> which SCC the vertex belongs to
 *  - condensation DAG over SCCs
 */
public class TarjanSCC {

    private final List<List<Integer>> adj;
    private final int n;
    private final int[] ids, low;
    private final boolean[] onStack;
    private final Deque<Integer> stack;
    private int id;
    private final List<List<Integer>> components;

    // filled after run()
    private int[] componentId;

    public TarjanSCC(List<List<Integer>> adj) {
        if (adj == null || adj.isEmpty()) {
            throw new IllegalArgumentException("Graph adjacency list cannot be null or empty");
        }
        this.adj = adj;
        this.n = adj.size();
        this.ids = new int[n];
        this.low = new int[n];
        this.onStack = new boolean[n];
        this.stack = new ArrayDeque<>();
        this.components = new ArrayList<>();
        Arrays.fill(ids, -1);
    }

    /**
     * Runs Tarjan and returns SCCs.
     * Components are sorted deterministically.
     */
    public List<List<Integer>> run(MetricsTracker m) {
        m.start();
        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i, m);
            }
        }
        m.stop();

        // sort SCCs for stable output
        components.sort(Comparator.comparingInt(c -> Collections.min(c)));

        // build componentId[v]
        componentId = new int[n];
        for (int compIdx = 0; compIdx < components.size(); compIdx++) {
            for (int v : components.get(compIdx)) {
                componentId[v] = compIdx;
            }
        }

        return components;
    }

    /**
     * Returns componentId[v] after run().
     */
    public int[] getComponentIds() {
        if (componentId == null) {
            throw new IllegalStateException("run() must be called before getComponentIds()");
        }
        return componentId;
    }

    /**
     * Builds condensation DAG: each SCC is a node, edges go between SCCs.
     * No duplicates, sorted adjacency lists.
     */
    public List<List<Integer>> buildCondensation() {
        if (componentId == null) {
            throw new IllegalStateException("run() must be called before buildCondensation()");
        }
        int k = components.size();
        List<Set<Integer>> tmp = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            tmp.add(new HashSet<>());
        }

        for (int u = 0; u < n; u++) {
            int cu = componentId[u];
            for (int v : adj.get(u)) {
                int cv = componentId[v];
                if (cu != cv) {
                    tmp.get(cu).add(cv);
                }
            }
        }

        List<List<Integer>> dag = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            List<Integer> out = new ArrayList<>(tmp.get(i));
            Collections.sort(out);
            dag.add(out);
        }
        return dag;
    }

    private void dfs(int at, MetricsTracker m) {
        m.incDfs();
        stack.push(at);
        onStack[at] = true;
        ids[at] = low[at] = id++;

        for (int to : adj.get(at)) {
            m.incEdge();
            if (ids[to] == -1) {
                dfs(to, m);
                low[at] = Math.min(low[at], low[to]);
            } else if (onStack[to]) {
                low[at] = Math.min(low[at], ids[to]);
            }
        }

        if (ids[at] == low[at]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int node = stack.pop();
                onStack[node] = false;
                comp.add(node);
                if (node == at) break;
            }
            components.add(comp);
        }
    }
}
