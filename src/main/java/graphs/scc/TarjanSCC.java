package graphs.scc;

import java.util.*;
import metrics.MetricsTracker;

public class TarjanSCC {

    private final List<List<Integer>> adj;
    private final int n;
    private final int[] ids, low;
    private final boolean[] onStack;
    private final Deque<Integer> stack;
    private int id;
    private final List<List<Integer>> components;

    public TarjanSCC(List<List<Integer>> adj) {
        this.adj = adj;
        this.n = adj.size();
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
        components = new ArrayList<>();
        Arrays.fill(ids, -1);
    }

    public List<List<Integer>> run(MetricsTracker m) {
        m.start();
        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) dfs(i, m);
        }
        m.stop();
        return components;
    }

    private void dfs(int at, MetricsTracker m) {
        m.incDfs();
        stack.push(at);
        onStack[at] = true;
        ids[at] = low[at] = id++;

        for (int to : adj.get(at)) {
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
