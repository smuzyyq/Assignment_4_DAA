package graphs.topo;

import java.util.*;
import metrics.MetricsTracker;

public class KahnTopologicalSort {

    public static List<Integer> sort(List<List<Integer>> adj, MetricsTracker m) {
        m.start();

        int n = adj.size();
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) indeg[v]++;
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.remove();
            m.incTopo();
            order.add(u);
            for (int v : adj.get(u)) {
                if (--indeg[v] == 0) q.add(v);
            }
        }

        m.stop();

        if (order.size() != n)
            throw new IllegalStateException("Graph has a cycle");

        return order;
    }
}
