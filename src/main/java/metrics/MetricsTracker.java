package metrics;

public final class MetricsTracker {

    private long startNs;
    private long elapsedNs;

    private long dfsOps;
    private long topoOps;
    private long relaxOps;

    public void start() {
        startNs = System.nanoTime();
    }

    public void stop() {
        elapsedNs = System.nanoTime() - startNs;
    }

    public double getElapsedMs() {
        return elapsedNs / 1_000_000.0;
    }

    public void incDfs() {
        dfsOps++;
    }

    public void incTopo() {
        topoOps++;
    }

    public void incRelax() {
        relaxOps++;
    }

    public long getDfsOps() {
        return dfsOps;
    }

    public long getTopoOps() {
        return topoOps;
    }

    public long getRelaxOps() {
        return relaxOps;
    }
}
