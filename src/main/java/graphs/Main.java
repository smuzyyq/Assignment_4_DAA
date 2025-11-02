package graphs;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphs.dagsp.DagShortestPaths;
import graphs.scc.TarjanSCC;
import graphs.topo.KahnTopologicalSort;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");
        if (!Files.exists(dataDir)) {
            System.out.println("data/ not found");
            return;
        }

        // обрабатываем только наши три
        List<String> inputs = List.of("small.json", "medium.json", "large.json");

        for (String name : inputs) {
            Path in = dataDir.resolve(name);
            if (Files.exists(in)) {
                processOneFile(in);
            }
        }
    }

    private static void processOneFile(Path inputFile) throws IOException {
        List<JsonGraphReader.SingleGraph> graphs = JsonGraphReader.readMany(inputFile);

        List<Map<String, Object>> outGraphs = new ArrayList<>();

        for (JsonGraphReader.SingleGraph g : graphs) {
            Map<String, Object> gRes = new LinkedHashMap<>();
            gRes.put("id", g.id());
            gRes.put("nodes", g.nodes());
            gRes.put("edgesCount", g.edgesCount());
            if (g.density() != null) gRes.put("density", g.density());
            if (g.isDag() != null) gRes.put("isDAG_input", g.isDag());

            // SCC
            var scc = TarjanSCC.run(g.adj());
            gRes.put("sccCount", scc.size());
            gRes.put("scc", scc);

            // topo + dagsp
            try {
                var topo = KahnTopologicalSort.sort(g.adj());
                gRes.put("topoOrder", topo);
                gRes.put("isDAG_detected", true);

                double[] dist = DagShortestPaths.shortest(g.weightedAdj(), topo, 0);
                List<Double> distList = new ArrayList<>(dist.length);
                for (double d : dist) distList.add(d);
                gRes.put("dagspFrom0", distList);
            } catch (IllegalStateException e) {
                gRes.put("topoOrder", "cycle");
                gRes.put("isDAG_detected", false);
                gRes.put("dagspFrom0", "skipped");
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
