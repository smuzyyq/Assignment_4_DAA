
import graphs.topo.KahnTopologicalSort;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class KahnTopologicalSortTest {

    @Test
    void testTopoSort() {
        List<List<Integer>> adj = List.of(
                List.of(1, 2),
                List.of(3),
                List.of(3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var order = KahnTopologicalSort.sort(adj, m);

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(3) > order.indexOf(1));
        assertTrue(order.indexOf(3) > order.indexOf(2));
        assertTrue(m.getTopoOps() > 0);
    }

    @Test
    void testCycleThrows() {
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(0)
        );
        MetricsTracker m = new MetricsTracker();
        assertThrows(IllegalStateException.class, () -> KahnTopologicalSort.sort(adj, m));
    }
}
