package graphs.topo;

import java.util.*;
import metrics.MetricsTracker;

/**
 * Kahn's algorithm for topological sorting of a directed acyclic graph (DAG).
 *
 * Time complexity: O(V + E)
 * Space complexity: O(V)
 *
 * The algorithm maintains in-degrees for all vertices and processes vertices
 * with zero in-degree in queue order. When a vertex is removed, its neighbors'
 * in-degrees are decremented. If all vertices are processed, the graph is a DAG;
 * otherwise, a cycle exists.
 */
public class KahnTopologicalSort {

    /**
     * Computes a topological ordering of the given directed graph.
     * Uses a priority queue to ensure deterministic output when multiple valid orders exist.
     *
     * @param adj Adjacency list representation of the directed graph.
     * @param m MetricsTracker to record queue operations, edge explorations, and execution time.
     * @return List of vertex indices in topological order.
     * @throws IllegalArgumentException if adj is null or empty.
     * @throws IllegalStateException if the graph contains a cycle (not a DAG).
     */
    public static List<Integer> sort(List<List<Integer>> adj, MetricsTracker m) {
        if (adj == null || adj.isEmpty()) {
            throw new IllegalArgumentException("Graph adjacency list cannot be null or empty");
        }

        m.start();
        int n = adj.size();
        int[] indeg = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) {
                indeg[v]++;
            }
        }

        // Use PriorityQueue for deterministic order
        Queue<Integer> q = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.remove();
            m.incTopo(); // Track queue removal
            order.add(u);

            for (int v : adj.get(u)) {
                m.incEdge(); // Track edge exploration
                if (--indeg[v] == 0) {
                    q.add(v);
                }
            }
        }
        m.stop();

        if (order.size() != n) {
            throw new IllegalStateException("Graph has a cycle (not a DAG)");
        }
        return order;
    }
}
