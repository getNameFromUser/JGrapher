import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

/**
 * Beschreibung
 *
 * @author Philip Maus (https://github.com/getNameFromUser)
 * @version 1.0 vom 27.03.2021
 */

public class GraphAnwendung {

    public static class DijkstraVertex extends Vertex {

        private Vertex predecessor;
        private double dist = Double.MAX_VALUE;

        public DijkstraVertex(String pID) {
            super(pID);
        }

        public Vertex getPredecessor() {
            return predecessor;
        }

        public double getDistance() {
            return dist;
        }

        public void setPredecessor(Vertex pPredecessor) {
            this.predecessor = pPredecessor;
        }

        public void setDistance(double dist) {
            this.dist = dist;
        }
    }


    public static void main(String[] args) {
        new GraphFenster<>(Tools.demoGraph(), DijkstraVertex.class).aktion("Eulerkreis", GraphAnwendung::eulerkreis).
                aktion("Tiefensuche", (graph, knoten) -> knoten.length == 2 ? (tiefensuche(graph, knoten[0], knoten[1]) ? "" : "Kein ") +
                        "Weg gefunden." : knoten.length == 1 ? tiefensuche(graph, knoten[0]) : "Bitte ein oder zwei Knoten auswählen.").
                aktion("Kürzester Weg", (graph, knoten) -> knoten.length == 2 ? new DecimalFormat("#0.0").
                        format(kuerzesterWeg(graph, knoten[0], knoten[1], Double.MAX_VALUE, 0)) : "Bitte zwei Knoten auswählen.").
                aktion("Traveling Salesman", g -> new DecimalFormat("#0.0").format(travelingSalesmanProblem(g))).
                aktion("Tiefendurchlauf (Stack)", (graph, knoten) -> knoten.length == 1 ? tiefensucheStack(graph, knoten[0]) : "Bitte einen Knoten auswählen.").
                aktion("Breitensuche", (graph, knoten) -> knoten.length == 1 ? breitensucheQueue(graph, knoten[0]) : "Bitte einen Knoten auswählen.").
                aktion("Krukskal", GraphAnwendung::kruskal).
                aktion("Adjazenzmatrix", g -> GraphAnwendung.gibAdjazenzmatrix(g).toString()).aktion("© Philip Maus 2021", g -> {
            try {
                java.awt.Desktop.getDesktop().browse(new URI("https://www.pmaus.de/?project=jgrapher"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static String eulerkreis(Graph pGraph) {
        boolean result = true;
        pGraph.setAllVertexMarks(false);
        List<Vertex> knotenliste = pGraph.getVertices();
        for (knotenliste.toFirst(); knotenliste.hasAccess(); knotenliste.next()) {
            int grad = 0;
            Vertex knoten = knotenliste.getContent();

            List<Vertex> nachbarliste = pGraph.getNeighbours(knoten);
            for (nachbarliste.toFirst(); nachbarliste.hasAccess(); nachbarliste.next()) grad++;

            if (grad % 2 != 0) {
                result = false;
                knoten.setMark(true);
            }
        }
        return "Es exisitert " + (result ? "ein Eulerkreis" : "kein Eulerkreis, da es Knoten mit unegeradem Grad gibt (markiert).");
    }

    private static boolean tiefensuche(Graph pGraph, Vertex start, Vertex ziel) {
        if (start.getID().equals(ziel.getID())) return true;
        start.setMark(true);

        List<Vertex> neighbours = pGraph.getNeighbours(start);
        for (neighbours.toFirst(); neighbours.hasAccess(); neighbours.next()) {
            Vertex neighbour = neighbours.getContent();
            if (!neighbour.isMarked() && pGraph.getEdge(start, neighbour).isMarked() && tiefensuche(pGraph, neighbour, ziel))
                return true;
        }

        start.setMark(false);
        return false;
    }

    public static String tiefensuche(Graph pGraph, Vertex startknoten) {
        startknoten.setMark(true);
        if (pGraph.allVerticesMarked()) return startknoten.getID();
        StringBuilder result = new StringBuilder(startknoten.getID());
        List<Vertex> nachbarliste = pGraph.getNeighbours(startknoten);
        for (nachbarliste.toFirst(); nachbarliste.hasAccess(); nachbarliste.next()) {
            Vertex nachbarknoten = nachbarliste.getContent();
            if (!nachbarknoten.isMarked()) {
                pGraph.getEdge(startknoten, nachbarknoten).setMark(true);
                result.append(tiefensuche(pGraph, nachbarknoten));
            }
        }
        return result.toString();
    }

    private static double kuerzesterWeg(Graph pGraph, Vertex start, Vertex ziel, double minDistanz, double distanz) {
        if (start.getID().equals(ziel.getID()))
            return Math.min(distanz, minDistanz);
        start.setMark(true);
        List<Vertex> nachbarn = pGraph.getNeighbours(start);
        for (nachbarn.toFirst(); nachbarn.hasAccess(); nachbarn.next())
            if (!nachbarn.getContent().isMarked())
                minDistanz = kuerzesterWeg(pGraph, nachbarn.getContent(), ziel, minDistanz,
                        distanz + pGraph.getEdge(start, nachbarn.getContent()).getWeight());
        start.setMark(false);
        return minDistanz;
    }

    private static double travelingSalesmanProblem(Graph pGraph, Vertex aktuellerknoten, Vertex ziel, double minDistanz, double distanz) {
        aktuellerknoten.setMark(true);

        if (pGraph.allVerticesMarked() && pGraph.getEdge(aktuellerknoten, ziel) != null) {
            double kantengewicht = pGraph.getEdge(aktuellerknoten, ziel).getWeight();
            if (distanz + kantengewicht < minDistanz) minDistanz = distanz + kantengewicht;
        } else {
            List<Vertex> nachbarliste = pGraph.getNeighbours(aktuellerknoten);
            for (nachbarliste.toFirst(); nachbarliste.hasAccess(); nachbarliste.next()) {
                Vertex nachbarknoten = nachbarliste.getContent();
                if (!nachbarknoten.isMarked()) minDistanz = travelingSalesmanProblem(pGraph, nachbarknoten, ziel,
                        minDistanz, distanz + pGraph.getEdge(aktuellerknoten, nachbarknoten).getWeight());
            }
        }
        aktuellerknoten.setMark(false);
        return minDistanz;
    }

    public static double travelingSalesmanProblem(Graph pGraph) {
        List<Vertex> knotenliste = pGraph.getVertices();
        knotenliste.toFirst();
        Vertex ziel = knotenliste.getContent();
        return travelingSalesmanProblem(pGraph, ziel, ziel, Double.MAX_VALUE, 0);
    }

    public static String tiefensucheStack(Graph pGraph, Vertex start) {
        Stack<Vertex> s = new Stack<>();
        StringBuilder result = new StringBuilder();
        s.push(start);
        start.setMark(true);
        while (!s.isEmpty()) {
            Vertex aktuellerknoten = s.top();
            List<Vertex> nachbarliste = pGraph.getNeighbours(aktuellerknoten);
            nachbarliste.toFirst();
            while (nachbarliste.hasAccess() && (nachbarliste.getContent()).isMarked())
                nachbarliste.next();
            if (nachbarliste.hasAccess()) {
                Vertex nachbarknoten = nachbarliste.getContent();
                nachbarknoten.setMark(true);
                pGraph.getEdge(aktuellerknoten, nachbarknoten).setMark(true);
                result.append(aktuellerknoten.getID()).append("-").append(nachbarknoten.getID()).append(", ");
                s.push(nachbarknoten);
            } else s.pop();
        }
        return result.toString();
    }

    public static String breitensucheQueue(Graph pGraph, Vertex start) {
        Queue<Vertex> q = new Queue<>();
        StringBuilder result = new StringBuilder();
        q.enqueue(start);
        start.setMark(true);
        while (!q.isEmpty()) {
            Vertex aktuellerknoten = q.front();
            List<Vertex> nachbarliste = pGraph.getNeighbours(aktuellerknoten);

            nachbarliste.toFirst();
            while (nachbarliste.hasAccess() && nachbarliste.getContent().isMarked()) nachbarliste.next();

            if (nachbarliste.hasAccess()) {
                Vertex nachbarknoten = nachbarliste.getContent();
                nachbarknoten.setMark(true);
                pGraph.getEdge(aktuellerknoten, nachbarknoten).setMark(true);
                result.append(aktuellerknoten.getID()).append("-").append(nachbarknoten.getID()).append(", ");
                q.enqueue(nachbarknoten);
            } else q.dequeue();
        }
        return result.toString();
    }

    public static String kruskal(Graph pGraph) {
        String result = "Kanten: ";

        List<Edge> edges = pGraph.getEdges();
        PriorityQueue<Edge> prioEdges = new PriorityQueue<>();
        for (edges.toFirst(); edges.hasAccess(); edges.next()) {
            Edge e = edges.getContent();
            prioEdges.enqueue(e, e.getWeight());
        }

        while (!prioEdges.isEmpty()) {
            Edge e = prioEdges.front();
            prioEdges.dequeue();
            pGraph.setAllVertexMarks(false);

            if (!e.isMarked() && !tiefensuche(pGraph, e.getVertices()[0], e.getVertices()[1])) e.setMark(true);
        }
        pGraph.setAllVertexMarks(false);
        return result;
    }

    public static Adjazenzmatrix gibAdjazenzmatrix(Graph pGraph) {
        List<Vertex> knoten = pGraph.getVertices();
        int anzahl = 0, nummer = 0;
        for (knoten.toFirst(); knoten.hasAccess(); knoten.next()) anzahl++;
        Adjazenzmatrix a = new Adjazenzmatrix(anzahl);
        for (knoten.toFirst(); knoten.hasAccess(); knoten.next()) {
            a.setzeID(nummer, knoten.getContent().getID());
            nummer++;
        }
        for (int x = 0; x < anzahl; x++)
            for (int y = 0; y < anzahl; y++) {
                Edge e = pGraph.getEdge(pGraph.getVertex(a.gibID(x)), pGraph.getVertex(a.gibID(y)));
                if (e != null) a.setzeEintrag(x, y, e.getWeight());
                else if (x != y) a.setzeEintrag(x, y, Double.MAX_VALUE);
            }
        return a;
    }

    public static class Adjazenzmatrix {
        private final String[] ids;
        private final double[][] matrix;
        private final int anzahl;

        public Adjazenzmatrix(int pAnzahl) {
            ids = new String[pAnzahl];
            matrix = new double[pAnzahl][pAnzahl];
            anzahl = pAnzahl;
        }

        public void setzeEintrag(int pZeile, int pSpalte, double pWert) {
            matrix[pSpalte][pZeile] = pWert;
        }

        public void setzeID(int pNummer, String pID) {
            ids[pNummer] = pID;
        }

        public String gibID(int pNummer) {
            return ids[pNummer];
        }

        private String format(String text) {
            return "     ".substring(text.length()) + text + "  ";
        }

        public String toString() {
            StringBuilder s = new StringBuilder(format("") + "|");
            for (int x = 0; x < anzahl; x++)
                s.append(format(ids[x])).append("|");

            s.append("\n");
            for (int i = 0; i < Math.max(0, anzahl + 1); i++)
                s.append("-------+");
            s.append("\n");

            for (int y = 0; y < anzahl; y++) {
                s.append(format(ids[y])).append("|");
                for (int x = 0; x < anzahl; x++) {
                    if (x == y) s.append(format("---"));
                    else if (matrix[x][y] == Double.MAX_VALUE) s.append(format("inf"));
                    else s.append(format("" + matrix[x][y]));
                    s.append("|");
                }
                s.append("\n");
                for (int i = 0; i < Math.max(0, anzahl + 1); i++)
                    s.append("-------+");
                s.append("\n");
            }
            return s.toString();
        }
    }
}
