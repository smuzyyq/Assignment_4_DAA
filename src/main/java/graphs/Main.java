package graphs;

import JSONReader.JsonGraphReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphs.dagsp.DagShortestPaths;
import graphs.scc.TarjanSCC;
import graphs.topo.KahnTopologicalSort;
import metrics.MetricsTracker;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Main entry point for Assignment 4.
 * Now:
 *  1) always does SCC
 *  2) builds condensation DAG
 *  3) topo + DAG-SP run on condensation, so cycles do not break pipeline
 */
public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        if (!Files.exists(dataDir)) {
            System.out.println("Error: data/ directory not found");
            return;
        }

        Path csv = dataDir.resolve("metrics.csv");
        Files.deleteIfExists(csv);

        List<String> inputs = List.of("small.json", "medium.json", "large.json");
        for (String name : inputs) {
            Path in = dataDir.resolve(name);
            if (Files.exists(in)) {
                processOneFile(in, csv);
            } else {
                System.out.println("Skipping missing file: " + name);
            }
        }
        System.out.println("Metrics saved to data/metrics.csv");
        System.out.println("Results saved to data/*-output.json");
    }

    private static void processOneFile(Path inputFile, Path csv) throws IOException {
        List<JsonGraphReader.SingleGraph> graphs = JsonGraphReader.readMany(inputFile);
        List<Map<String, Object>> outGraphs = new ArrayList<>();

        for (JsonGraphReader.SingleGraph g : graphs) {
            MetricsTracker m = new MetricsTracker();
            Map<String, Object> gRes = new LinkedHashMap<>();

            gRes.put("id", g.id());
            gRes.put("nodes", g.nodes());
            gRes.put("edgesCount", g.edgesCount());
            if (g.density() != null) gRes.put("density", g.density());
            if (g.isDag() != null) gRes.put("isDAG_input", g.isDag());

            // 1) SCC
            TarjanSCC tarjan = new TarjanSCC(g.adj());
            List<List<Integer>> scc = tarjan.run(m);
            gRes.put("sccCount", scc.size());
            gRes.put("scc", scc);

            int[] compId = tarjan.getComponentIds();
            List<List<Integer>> condAdj = tarjan.buildCondensation();
            gRes.put("condensationAdj", condAdj);

            // 2) topo over condensation DAG
            List<Integer> topoCond = KahnTopologicalSort.sort(condAdj, m);
            gRes.put("condensationTopo", topoCond);

            // derive order of original vertices according to SCC topo
            List<Integer> derivedOrder = new ArrayList<>();
            for (int cid : topoCond) {
                List<Integer> verts = new ArrayList<>(scc.get(cid));
                Collections.sort(verts);
                derivedOrder.addAll(verts);
            }
            gRes.put("derivedVertexOrder", derivedOrder);

            // 3) build weighted condensation and run DAG-SP on it
            List<List<DagShortestPaths.Edge>> weightedCond =
                    buildWeightedCondensation(g.weightedAdj(), compId, scc.size());

            int srcVertex = (g.source() != null) ? g.source() : 0;
            int srcComp = compId[srcVertex];
            gRes.put("sourceVertex", srcVertex);
            gRes.put("sourceComponent", srcComp);

            // shortest with parent
            int[] spParent = new int[weightedCond.size()];
            double[] spDist = DagShortestPaths.shortest(weightedCond, topoCond, srcComp, m, spParent);
            gRes.put("shortestFromComponent", toList(spDist));

            // reconstruct one shortest path: to last component in topo
            int targetComp = topoCond.get(topoCond.size() - 1);
            List<Integer> shortestPathCond = DagShortestPaths.reconstructPath(spParent, targetComp);
            gRes.put("shortestPathCondensed_src" + srcComp + "_to_" + targetComp, shortestPathCond);

            // longest (critical path) on condensation
            int[] lpParent = new int[weightedCond.size()];
            double[] lpDist = DagShortestPaths.longest(weightedCond, topoCond, srcComp, m, lpParent);
            gRes.put("criticalPathDistances", toList(lpDist));

            int best = 0;
            for (int i = 1; i < lpDist.length; i++) {
                if (lpDist[i] > lpDist[best]) best = i;
            }
            List<Integer> criticalPathCond = DagShortestPaths.reconstructPath(lpParent, best);
            gRes.put("criticalPathCondensed", criticalPathCond);
            gRes.put("criticalPathLength", lpDist[best]);

            // metrics
            gRes.put("elapsedMs", m.getElapsedMs());
            gRes.put("dfsOps", m.getDfsOps());
            gRes.put("edgeOps", m.getEdgeOps());
            gRes.put("topoOps", m.getTopoOps());
            gRes.put("relaxOps", m.getRelaxOps());

            // CSV
            appendMetricsToCsv(csv, inputFile.getFileName().toString(), g.id(), m);

            outGraphs.add(gRes);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("inputFile", inputFile.getFileName().toString());
        root.put("graphs", outGraphs);

        String outName = inputFile.getFileName().toString().replace(".json", "-output.json");
        Path outFile = inputFile.getParent().resolve(outName);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outFile.toFile(), root);
    }

    private static List<List<DagShortestPaths.Edge>> buildWeightedCondensation(
            List<List<DagShortestPaths.Edge>> wAdj,
            int[] compId,
            int compCount
    ) {
        List<Set<Integer>> seen = new ArrayList<>(compCount);
        List<List<DagShortestPaths.Edge>> res = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) {
            seen.add(new HashSet<>());
            res.add(new ArrayList<>());
        }
        for (int u = 0; u < wAdj.size(); u++) {
            int cu = compId[u];
            for (DagShortestPaths.Edge e : wAdj.get(u)) {
                int cv = compId[e.to];
                if (cu == cv) continue;
                // avoid duplicates
                if (seen.get(cu).add(cv)) {
                    res.get(cu).add(new DagShortestPaths.Edge(cv, e.weight));
                }
            }
        }
        return res;
    }

    private static void appendMetricsToCsv(Path csv, String inputFile, String graphId,
                                           MetricsTracker m) throws IOException {
        boolean exists = Files.exists(csv);
        try (var writer = Files.newBufferedWriter(csv,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            if (!exists) {
                writer.write("inputFile,graphId,elapsedMs,dfsOps,edgeOps,topoOps,relaxOps\n");
            }
            writer.write(String.format(Locale.US, "%s,%s,%.3f,%d,%d,%d,%d%n",
                    inputFile, graphId,
                    m.getElapsedMs(), m.getDfsOps(), m.getEdgeOps(),
                    m.getTopoOps(), m.getRelaxOps()));
        }
    }

    private static List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>(arr.length);
        for (double d : arr) {
            list.add(d);
        }
        return list;
    }
}
