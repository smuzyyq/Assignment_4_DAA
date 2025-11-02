import graphs.dagsp.DagShortestPaths;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DAG shortest and longest path algorithms.
 * Tests cover: shortest paths, longest paths, path reconstruction, edge cases.
 */
public class DagShortestPathsTest {

    @Test
    void testShortestPath() {
        // DAG: 0→1(1), 0→2(4), 1→2(2), 1→3(6), 2→3(3)
        List<List<DagShortestPaths.Edge>> adj = List.of(
                List.of(new DagShortestPaths.Edge(1, 1), new DagShortestPaths.Edge(2, 4)),
                List.of(new DagShortestPaths.Edge(2, 2), new DagShortestPaths.Edge(3, 6)),
                List.of(new DagShortestPaths.Edge(3, 3)),
                List.of()
        );
        List<Integer> topo = List.of(0, 1, 2, 3);
        MetricsTracker m = new MetricsTracker();

        double[] dist = DagShortestPaths.shortest(adj, topo, 0, m);

        assertArrayEquals(new double[]{0.0, 1.0, 3.0, 6.0}, dist, 1e-9,
                "Shortest paths should be [0, 1, 3, 6]");
        assertTrue(m.getRelaxOps() > 0, "Should track relaxation operations");
    }

    @Test
    void testLongestPath() {
        // DAG: 0→1(2), 0→2(3), 1→3(4), 2→3(1)
        // Longest path from 0: 0→1→3 = 2+4 = 6
        List<List<DagShortestPaths.Edge>> adj = List.of(
                List.of(new DagShortestPaths.Edge(1, 2), new DagShortestPaths.Edge(2, 3)),
                List.of(new DagShortestPaths.Edge(3, 4)),
                List.of(new DagShortestPaths.Edge(3, 1)),
                List.of()
        );
        List<Integer> topo = List.of(0, 1, 2, 3);
        MetricsTracker m = new MetricsTracker();
        int[] parent = new int[4];

        double[] dist = DagShortestPaths.longest(adj, topo, 0, m, parent);

        assertArrayEquals(new double[]{0.0, 2.0, 3.0, 6.0}, dist, 1e-9,
                "Longest paths should be [0, 2, 3, 6]");
        assertEquals(1, parent[3], "Parent of node 3 should be 1 (critical path)");
        assertTrue(m.getRelaxOps() > 0, "Should track relaxations");
    }

    @Test
    void testPathReconstruction() {
        // DAG: 0→1(1), 1→2(1), 2→3(1)
        List<List<DagShortestPaths.Edge>> adj = List.of(
                List.of(new DagShortestPaths.Edge(1, 1)),
                List.of(new DagShortestPaths.Edge(2, 1)),
                List.of(new DagShortestPaths.Edge(3, 1)),
                List.of()
        );
        List<Integer> topo = List.of(0, 1, 2, 3);
        MetricsTracker m = new MetricsTracker();
        int[] parent = new int[4];

        DagShortestPaths.longest(adj, topo, 0, m, parent);
        List<Integer> path = DagShortestPaths.reconstructPath(parent, 3);

        assertEquals(List.of(0, 1, 2, 3), path,
                "Path from 0 to 3 should be [0, 1, 2, 3]");
    }

    @Test
    void testUnreachableVertex() {
        // DAG: 0→1(1), 2→3(1) (0,1 disconnected from 2,3)
        List<List<DagShortestPaths.Edge>> adj = List.of(
                List.of(new DagShortestPaths.Edge(1, 1)),
                List.of(),
                List.of(new DagShortestPaths.Edge(3, 1)),
                List.of()
        );
        List<Integer> topo = List.of(0, 1, 2, 3);
        MetricsTracker m = new MetricsTracker();

        double[] dist = DagShortestPaths.shortest(adj, topo, 0, m);

        assertEquals(0.0, dist[0], "Source distance should be 0");
        assertEquals(1.0, dist[1], "Node 1 reachable");
        assertEquals(Double.POSITIVE_INFINITY, dist[2], "Node 2 unreachable from 0");
        assertEquals(Double.POSITIVE_INFINITY, dist[3], "Node 3 unreachable from 0");
    }

    @Test
    void testNegativeWeights() {
        // DAG: 0→1(-2), 1→2(5), 0→2(1)
        // Shortest to 2: 0→1→2 = -2+5 = 3 (not direct 1)
        List<List<DagShortestPaths.Edge>> adj = List.of(
                List.of(new DagShortestPaths.Edge(1, -2), new DagShortestPaths.Edge(2, 1)),
                List.of(new DagShortestPaths.Edge(2, 5)),
                List.of()
        );
        List<Integer> topo = List.of(0, 1, 2);
        MetricsTracker m = new MetricsTracker();

        double[] dist = DagShortestPaths.shortest(adj, topo, 0, m);

        assertArrayEquals(new double[]{0.0, -2.0, 1.0}, dist, 1e-9,
                "Should handle negative weights correctly");
    }

    @Test
    void testSingleNode() {
        List<List<DagShortestPaths.Edge>> adj = List.of(List.of());
        List<Integer> topo = List.of(0);
        MetricsTracker m = new MetricsTracker();

        double[] dist = DagShortestPaths.shortest(adj, topo, 0, m);

        assertArrayEquals(new double[]{0.0}, dist, 1e-9, "Single node distance should be 0");
    }

    @Test
    void testInvalidSourceThrows() {
        List<List<DagShortestPaths.Edge>> adj = List.of(List.of(), List.of());
        List<Integer> topo = List.of(0, 1);
        MetricsTracker m = new MetricsTracker();

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPaths.shortest(adj, topo, -1, m),
                "Negative source should throw");
        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPaths.shortest(adj, topo, 5, m),
                "Out of bounds source should throw");
    }
}
