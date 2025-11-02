package graphs.topo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class KahnTopologicalSort {

    private KahnTopologicalSort() {
    }

    public static List<Integer> sort(List<List<Integer>> adj) {
        int n = adj.size();
        int[] indeg = new int[n];

        for (int v = 0; v < n; v++) {
            for (int to : adj.get(v)) {
                indeg[to]++;
            }
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                q.add(v);
            }
        }

        List<Integer> order = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int v = q.poll();
            order.add(v);
            for (int to : adj.get(v)) {
                if (--indeg[to] == 0) {
                    q.add(to);
                }
            }
        }

        if (order.size() != n) {
            throw new IllegalStateException("graph has cycle, topo order not possible");
        }

        return order;
    }
}
