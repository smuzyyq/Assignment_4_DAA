package graphs.dagsp;

import java.util.*;
import metrics.MetricsTracker;

public class DagShortestPaths {

    public static class Edge {
        public final int to;
        public final double weight;

        public Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public static double[] shortest(List<List<Edge>> adj, List<Integer> topo, int src, MetricsTracker m) {
        m.start();
        int n = adj.size();
        double[] dist = new double[n];
        Arrays.fill(dist, 1e18);
        dist[src] = 0.0;

        for (int u : topo) {
            for (Edge e : adj.get(u)) {
                int v = e.to;
                double w = e.weight;
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    m.incRelax();
                }
            }
        }

        m.stop();
        return dist;
    }
}
