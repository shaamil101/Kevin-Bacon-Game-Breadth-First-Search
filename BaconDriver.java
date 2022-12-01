import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BaconDriver {
    public static Map<Integer, String> actorsById;
    public static Map<Integer, String> moviesById;
    public static Map<Integer, Set<Integer>> actorsByMovie;
    public static Graph<String, String> actorsGraph;

    // as they will hold references to the same objects, doing three lists doesn't make that much of a difference
    public static List<ActorProfile> actorProfiles;
    public static List<ActorProfile> actorProfilesBySeparation;
    public static List<ActorProfile> actorProfilesByCostars;

    // less space than Universe class for comparisons
    public static class ActorProfile {
        String name;
        int costars;
        double averageSeparation;

        public ActorProfile(String name) {
            Universe u = new Universe(name, false); // will get garbage collected
            costars = u.costars;
            averageSeparation = u.averageSeparation;
            this.name = name;
        }
    }

    public static class Universe {
        String center;
        Graph<String, String> tree;
        int costars;
        double averageSeparation;

        public Universe(String actor, boolean print){
            if (!actorsGraph.hasVertex(actor)) {
                System.out.println("Actor does not exist!");
                return;
            }
            this.center = actor;
            this.tree = BFS.bfs(actorsGraph, this.center);

            this.averageSeparation = BFS.averageSeparation(this.tree, this.center);
            this.costars = actorsGraph.inDegree(actor);
            if (print) System.out.printf("%s is now the center of the acting universe, connected to %d/%d actors, with average separation %f\n", this.center, this.tree.numVertices(), actorsGraph.numVertices(), averageSeparation);
        }

        /**
         * Print steps of distance and path to an actor from the center of the universe
         * @param actor
         */
        public void printPathToActor(String actor) {
            List<String> path = BFS.getPath(this.tree, actor);

            if (path.size() == 0) {
                System.out.printf("%s is not reachable from %s!\n", actor, this.center);
            }

            System.out.printf("%s's number is %d\n", actor, path.size() - 1);

            for (int i = 0; i < path.size() - 1; i++) {
                System.out.printf("%s appeared in [%s] with %s\n", path.get(i), actorsGraph.getLabel(path.get(i), path.get(i + 1)), path.get(i + 1));
            }
        }

        /**
         * Print the list of actors that are disconnected to center
         */
        public void printUnconnectedActors() {
            Set<String> unconnected = BFS.missingVertices(actorsGraph, this.tree);

            System.out.printf("The following actors can't be reached from %s:", this.center);
            System.out.println(String.join(", ", unconnected));
        }
    }

    /**
     * Loads dictionaries for ID to actor, ID to movie, and Movie to actors from files
     * @throws IOException
     */
    public static void loadDatasets() throws IOException {
        String actorsFilePath = "PS4/actors.txt";
        String moviesFilePath = "PS4/movies.txt";
        String movieActorsFilePath = "PS4/movie-actors.txt";

        actorsById = new HashMap<>();
        moviesById = new HashMap<>();
        actorsByMovie = new HashMap<>();

        String line;


        BufferedReader actorReader = new BufferedReader(new FileReader(actorsFilePath));
        while ((line = actorReader.readLine()) != null) {
            String [] parts = line.split("\\|");

            int id = Integer.parseInt(parts[0]);
            String actorName = parts[1];

            actorsById.put(id, actorName);
        }

        BufferedReader movieReader = new BufferedReader(new FileReader(moviesFilePath));
        while ((line = movieReader.readLine()) != null) {
            String [] parts = line.split("\\|");

            int id = Integer.parseInt(parts[0]);
            String movieName = parts[1];

            moviesById.put(id, movieName);
        }


        BufferedReader movieActorReaders = new BufferedReader(new FileReader(movieActorsFilePath));
        while ((line = movieActorReaders.readLine()) != null) {
            String [] parts = line.split("\\|");

            int movieId = Integer.parseInt(parts[0]);
            int actorId = Integer.parseInt(parts[1]);

            if (actorsByMovie.containsKey(movieId)) {
                actorsByMovie.get(movieId).add(actorId);
            } else {
                Set actorsSet = new HashSet();
                actorsSet.add(actorId);
                actorsByMovie.put(movieId, actorsSet);
            }
        }
    }

    /**
     * Builds a graph with every actor as vertices and movies as edges.
     */
    public static void buildGraph() {
        actorsGraph  = new AdjacencyMapGraph<>();

        for (String actor : actorsById.values()) {
            actorsGraph.insertVertex(actor);
        }

        for (Map.Entry<Integer, Set<Integer>> actorInMovie : actorsByMovie.entrySet()) {
           String movieName = moviesById.get(actorInMovie.getKey());

           // Find every pair of actors in the movie to add an edge. Note that this looks at (A, B) and (B, A), but
           //  as the insertUndirected adds both edges either way, the performance is the same as looking only at (A, B).
           for (Integer actor : actorInMovie.getValue()) {
               String actorName = actorsById.get(actor);

               for (Integer actor2 : actorInMovie.getValue()) {
                   if (actor == actor2) continue;

                   String actor2Name = actorsById.get(actor2);
                   actorsGraph.insertDirected(actorName, actor2Name, movieName);
               }

           }
        }
    }

    /**
     * Calculates average separation and number of costars for each actor. As normally one would look at both when
     * analyzing an actor, it makes sense to calculate them both at the same time.
     */
    public static void getActorProfiles() {
        actorProfiles = new ArrayList<>();
        int i = 0;
        for (String actor :  actorsById.values()) {
            ActorProfile ap = new ActorProfile(actor);
            if (ap.costars == 0) continue; // skip actors with no costars

            actorProfiles.add(ap);
            i++;
            if (i % 100 == 0) {
                System.out.printf("%d/%d...\n", i, actorsGraph.numVertices()); // just to keep track of progress
            }
        }
    }

    /**
     * Prints actors sorted by lowest/highest average separation
     * @param amt amount of actors to print. If negative, prints the highest, otherwise the lowest
     */
    public static void printBestActorsByAverageSeparation(int amt) {
        if (actorProfiles == null) getActorProfiles();
        if (actorProfilesBySeparation == null) {
            actorProfilesBySeparation = actorProfiles.subList(0, actorProfiles.size()); // shallow copy
            actorProfilesBySeparation.sort(Comparator.comparingDouble((ActorProfile a) -> a.averageSeparation));
        }

        if (amt < 0) {
            for (int i = actorProfilesBySeparation.size() - 1; i >= actorProfilesBySeparation.size() + amt; i--) {
                System.out.println(actorProfilesBySeparation.get(i).name + " - " + actorProfilesBySeparation.get(i).averageSeparation);
            }
        } else {
            for (int i = 0; i < amt; i++)
                System.out.println(actorProfilesBySeparation.get(i).name + " - " + actorProfilesBySeparation.get(i).averageSeparation);
        }
    }

    /**
     * Prints actors sorted by lowest/highest number of costars
     * @param amt amount of actors to print. If negative, prints the highest, otherwise the lowest
     */
    public static void printBestActorsByCostars(int amt) {
        if (actorProfiles == null) getActorProfiles();
        if (actorProfilesByCostars == null) {
            actorProfilesByCostars = actorProfiles.subList(0, actorProfiles.size()); // shallow copy
            actorProfilesByCostars.sort(Comparator.comparingInt((ActorProfile a) -> a.costars));
        }

        if (amt < 0) {
            for (int i = actorProfilesByCostars.size() - 1; i >= actorProfilesByCostars.size() + amt; i--) {
                System.out.println(actorProfilesByCostars.get(i).name + " - " + actorProfilesByCostars.get(i).costars);
            }
        } else {
            for (int i = 0; i < amt; i++)
                System.out.println(actorProfilesByCostars.get(i).name + " - " + actorProfilesByCostars.get(i).costars);
        }
    }

    public static void testCases() {
        System.out.println("Test cases:");

        // Create graph
        Graph<String, Character> g = new AdjacencyMapGraph<>();
        g.insertVertex("Kevin Bacon");
        g.insertVertex("Dartmouth (Earl thereof)");
        g.insertVertex("Alice");
        g.insertVertex("Charlie");
        g.insertVertex("Bob");
        g.insertVertex("Nobody");
        g.insertVertex("Nobody's Friend");

        g.insertUndirected("Kevin Bacon", "Alice", 'A');
        g.insertUndirected("Kevin Bacon", "Bob", 'A');
        g.insertUndirected("Bob", "Alice", 'A');
        g.insertUndirected("Bob", "Charlie", 'C');
        g.insertUndirected("Alice", "Charlie", 'D');
        g.insertUndirected("Dartmouth (Earl thereof)", "Charlie", 'B');
        g.insertUndirected("Nobody", "Nobody's Friend", 'B');

        // Find paths to Kevin Bacon
        Graph<String, Character> tree = BFS.bfs(g, "Kevin Bacon");

        System.out.println("Minimum path tree:");
        System.out.println(tree);

        // Finding paths
        List<String> path = BFS.getPath(tree, "Dartmouth (Earl thereof)");
        System.out.println("Path from Dartmouth to Kevin Bacon:");
        System.out.println(path);

        // Find path from disconnected node
        List<String> path2 = BFS.getPath(tree, "Nobody");
        System.out.println("Path from Nobody to Kevin Bacon:");
        System.out.println(path2);


        // Build universe from Kevin Bacon and print average separation and connected actors
        Universe un = new Universe("Kevin Bacon", true);

        // Print disconnected actors
        un.printUnconnectedActors();

    }

    public static void main(String[] args) throws IOException {
        loadDatasets();
        buildGraph();

        boolean quit = false;
        Scanner sc = new Scanner(System.in);
        Universe un = new Universe("Kevin Bacon", true);

        do {
            System.out.printf("%s game $ ", un.center);
            String line = sc.nextLine();
            String[] tokens = line.split(" ");
            String command = tokens[0];


            switch (command) {
                case "u": // change universe center
                    if (!actorsGraph.hasVertex(line.substring(2))) {
                        System.out.println("Actor not found!");
                        break;
                    }
                    un = new Universe(line.substring(2), true);
                    break;
                case "p": // change find path to actor
                    if (!actorsGraph.hasVertex(line.substring(2))) {
                        System.out.println("Actor not found!");
                        break;
                    }
                    un.printPathToActor(line.substring(2));
                    break;
                case "q": // quit game
                    quit = true;
                    break;
                case "i": // print list of unconnected actors
                    un.printUnconnectedActors();
                    break;
                case "c": // print X lowest/highest average separation actors
                    printBestActorsByAverageSeparation(Integer.parseInt(tokens[1]));
                    break;
                case "r": // print X lowest/highest number of costars actors
                    printBestActorsByCostars(Integer.parseInt(tokens[1]));
                    break;
                case "t": // run test cases
                    testCases();
                    break;
                default:
                    System.out.println("Command not recognized.");
            }
        } while (!quit);
    }
}
