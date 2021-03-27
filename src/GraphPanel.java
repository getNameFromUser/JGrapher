import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.StringJoiner;

/**
 * @author Philip Maus (https://github.com/getNameFromUser)
 * @param <V>
 */

@SuppressWarnings("unchecked")
public class GraphPanel<V extends Vertex> extends JPanel {

    private Graph graph;
    private List<VertexGUI<V>> vertices = new List<>();
    private double zoom = 1;
    private final Timer timer = new Timer(33, e -> {
        berechnePunkteDynamisch();
        repaint();
    });
    private List<EdgeGUI<V>> edges = new List<>();
    private AffineTransform transformation;
    private boolean gravitation = false;
    private VertexGUI<V> aktuellerKnoten, tempMalenStart;
    private Point2D tempMalenZiel;
    private final Point2D zoomOffset = new Point2D.Double(0, 0);
    private MausBewegung bewegung = null;

    public GraphPanel(Graph pGraph) {
        setzeGraph(pGraph);
        setBackground(Color.WHITE);
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                if (gravitation) timer.start();
            }

            public void componentHidden(ComponentEvent evt) {
                timer.stop();
            }

            public void componentResized(ComponentEvent evt) {
                transformation = null;
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1)
                    aktuellerKnoten = knotenBei(gibMausPosition(evt.getX(), evt.getY()));
                else {
                    zoom = 1;
                    zoomOffset.setLocation(0, 0);
                    transformation = null;
                    repaint();
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1)
                    aktuellerKnoten = null;
            }

            public void mouseClicked(MouseEvent evt) {
                VertexGUI<V> vertexZiel = knotenBei(gibMausPosition(evt.getX(), evt.getY()));
                if (vertexZiel != null) {
                    if (evt.getClickCount() == 1) {
                        vertexZiel.setzeFokussiert(!vertexZiel.istFokussiert());
                        vertexZiel.repaint();
                        repaint();
                    } else doppelklickAuf(vertexZiel);
                } else {
                    EdgeGUI<V> edgeZiel = kanteBei(gibMausPosition(evt.getX(), evt.getY()));
                    if (edgeZiel != null) {
                        if (evt.getClickCount() == 1) {
                            edgeZiel.setzeFokussiert(!edgeZiel.istFokussiert());
                            repaint();
                        } else doppelklickAuf(edgeZiel);
                    } else {
                        setzeAlleKantenFokussiert(false);
                        setzeAlleVerticesFokussiert(false);
                    }
                }
            }
        });
        addMouseWheelListener(e -> {
            zoom -= 0.1 * e.getWheelRotation();
            //zoomOffset.setLocation(e.getX() - getWidth()/2., e.getY() - getHeight()/2.);
            if (zoom <= 0) zoom = 0.1;
            transformation = null;
            repaint();
        });
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent evt) {
                mausBewegt(gibMausPosition(evt.getX(), evt.getY()));
            }

            public void mouseMoved(MouseEvent evt) {
                mausBewegt(gibMausPosition(evt.getX(), evt.getY()));
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("GraphPanel");
        frame.setSize(480, 480);
        frame.setContentPane(new GraphPanel<>(Tools.demoGraph()));
        frame.setVisible(true);
    }

    private void doppelklickAuf(EdgeGUI<V> pEdge) {
        pEdge.zeigeDialog();
    }

    public void doppelklickAuf(VertexGUI<V> pVertex) {
        pVertex.zeigeDialog();
    }

    /**
     * Transformiert die Position des Mauszeigers auf die Zeichenfläche
     * <p>
     * Das Koordinatensystem der Zeichenfläche und der Mausfläche unterscheiden sich. Mit dieser Methode wird die Maus-
     * Position mithilfe von AffineTransform auf die Position im Zeichen-Feld übertragen
     *
     * @param x X-Koordinate der Maus
     * @param y Y-Koordinate der Maus
     * @return Punkt auf Zeichenfläche
     */
    protected Point2D gibMausPosition(double x, double y) {
        AffineTransform tr;
        try {
            tr = getTransformation().createInverse();
        } catch (NoninvertibleTransformException exc) {
            return null;
        }
        return tr.transform(new Point2D.Double(x, y), null);
    }

    public void setzeMausbewegung(MausBewegung pMausBewegung) {
        bewegung = pMausBewegung;
    }

    protected void mausBewegt(Point2D mausPosition) {
        transformation = getTransformation();
        if (!bewegung.mausBewegt(mausPosition)) {
            if (aktuellerKnoten == null || !aktuellerKnoten.trifftPunkt(mausPosition)) {
                aktuellerKnoten = null;
            } else if (aktuellerKnoten != null) {
                aktuellerKnoten.setLocation((int) mausPosition.getX(), (int) mausPosition.getY());
                transformation = null;
            }
            repaint();
        }
    }

    public EdgeGUI<V> gibGUI(Edge pEdge) {
        edges.toFirst();
        while (edges.hasAccess() && edges.getContent().gibEdge() != pEdge)
            edges.next();
        return edges.getContent();
    }

    public VertexGUI<V> gibGUI(V pVertex) {
        vertices.toFirst();
        while (vertices.hasAccess() && vertices.getContent().gibVertex() != pVertex)
            vertices.next();
        return vertices.getContent();
    }

    public Graph gibGraph() {
        return graph;
    }

    public AffineTransform getTransformation() {
        if (transformation != null)
            return transformation;
        Rectangle2D grenzen = null;
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            grenzen = grenzen == null ? vertices.getContent().getBounds2D() : grenzen.createUnion(vertices.getContent().getBounds2D());

        if (grenzen == null)
            grenzen = new Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0);

        transformation = new AffineTransform();

        double skalierung = Math.min(getWidth() / grenzen.getWidth(), getHeight() / grenzen.getHeight()) * zoom;
        transformation.translate(getWidth() / 2.0, getHeight() / 2.0);
        transformation.scale(skalierung, skalierung);

        double vX = grenzen.getX() + grenzen.getWidth() / 2.0 + zoomOffset.getX(),
                vY = grenzen.getY() + grenzen.getHeight() / 2.0 + zoomOffset.getY();
        transformation.translate(-vX, -vY);

        return transformation;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = (Graphics2D) g;
        gr.setStroke(new BasicStroke(Konfiguration.STRICHSTAERKE));
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (vertices == null || vertices.isEmpty()) return;
        AffineTransform tr = new AffineTransform(getTransformation());
        tr.preConcatenate(gr.getTransform());
        gr.setTransform(tr);
        gr.setColor(Color.BLACK);
        List<EdgeGUI<V>> edgesKopie = Tools.kopiereListe(edges);
        for (edges.toFirst(); edges.hasAccess(); edges.next()) {
            List<Double> schnitte = edges.getContent().schneidet(edgesKopie);
            int index = -1;
            double l = edges.getContent().gibB().distance(edges.getContent().gibA());
            Double[] schnitteArray;
            if (l > 0.0) {
                double f = Konfiguration.KNOTEN_RADIUS / l, fInverse = 1.0 - f;
                schnitte.append(f);
                schnitte.append(fInverse);
                schnitteArray = Tools.alsDoubleArray(schnitte);
                Tools.mergesort(schnitteArray);
                int len = schnitteArray.length - 1;
                double maxDist = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < len; i++) {
                    double cs = schnitteArray[i], ns = schnitteArray[i + 1];
                    if (cs >= f) {
                        if (ns > fInverse) i = len;
                        double d = ns - cs;
                        if (d > maxDist) {
                            index = i;
                            maxDist = d;
                        }
                    }
                }
            } else schnitteArray = Tools.alsDoubleArray(schnitte);
            Point2D pa = edges.getContent().gibA(), pb = edges.getContent().gibB(), pos;
            g.setColor(edges.getContent().gibEdge().isMarked() ? Color.RED : Color.BLACK);
            Stroke vorherig = gr.getStroke();
            if (edges.getContent().istFokussiert())
                gr.setStroke(new BasicStroke(Konfiguration.STRICHSTAERKE * 2));
            g.drawLine((int) pa.getX(), (int) pa.getY(), (int) pb.getX(), (int) pb.getY());
            pos = edges.getContent().gerade(index != -1 && schnitteArray.length > index + 1 ? (schnitteArray[index] + schnitteArray[index + 1]) / 2 : 0.5);
            Tools.zentrierterText(gr, pos, Konfiguration.KLEINE_SCHRIFTART, Color.BLACK, new DecimalFormat("0.##").
                    format(edges.getContent().gibEdge().getWeight()), true);
            gr.setStroke(vorherig);
        }
        if (tempMalenStart != null && tempMalenZiel != null)
            gr.drawLine(tempMalenStart.getX(), tempMalenStart.getY(), (int) tempMalenZiel.getX(), (int) tempMalenZiel.getY());
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            vertices.getContent().paintComponent(g);

        if (vertices.isEmpty() && edges.isEmpty())
            Tools.zentrierterText(gr, new Point2D.Double(0, 0), Konfiguration.KLEINE_SCHRIFTART, Color.BLACK,
                    "Der Graph ist leer", true);
    }

    public void setzeAlleVerticesFokussiert(boolean pWert) {
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            vertices.getContent().setzeFokussiert(pWert);
    }

    public void setzeAlleKantenFokussiert(boolean pWert) {
        for (edges.toFirst(); edges.hasAccess(); edges.next())
            edges.getContent().setzeFokussiert(pWert);
    }

    public VertexGUI<V> knotenBei(Point2D point2D) {
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            if (vertices.getContent().trifftPunkt(point2D))
                return vertices.getContent();
        return null;
    }

    public VertexGUI<V>[] gibFokussierteKnoten() {
        List<VertexGUI<V>> knoten = new List<>();
        int i = 0;
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            if (vertices.getContent().istFokussiert()) {
                knoten.append(vertices.getContent());
                i++;
            }
        VertexGUI<V>[] v = new VertexGUI[i];
        i = 0;
        for (knoten.toFirst(); knoten.hasAccess(); knoten.next())
            v[i++] = knoten.getContent();
        return v;
    }

    public EdgeGUI<V>[] gibFokussierteKanten() {
        List<EdgeGUI<V>> kanten = new List<>();
        int i = 0;
        for (edges.toFirst(); edges.hasAccess(); edges.next())
            if (edges.getContent().istFokussiert()) {
                kanten.append(edges.getContent());
                i++;
            }
        EdgeGUI<V>[] e = new EdgeGUI[i];
        i = 0;
        for (kanten.toFirst(); kanten.hasAccess(); kanten.next())
            e[i++] = kanten.getContent();
        return e;
    }

    public void setzeGraph(Graph pGraph) {
        this.graph = pGraph;
        this.vertices = new List<>();
        List<V> vertices = (List<V>) pGraph.getVertices();
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            this.vertices.append(new VertexGUI<>(vertices.getContent(), this));
        this.edges = new List<>();
        List<Edge> edges = pGraph.getEdges();
        for (edges.toFirst(); edges.hasAccess(); edges.next())
            this.edges.append(new EdgeGUI<>(edges.getContent(), this));
        berechnePunkteKreis();
    }

    public double kreisRadius() {
        int len = Tools.listeLaenge(vertices);
        return len < 2 ? 0 : (len > 3 ? (Konfiguration.KNOTEN_RADIUS + Konfiguration.PADDING) * 3 + ++len / (2 * Math.PI) : Konfiguration.KNOTEN_RADIUS * 2);
    }

    public void berechnePunkteKreis() {
        if (vertices != null) {
            int i = 0, len = Tools.listeLaenge(vertices);
            double r = kreisRadius();

            for (vertices.toFirst(); vertices.hasAccess(); vertices.next()) {
                double winkel = (2 * Math.PI) * i / len;
                vertices.getContent().setLocation((int) (r * Math.sin(winkel)), (int) (-r * Math.cos(winkel)));
                i++;
            }
            transformation = null;
            repaint();
        }
    }

    public void berechnePunkteZufall() {
        Random rnd = new Random();
        double r = kreisRadius();
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            vertices.getContent().setLocation((int) (r * (rnd.nextDouble() * 2 - 1)), (int) (r * (rnd.nextDouble() * 2 - 1)));
        transformation = null;
        repaint();
    }

    public void berechnePunkteDynamisch() {
        for (int i = 0; i < Konfiguration.SCHRITTE; i++)
            berechnePunkteDynamisch(1./Konfiguration.SCHRITTE);
        transformation = null;
        repaint();
    }

    public void berechnePunkteDynamisch(double delta) {
        List<VertexGUI<V>> verticesKopie = Tools.kopiereListe(vertices);
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            vertices.getContent().getKraft().setLocation(0, 0);
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next()) {
            for (verticesKopie.toFirst(); verticesKopie.hasAccess(); verticesKopie.next()) {
                Point2D kraft = vertices.getContent().berechneKraft(verticesKopie.getContent());
                vertices.getContent().getKraft().setLocation(vertices.getContent().getKraft().getX() + kraft.getX(),
                        vertices.getContent().getKraft().getY() + kraft.getY());
            }
        }
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            if (vertices.getContent() != aktuellerKnoten)
                vertices.getContent().setLocation((int) (vertices.getContent().getX() + vertices.getContent().getKraft().getX() * delta),
                        (int) (vertices.getContent().getY() + vertices.getContent().getKraft().getY() * delta));
        repaint();
    }

    ////////////////////////
    // Graph Manipulation //
    ////////////////////////

    public EdgeGUI<V> kanteBei(Point2D point2D) {
        for (edges.toFirst(); edges.hasAccess(); edges.next())
            if (edges.getContent().trifftPunkt(point2D))
                return edges.getContent();
        return null;
    }

    /**
     * Entferne Knoten
     *
     * @param knoten Zu entfernender Knoten
     */
    public void entferneKnoten(VertexGUI<V> knoten) {
        List<Edge> kanten = graph.getEdges(knoten.gibVertex());
        for (kanten.toFirst(); kanten.hasAccess(); kanten.next())
            Tools.entferneAusListe(edges, gibGUI(kanten.getContent()));
        graph.removeVertex(knoten.gibVertex());
        Tools.entferneAusListe(vertices, knoten);
        transformation = null;
        repaint();
    }

    //////////////////////////
    // Daten im- und export //
    //////////////////////////

    /**
     * Entferne Kante
     *
     * @param kante Zu entfernende Kante
     */
    public void entferneKante(EdgeGUI<V> kante) {
        graph.removeEdge(kante.gibEdge());
        Tools.entferneAusListe(edges, kante);
        transformation = null;
        repaint();
    }

    /**
     * Graph in Datei speichern
     *
     * @param dateiname In diese Datei soll gespeichert werden
     * @throws IOException Fehler beim Speichern
     */

    public void speichern(String dateiname) throws IOException {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("Knoten:");
        for (vertices.toFirst(); vertices.hasAccess(); vertices.next())
            stringJoiner.add(vertices.getContent().toString());
        stringJoiner.add("Kanten:");
        for (edges.toFirst(); edges.hasAccess(); edges.next())
            stringJoiner.add(edges.getContent().toString());
        Files.write(Path.of(dateiname), Arrays.asList(stringJoiner.toString().split("\n")));
    }

    /**
     * Graph aus Datei lesen
     *
     * @param dateiname Hier ist der Graph gespeichert
     * @throws IOException Fehler beim Lesen
     */

    public void laden(String dateiname) throws IOException {
        String[] zeilen = Files.readAllLines(Paths.get(dateiname)).toArray(String[]::new);
        if (!zeilen[0].equalsIgnoreCase("Knoten:"))
            throw new IOException("Die übergebene Datei ist wahrscheinlich kein Graph-Export");
        graph = new Graph();
        int i;
        vertices = new List<>();
        edges = new List<>();
        for (i = 1; i < zeilen.length && !zeilen[i].equalsIgnoreCase("Kanten:"); i++)
            vertices.append(new VertexGUI<>(zeilen[i], this));
        for (i++; i < zeilen.length; i++)
            edges.append(new EdgeGUI<>(zeilen[i], this));
        setzeGraph(graph);
    }

    public void neuerVertex(V pVertex, Point2D position) {
        graph.addVertex(pVertex);
        VertexGUI<V> vertexGUI = new VertexGUI<>(pVertex, this);
        vertices.append(vertexGUI);
        if (position != null) vertexGUI.setLocation((int) position.getX(), (int) position.getY());
        setzeAlleVerticesFokussiert(false);
        setzeAlleKantenFokussiert(false);
        transformation = null;
        repaint();
    }

    public EdgeGUI<V> neueKante(VertexGUI<V> eins, VertexGUI<V> zwei) {
        Edge e = new Edge(eins.gibVertex(), zwei.gibVertex(), 0);
        gibGraph().addEdge(e);
        EdgeGUI<V> edgeGUI = new EdgeGUI<>(e, this);
        edges.append(edgeGUI);
        setzeAlleVerticesFokussiert(false);
        setzeAlleKantenFokussiert(false);
        repaint();
        return edgeGUI;
    }

    public void setzeGravitation(boolean pGravitation) {
        if (gravitation = pGravitation) timer.start();
        else timer.stop();
    }

    public void male(VertexGUI<V> pVon, Point2D pNach) {
        tempMalenStart = pVon;
        tempMalenZiel = pNach;
    }

    public interface MausBewegung {
        boolean mausBewegt(Point2D mausPunkt);
    }
}
