package graphs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphs.dagsp.DagShortestPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonGraphReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonGraphReader() { }

    public static List<SingleGraph> readMany(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        JsonNode root = MAPPER.readTree(bytes);

        List<SingleGraph> result = new ArrayList<>();

        if (root.has("graphs")) {
            for (JsonNode g : root.withArray("graphs")) {
                result.add(parseOne(g));
            }
        } else {
            result.add(parseOne(root));
        }

        return result;
    }

    private static SingleGraph parseOne(JsonNode g) {
        String id = g.has("id") ? g.get("id").asText() : "no-id";
        int n = g.get("nodes").asInt();

        Integer edgesCount = g.has("edgesCount") ? g.get("edgesCount").asInt() : null;
        String density = g.has("density") ? g.get("density").asText() : null;
        Boolean isDag = g.has("isDAG") ? g.get("isDAG").asBoolean() : null;

        List<List<Integer>> adj = new ArrayList<>(n);
        List<List<DagShortestPaths.Edge>> wadj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            wadj.add(new ArrayList<>());
        }

        int countedEdges = 0;
        for (JsonNode e : g.withArray("edges")) {
            int from = e.get("from").asInt();
            int to = e.get("to").asInt();
            int w = e.has("w") ? e.get("w").asInt() : 1;
            adj.get(from).add(to);
            wadj.get(from).add(new DagShortestPaths.Edge(to, w));
            countedEdges++;
        }

        if (edgesCount == null) {
            edgesCount = countedEdges;
        }

        return new SingleGraph(id, n, adj, wadj, edgesCount, density, isDag);
    }

    public record SingleGraph(
            String id,
            int nodes,
            List<List<Integer>> adj,
            List<List<DagShortestPaths.Edge>> weightedAdj,
            int edgesCount,
            String density,
            Boolean isDag
    ) { }
}
