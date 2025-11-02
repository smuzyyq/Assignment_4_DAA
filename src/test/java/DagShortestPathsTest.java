import graphs.dagsp.DagShortestPaths;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertArrayEquals(new double[]{0.0, 1.0, 3.0, 6.0}, dist, 1e-9);
        assertTrue(m.getRelaxOps() > 0);
    }
}
