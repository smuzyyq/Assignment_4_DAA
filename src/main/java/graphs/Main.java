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
import java.util.Locale;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        if (!Files.exists(dataDir)) {
            System.out.println("data/ not found");
            return;
        }

        // Удаляем старый metrics.csv
        Path csv = dataDir.resolve("metrics.csv");
        Files.deleteIfExists(csv);

        List<String> inputs = List.of("small.json", "medium.json", "large.json");
        for (String name : inputs) {
            Path in = dataDir.resolve(name);
            if (Files.exists(in)) {
                processOneFile(in, csv);
            }
        }

        System.out.println("✅ Metrics saved to data/metrics.csv");
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

            //SCC
            var scc = new TarjanSCC(g.adj()).run(m);
            gRes.put("sccCount", scc.size());
            gRes.put("scc", scc);

            //Topological sort + DAG
            try {
                var topo = KahnTopologicalSort.sort(g.adj(), m);
                gRes.put("topoOrder", topo);
                gRes.put("isDAG_detected", true);

                double[] dist = DagShortestPaths.shortest(g.weightedAdj(), topo, 0, m);
                List<Double> distList = new ArrayList<>(dist.length);
                for (double d : dist) distList.add(d);
                gRes.put("dagspFrom0", distList);
            } catch (IllegalStateException e) {
                gRes.put("topoOrder", "cycle");
                gRes.put("isDAG_detected", false);
                gRes.put("dagspFrom0", "skipped");
            }

            //Metrics
            gRes.put("elapsedMs", m.getElapsedMs());
            gRes.put("dfsOps", m.getDfsOps());
            gRes.put("topoOps", m.getTopoOps());
            gRes.put("relaxOps", m.getRelaxOps());

            //Metrics to CSV
            boolean exists = Files.exists(csv);
            try (var writer = Files.newBufferedWriter(csv,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (!exists)
                    writer.write("inputFile,graphId,elapsedMs,dfsOps,topoOps,relaxOps\n");
                writer.write(String.format(Locale.US, "%s,%s,%.3f,%d,%d,%d%n",
                        inputFile.getFileName(), g.id(),
                        m.getElapsedMs(), m.getDfsOps(), m.getTopoOps(), m.getRelaxOps()));
            }

            outGraphs.add(gRes);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("inputFile", inputFile.getFileName().toString());
        root.put("graphs", outGraphs);

        String outName = inputFile.getFileName().toString().replace(".json", "-output.json");
        Path outFile = inputFile.getParent().resolve(outName);

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outFile.toFile(), root);
    }
}
