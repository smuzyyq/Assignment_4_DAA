package graphs.dagsp;

import java.util.ArrayList;
import java.util.List;

public final class DagShortestPaths {

    private static final double INF = 1e18;

    private DagShortestPaths() {
    }

    /** Ребро DAG. */
    public static final class Edge {
        public final int to;
        public final int w;

        public Edge(int to, int w) {
            this.to = to;
            this.w = w;
        }
    }


    public static double[] shortest(List<List<Edge>> adj, List<Integer> topo, int src) {
        int n = adj.size();
        double[] dist = new double[n];
        for (int i = 0; i < n; i++) dist[i] = INF;
        dist[src] = 0.0;

        for (int v : topo) {
            if (dist[v] == INF) continue;
            for (Edge e : adj.get(v)) {
                double nd = dist[v] + e.w;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                }
            }
        }
        return dist;
    }


    public static double[] longest(List<List<Edge>> adj, List<Integer> topo, int src) {
        int n = adj.size();
        double[] dist = new double[n];
        for (int i = 0; i < n; i++) dist[i] = -INF;
        dist[src] = 0.0;

        for (int v : topo) {
            if (dist[v] == -INF) continue;
            for (Edge e : adj.get(v)) {
                double nd = dist[v] + e.w;
                if (nd > dist[e.to]) {
                    dist[e.to] = nd;
                }
            }
        }
        return dist;
    }


    public static List<List<Edge>> emptyAdj(int n) {
        List<List<Edge>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        return adj;
    }
}
