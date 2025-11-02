import graphs.scc.TarjanSCC;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tarjan's SCC algorithm.
 * Tests cover: basic cycles, single SCC, multiple SCCs, DAG (no cycles), edge cases.
 */
public class TarjanSCCTest {

    @Test
    void testSimpleGraphWithCycle() {
        // Graph: 0→1→2→0 (cycle), 3 isolated
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(0, 3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);

        assertEquals(2, scc.size(), "Should have 2 SCCs");
        assertTrue(scc.stream().anyMatch(c -> c.size() == 3), "One SCC should have 3 nodes");
        assertTrue(scc.stream().anyMatch(c -> c.size() == 1 && c.contains(3)), "Node 3 should be isolated");
        assertTrue(m.getDfsOps() > 0, "DFS operations should be tracked");
        assertTrue(m.getEdgeOps() > 0, "Edge operations should be tracked");
    }

    @Test
    void testSingleStronglyConnectedComponent() {
        // Graph: 0→1→2→0, all in one SCC
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(0)
        );
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);

        assertEquals(1, scc.size(), "Should have 1 SCC");
        assertEquals(3, scc.get(0).size(), "SCC should contain all 3 nodes");
        assertEquals(3, m.getDfsOps(), "Should visit all 3 nodes");
    }

    @Test
    void testDAGNoStrongComponents() {
        // Graph: 0→1→2→3 (chain, no cycles)
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);

        assertEquals(4, scc.size(), "DAG should have 4 trivial SCCs");
        assertTrue(scc.stream().allMatch(c -> c.size() == 1), "Each SCC should have 1 node");
    }

    @Test
    void testSingleNodeGraph() {
        // Graph: single node with no edges
        List<List<Integer>> adj = List.of(List.of());
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);

        assertEquals(1, scc.size(), "Should have 1 SCC");
        assertEquals(1, scc.get(0).size(), "SCC should contain the single node");
        assertEquals(1, m.getDfsOps(), "Should visit 1 node");
    }

    @Test
    void testMultipleSeparateComponents() {
        // Graph: 0→1, 2→3 (two separate chains)
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);

        assertEquals(4, scc.size(), "Should have 4 trivial SCCs");
        assertEquals(4, m.getDfsOps(), "Should visit all 4 nodes");
    }

    @Test
    void testNullGraphThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TarjanSCC(null));
    }

    @Test
    void testEmptyGraphThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TarjanSCC(List.of()));
    }
}
