package JSONReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphs.dagsp.DagShortestPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON parser for graph datasets in Assignment 4 format.
 *
 * Supported JSON formats:
 * 1. Single graph: { "id": "...", "nodes": N, "edges": [...], ... }
 * 2. Multiple graphs: { "graphs": [ {graph1}, {graph2}, ... ] }
 *
 * Required fields:
 * - "nodes": integer, number of vertices (0-indexed: 0 to N-1)
 * - "edges": array of objects, each with "from" and "to" (required), "w" (optional weight, default 1.0)
 *
 * Optional fields:
 * - "id": string identifier
 * - "edgesCount": integer (computed if absent)
 * - "density": double (graph density metric)
 * - "isDAG": boolean (true if acyclic)
 * - "source": integer (source vertex for shortest path queries, default 0)
 *
 * Weight model: Weights are assigned to edges (via "w" field), not vertices.
 */
public final class JsonGraphReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonGraphReader() { }

    /**
     * Reads one or more graphs from a JSON file.
     * @param file Path to JSON file in data/ directory.
     * @return List of parsed graphs (even if file contains a single graph).
     * @throws IOException if file cannot be read or JSON is malformed.
     */
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
        if (!g.has("nodes") || !g.get("nodes").isInt()) {
            throw new IllegalArgumentException("Missing or invalid 'nodes' field");
        }
        if (!g.has("edges") || !g.get("edges").isArray()) {
            throw new IllegalArgumentException("Missing or invalid 'edges' array");
        }

        String id = g.has("id") ? g.get("id").asText() : "no-id";
        int n = g.get("nodes").asInt();
        Integer edgesCount = g.has("edgesCount") ? g.get("edgesCount").asInt() : null;
        String density = g.has("density") ? g.get("density").asText() : null;
        Boolean isDag = g.has("isDAG") ? g.get("isDAG").asBoolean() : null;
        Integer source = g.has("source") ? g.get("source").asInt() : null;

        List<List<Integer>> adj = new ArrayList<>(n);
        List<List<DagShortestPaths.Edge>> wadj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            wadj.add(new ArrayList<>());
        }

        int actualEdges = 0;
        for (JsonNode e : g.withArray("edges")) {
            if (!e.has("from") || !e.has("to")) {
                throw new IllegalArgumentException("Edge missing 'from' or 'to' field");
            }
            int from = e.get("from").asInt();
            int to = e.get("to").asInt();

            // Validate edge endpoints
            if (from < 0 || from >= n || to < 0 || to >= n) {
                throw new IllegalArgumentException(
                        String.format("Edge endpoint out of range: %d -> %d (graph has %d nodes)", from, to, n));
            }

            double w = e.has("w") ? e.get("w").asDouble() : 1.0;
            adj.get(from).add(to);
            wadj.get(from).add(new DagShortestPaths.Edge(to, w));
            actualEdges++;
        }

        if (edgesCount == null) {
            edgesCount = actualEdges;
        }

        return new SingleGraph(id, n, adj, wadj, edgesCount, density, isDag, source);
    }

    /**
     * Immutable record holding parsed graph data.
     *
     * @param id Graph identifier
     * @param nodes Number of vertices
     * @param adj Unweighted adjacency list (for SCC, topological sort)
     * @param weightedAdj Weighted adjacency list (for DAG shortest/longest paths)
     * @param edgesCount Total number of edges
     * @param density Graph density metric (optional)
     * @param isDag True if graph is known to be acyclic (optional)
     * @param source Source vertex for path queries (optional, defaults to 0 if null)
     */
    public record SingleGraph(
            String id,
            int nodes,
            List<List<Integer>> adj,
            List<List<DagShortestPaths.Edge>> weightedAdj,
            Integer edgesCount,
            String density,
            Boolean isDag,
            Integer source
    ) { }
}
