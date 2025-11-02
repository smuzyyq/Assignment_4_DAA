package metrics;

/**
 * Lightweight metrics collector for Assignment 4: SCC, Topological Sort, DAG Shortest Paths.
 * Tracks wall-clock time (in nanoseconds, converted to milliseconds) and operation counters.
 *
 * Counters semantics:
 * - dfsOps: number of vertex visits during DFS (Tarjan's SCC)
 * - edgeOps: number of edges explored across all algorithms
 * - topoOps: number of queue removals in Kahn's topological sort
 * - relaxOps: number of successful distance updates in DAG shortest/longest paths
 */
public final class MetricsTracker {
    private long startNs;
    private long elapsedNs;
    private long dfsOps;
    private long edgeOps;
    private long topoOps;
    private long relaxOps;

    /**
     * Starts the timer for this metric collection session.
     */
    public void start() {
        startNs = System.nanoTime();
    }

    /**
     * Stops the timer and records elapsed time.
     */
    public void stop() {
        elapsedNs = System.nanoTime() - startNs;
    }

    /**
     * Resets all counters and elapsed time to zero.
     * Useful for reusing the same tracker instance across multiple experiments.
     */
    public void reset() {
        elapsedNs = dfsOps = edgeOps = topoOps = relaxOps = 0L;
    }

    /**
     * @return Elapsed time in milliseconds (with sub-millisecond precision).
     */
    public double getElapsedMs() {
        return elapsedNs / 1_000_000.0;
    }

    // Operation counters
    public void incDfs()   { dfsOps++; }
    public void incEdge()  { edgeOps++; }
    public void incTopo()  { topoOps++; }
    public void incRelax() { relaxOps++; }

    // Getters
    public long getDfsOps()   { return dfsOps; }
    public long getEdgeOps()  { return edgeOps; }
    public long getTopoOps()  { return topoOps; }
    public long getRelaxOps() { return relaxOps; }
}
