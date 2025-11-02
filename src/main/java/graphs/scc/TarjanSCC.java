package graphs.scc;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class TarjanSCC {

    private TarjanSCC() {
    }
    public static List<List<Integer>> run(List<List<Integer>> adj) {
        int n = adj.size();
        int[] index = new int[n];
        int[] low = new int[n];
        boolean[] onStack = new boolean[n];
        Stack<Integer> stack = new Stack<>();
        List<List<Integer>> comps = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            index[i] = -1;
        }

        int[] time = {0};

        for (int v = 0; v < n; v++) {
            if (index[v] == -1) {
                dfs(v, adj, index, low, onStack, stack, comps, time);
            }
        }
        return comps;
    }

    private static void dfs(
            int v,
            List<List<Integer>> adj,
            int[] index,
            int[] low,
            boolean[] onStack,
            Stack<Integer> stack,
            List<List<Integer>> comps,
            int[] time
    ) {
        index[v] = low[v] = time[0]++;
        stack.push(v);
        onStack[v] = true;

        for (int to : adj.get(v)) {
            if (index[to] == -1) {
                dfs(to, adj, index, low, onStack, stack, comps, time);
                low[v] = Math.min(low[v], low[to]);
            } else if (onStack[to]) {
                low[v] = Math.min(low[v], index[to]);
            }
        }

        if (low[v] == index[v]) {
            List<Integer> comp = new ArrayList<>();
            int x;
            do {
                x = stack.pop();
                onStack[x] = false;
                comp.add(x);
            } while (x != v);
            comps.add(comp);
        }
    }
}
