import graphs.topo.KahnTopologicalSort;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Kahn's topological sort algorithm.
 * Tests cover: valid DAGs, cycle detection, edge cases, metric tracking.
 */
public class KahnTopologicalSortTest {

    @Test
    void testBasicDAG() {
        // Graph: 0→1, 0→2, 1→3, 2→3
        List<List<Integer>> adj = List.of(
                List.of(1, 2),
                List.of(3),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var order = KahnTopologicalSort.sort(adj, m);

        assertEquals(4, order.size(), "Should have 4 vertices in order");
        assertTrue(order.indexOf(0) < order.indexOf(1), "0 should come before 1");
        assertTrue(order.indexOf(0) < order.indexOf(2), "0 should come before 2");
        assertTrue(order.indexOf(3) > order.indexOf(1), "3 should come after 1");
        assertTrue(order.indexOf(3) > order.indexOf(2), "3 should come after 2");
        assertEquals(4, m.getTopoOps(), "Should process all 4 nodes");
        assertTrue(m.getEdgeOps() > 0, "Should track edge operations");
    }

    @Test
    void testLinearChain() {
        // Graph: 0→1→2→3 (deterministic order)
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var order = KahnTopologicalSort.sort(adj, m);

        assertEquals(List.of(0, 1, 2, 3), order, "Linear chain should have deterministic order");
    }

    @Test
    void testCycleDetection() {
        // Graph: 0→1→2→0 (cycle)
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(0)
        );
        MetricsTracker m = new MetricsTracker();

        assertThrows(IllegalStateException.class,
                () -> KahnTopologicalSort.sort(adj, m),
                "Should throw exception for cyclic graph");
    }

    @Test
    void testSingleNode() {
        // Graph: single node
        List<List<Integer>> adj = List.of(List.of());
        MetricsTracker m = new MetricsTracker();
        var order = KahnTopologicalSort.sort(adj, m);

        assertEquals(List.of(0), order, "Single node should return [0]");
        assertEquals(1, m.getTopoOps(), "Should process 1 node");
    }

    @Test
    void testDisconnectedComponents() {
        // Graph: 0→1, 2→3 (two separate chains)
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var order = KahnTopologicalSort.sort(adj, m);

        assertEquals(4, order.size(), "Should have all 4 nodes");
        assertTrue(order.indexOf(0) < order.indexOf(1), "0 before 1");
        assertTrue(order.indexOf(2) < order.indexOf(3), "2 before 3");
    }

    @Test
    void testNullGraphThrows() {
        MetricsTracker m = new MetricsTracker();
        assertThrows(IllegalArgumentException.class,
                () -> KahnTopologicalSort.sort(null, m));
    }

    @Test
    void testEmptyGraphThrows() {
        MetricsTracker m = new MetricsTracker();
        assertThrows(IllegalArgumentException.class,
                () -> KahnTopologicalSort.sort(List.of(), m));
    }
}
