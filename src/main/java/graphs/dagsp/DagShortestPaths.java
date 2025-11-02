package graphs.dagsp;

import java.util.*;
import metrics.MetricsTracker;

/**
 * Shortest and longest path algorithms for DAGs.
 * Added: shortest(...) with parent[] to reconstruct one optimal path.
 */
public class DagShortestPaths {

    public static class Edge {
        public final int to;
        public final double weight;
        public Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    /**
     * Old API kept for compatibility.
     */
    public static double[] shortest(List<List<Edge>> adj, List<Integer> topo, int src, MetricsTracker m) {
        return shortest(adj, topo, src, m, null);
    }

    /**
     * New API: also fills parent[] if not null.
     */
    public static double[] shortest(List<List<Edge>> adj,
                                    List<Integer> topo,
                                    int src,
                                    MetricsTracker m,
                                    int[] parent) {
        validateInputs(adj, topo, src);
        if (parent != null && parent.length != adj.size()) {
            throw new IllegalArgumentException("parent length must equal number of vertices");
        }

        m.start();
        int n = adj.size();
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[src] = 0.0;
        if (parent != null) {
            Arrays.fill(parent, -1);
        }

        for (int u : topo) {
            if (dist[u] == Double.POSITIVE_INFINITY) continue;
            for (Edge e : adj.get(u)) {
                int v = e.to;
                double w = e.weight;
                double cand = dist[u] + w;
                if (cand < dist[v]) {
                    dist[v] = cand;
                    if (parent != null) {
                        parent[v] = u;
                    }
                    m.incRelax();
                }
            }
        }
        m.stop();
        return dist;
    }

    public static double[] longest(List<List<Edge>> adj,
                                   List<Integer> topo,
                                   int src,
                                   MetricsTracker m,
                                   int[] parent) {
        validateInputs(adj, topo, src);
        if (parent == null || parent.length != adj.size()) {
            throw new IllegalArgumentException("Parent array must have length equal to number of vertices");
        }

        m.start();
        int n = adj.size();
        double[] dist = new double[n];
        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        Arrays.fill(parent, -1);
        dist[src] = 0.0;

        for (int u : topo) {
            if (dist[u] == Double.NEGATIVE_INFINITY) continue;
            for (Edge e : adj.get(u)) {
                int v = e.to;
                double w = e.weight;
                double cand = dist[u] + w;
                if (cand > dist[v]) {
                    dist[v] = cand;
                    parent[v] = u;
                    m.incRelax();
                }
            }
        }
        m.stop();
        return dist;
    }

    public static List<Integer> reconstructPath(int[] parent, int dest) {
        if (parent == null || dest < 0 || dest >= parent.length) {
            throw new IllegalArgumentException("Invalid parent or dest");
        }
        List<Integer> path = new ArrayList<>();
        // if dest unreachable and not the source
        if (parent[dest] == -1 && findSource(parent) != dest) {
            return path;
        }
        for (int at = dest; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    private static int findSource(int[] parent) {
        for (int i = 0; i < parent.length; i++) {
            if (parent[i] == -1) return i;
        }
        return 0;
    }

    private static void validateInputs(List<List<Edge>> adj, List<Integer> topo, int src) {
        if (adj == null || adj.isEmpty()) {
            throw new IllegalArgumentException("adj is null/empty");
        }
        if (topo == null || topo.isEmpty()) {
            throw new IllegalArgumentException("topo is null/empty");
        }
        if (src < 0 || src >= adj.size()) {
            throw new IllegalArgumentException("src out of range");
        }
    }
}
