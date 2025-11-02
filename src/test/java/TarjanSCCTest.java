import graphs.scc.TarjanSCC;
import metrics.MetricsTracker;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TarjanSCCTest {

    @Test
    void testSimpleGraph() {
        List<List<Integer>> adj = List.of(
                List.of(1),
                List.of(2),
                List.of(0, 3),
                List.of()
        );
        MetricsTracker m = new MetricsTracker();
        var scc = new TarjanSCC(adj).run(m);


        assertEquals(2, scc.size());
        assertTrue(scc.stream().anyMatch(c -> c.contains(3)));
        assertTrue(m.getDfsOps() > 0);
    }
}
