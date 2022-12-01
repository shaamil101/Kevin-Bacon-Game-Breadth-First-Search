import java.util.*;

public class BFS<V,E>  {


    /**
     * Generates minimum path tree from a source to every connected vertex
     * @param g full graph
     * @param source root node for the tree
     * @return tree of minimum paths
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V,E> Graph<V,E> bfs(Graph<V, E> g, V source)  {
        Graph<V, E> tree = new AdjacencyMapGraph<>();
        tree.insertVertex(source);
        Set<V> visited = new HashSet<V>();
        Queue<V> queue = new LinkedList<V>();

        queue.add(source);
        visited.add(source);
        while (!queue.isEmpty()) {
            V u = queue.remove();
            for (V v : g.outNeighbors(u)) {
                // for every new vertex, add it to visited, the queue, and the tree
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.add(v); 
                    tree.insertVertex(v);
                    tree.insertDirected(u, v, g.getLabel(u, v));
                }
            }
        }

        return tree;
    }

    /**
     * Get a path from a node to the root
     * @param tree tree from bfs method
     * @param v vertex to look the path for
     * @return list representing the path
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v)
    {
        if(!tree.hasVertex(v)||tree.numVertices()==0) {
            return new ArrayList<V>();
        }
        ArrayList<V> path = new ArrayList<>();
        V current = v;
        // walk up the tree to the root
        while(current!=null)
        {
            path.add(current);
            if (tree.inDegree(current) == 0) break;
            current = tree.inNeighbors(current).iterator().next();

        }
        return path;
    }

    /**
     * Returns the set of vertices in the graph not present in the subgraph
     * @param graph full graph
     * @param subgraph
     * @return set of vertices missing
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        Set<V> missing = new HashSet<V>();
        for(V temp :graph.vertices())
        {
            if(!subgraph.hasVertex(temp))
                missing.add(temp);
        }
        return missing;
    }

    /**
     * Helper function for the recursion
     * @param tree minimum path tree
     * @param root root of the tree
     * @param d depth, starts at 0
     * @return sum of separations
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V, E> int totalSeparation(Graph<V, E> tree, V root, int d) {
        int total = d;
        for (V temp : tree.outNeighbors(root)) {
            total += totalSeparation(tree, temp, d + 1);
        }


        return total;
    }

    /**
     * Returns average separation of all vertices to root
     * @param tree tree of minimum paths
     * @param root root of the tree
     * @return average separation to the root
     * @param <V> vertex type
     * @param <E> edge type
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        double avgSeparation = (double)totalSeparation(tree, root, 0) / tree.numVertices();
        return avgSeparation;
    }

}