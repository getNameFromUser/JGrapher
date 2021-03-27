import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Philip Maus (https://github.com/getNameFromUser)
 * @param <V> Vertex-Typ
 */

public class GraphFenster<V extends Vertex> extends JFrame {

    private final JFileChooser jfcOpenDialog = new JFileChooser(new File(System.getProperty("user.dir"))),
            jfcSaveDialog = new JFileChooser(new File(System.getProperty("user.dir")));
    private final JTextArea jTfKnotenliste = new JTextArea(), jTfKantenliste = new JTextArea();
    private final JToolBar unten = new JToolBar("Funktionen");
    private Constructor<V> eigenerKonstruktor;
    private GraphPanel<V> graphPanel;
    private VertexGUI<V> start = null;

    public GraphFenster() {
        this(new Graph());
    }

    public GraphFenster(Graph pGraph) {
        this(pGraph, null);
    }

    public GraphFenster(Graph pGraph, Class<V> pKlasse) {
        super("JGrapher");
        Class<V> klasse;
        if (pKlasse == null) {
            //noinspection unchecked
            List<V> list = (List<V>) pGraph.getVertices();
            list.toFirst();
            if (list.hasAccess()) //noinspection unchecked
                klasse = (Class<V>) list.getContent().getClass();
            else //noinspection unchecked
                klasse = (Class<V>) Vertex.class;
        } else klasse = pKlasse;
        try {
            eigenerKonstruktor = klasse.getConstructor(String.class);
        } catch (NoSuchMethodException ignored) {
            eigenerKonstruktor = null;
        }
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(920, 530);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);

        JPanel root = new JPanel(), filler = new JPanel();
        JToolBar oben = new JToolBar("Aktionen"), rechts = new JToolBar("Graph erzeugen", SwingConstants.VERTICAL);
        JToggleButton jbDynamisch = new JToggleButton(), jbKnoten = new JToggleButton(), jbEntfernen = new JToggleButton(),
                jbKanten = new JToggleButton();
        JButton jbLaden = new JButton(), jbNeu = new JButton(), jbSpeichern = new JButton(), jbZufall = new JButton(),
                jbKreisform = new JButton(), jBGraphAusListen = new JButton(), jbWechseln = new JButton();
        root.setLayout(new BorderLayout());
        jbNeu.addActionListener(this::neuerGraph);
        jbNeu.setIcon(new ImageIcon(getClass().getResource("/img/Neu.png")));
        jbNeu.setToolTipText("Neuer Graph");
        oben.add(jbNeu);

        jbLaden.addActionListener(this::dateiLaden);
        jbLaden.setIcon(new ImageIcon(getClass().getResource("/img/Laden.png")));
        jbLaden.setToolTipText("Graph laden");
        oben.add(jbLaden);

        jbSpeichern.addActionListener(this::dateiSpeichern);
        jbSpeichern.setIcon(new ImageIcon(getClass().getResource("/img/Speichern.png")));
        jbSpeichern.setToolTipText("Graph speichern");
        oben.add(jbSpeichern);
        oben.add(new JSeparator(SwingConstants.VERTICAL));jbKnoten.addActionListener(e -> {
            graphPanel.setCursor(jbKnoten.isSelected() ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
            jbKanten.setSelected(false);
            jbEntfernen.setSelected(false);
        });
        jbKnoten.setIcon(new ImageIcon(getClass().getResource("/img/Knoten.png")));
        jbKnoten.setToolTipText("Neuer Knoten");
        oben.add(jbKnoten);

        jbKanten.addActionListener(e -> {
            graphPanel.setCursor(jbKanten.isSelected() ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
            jbKnoten.setSelected(false);
            jbEntfernen.setSelected(false);
        });
        jbKanten.setIcon(new ImageIcon(getClass().getResource("/img/Kanten.png")));
        jbKanten.setToolTipText("Neue Kante");
        oben.add(jbKanten);

        jbEntfernen.addActionListener(e -> {
            graphPanel.setCursor(jbEntfernen.isSelected() ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
            jbKnoten.setSelected(false);
            jbKanten.setSelected(false);
        });
        jbEntfernen.setIcon(new ImageIcon(getClass().getResource("/img/Entfernen.png")));
        jbEntfernen.setToolTipText("Entfernen");
        oben.add(jbEntfernen);
        oben.add(new JSeparator(SwingConstants.VERTICAL));

        jbDynamisch.addActionListener(e -> graphPanel.setzeGravitation(jbDynamisch.isSelected()));
        jbDynamisch.setIcon(new ImageIcon(getClass().getResource("/img/Dynamisch.png")));
        jbDynamisch.setToolTipText("Dynamisch");
        oben.add(jbDynamisch);

        jbKreisform.addActionListener(e -> graphPanel.berechnePunkteKreis());
        jbKreisform.setIcon(new ImageIcon(getClass().getResource("/img/Kreisform.png")));
        jbKreisform.setToolTipText("Kreisform");
        oben.add(jbKreisform);

        jbZufall.addActionListener(e -> graphPanel.berechnePunkteZufall());
        jbZufall.setIcon(new ImageIcon(getClass().getResource("/img/Zufall.png")));
        jbZufall.setToolTipText("Zufällig");
        oben.add(jbZufall);

        jbWechseln.addActionListener(e -> {
            VertexGUI<V>[] fokussiert = graphPanel.gibFokussierteKnoten();
            if (fokussiert.length != 0) {
                boolean setzenAuf = !fokussiert[0].gibVertex().isMarked();
                for (VertexGUI<V> v : fokussiert) v.gibVertex().setMark(setzenAuf);
            } else graphPanel.gibGraph().setAllVertexMarks(!graphPanel.gibGraph().allVerticesMarked());
            graphPanel.repaint();
        });
        jbWechseln.setIcon(new ImageIcon(getClass().getResource("/img/Wechseln.png")));
        jbWechseln.setToolTipText("Ausgewählte Knoten (de)markieren)");
        oben.add(jbWechseln);


        graphPanel = new GraphPanel<>(pGraph);
        graphPanel.setBounds(16, 64, 600, 400);
        graphPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (jbEntfernen.isSelected()) {
                    Point2D mausPosition = graphPanel.gibMausPosition(e.getX(), e.getY());
                    VertexGUI<V> knoten = graphPanel.knotenBei(mausPosition);
                    EdgeGUI<V> kante = graphPanel.kanteBei(mausPosition);
                    if (knoten != null) graphPanel.entferneKnoten(knoten);
                    else if (kante != null) graphPanel.entferneKante(kante);
                } else if (jbKnoten.isSelected()) {
                    Point2D mausPosition = graphPanel.gibMausPosition(e.getX(), e.getY());
                    graphPanel.neuerVertex(neuerVertex(freierName()), mausPosition);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (jbKanten.isSelected())
                    start = graphPanel.knotenBei(graphPanel.gibMausPosition(e.getX(), e.getY()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (jbKanten.isSelected()) {
                    VertexGUI<V> knoten = graphPanel.knotenBei(graphPanel.gibMausPosition(e.getX(), e.getY()));
                    graphPanel.male(null, null);
                    if (jbKanten.isSelected() && start != null && knoten != null && start != knoten &&
                            graphPanel.gibGraph().getEdge(start.gibVertex(), knoten.gibVertex()) == null) {
                        EdgeGUI<V> edge = graphPanel.neueKante(start, knoten);
                        graphPanel.repaint();
                        edge.zeigeDialog();
                    }
                    start = null;
                    graphPanel.repaint();
                }
            }
        });
        graphPanel.setzeMausbewegung(position -> {
            if (jbKanten.isSelected() && start != null) {
                graphPanel.male(start, position);
                graphPanel.repaint();
                return true;
            } else return false;
        });
        root.add(graphPanel, BorderLayout.CENTER);

        JLabel lKnotenliste = new JLabel(), lKantenliste = new JLabel();
        lKnotenliste.setText("Knotenliste:");
        rechts.add(lKnotenliste);

        jTfKnotenliste.setText("A,B,C,D,E,F");
        rechts.add(jTfKnotenliste);

        lKantenliste.setText("Kantenliste:");
        rechts.add(lKantenliste);

        jTfKantenliste.setText("AB1,AC1,BC1,BD1,CD1,BE1,CF1,DE1,DF1");
        rechts.add(jTfKantenliste);

        jBGraphAusListen.setText("Graph aus Listen erzeugen");
        jBGraphAusListen.addActionListener(this::graphAusListe);
        rechts.add(jBGraphAusListen);
        filler.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        rechts.add(filler);

        root.add(oben, BorderLayout.PAGE_START);
        root.add(rechts, BorderLayout.EAST);
        root.add(unten, BorderLayout.PAGE_END);
        setContentPane(root);

        setVisible(true);
        graphPanel.repaint();
    }

    public static void main(String[] args) {
        new GraphFenster<>();
    }

    @SuppressWarnings("unused")
    public void setzeGraph(Graph pGraph) {
        graphPanel.setzeGraph(pGraph);
    }

    public String ladenDialog() {
        jfcOpenDialog.setDialogTitle("Öffne Datei");
        return jfcOpenDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? jfcOpenDialog.getSelectedFile().getPath() : null;
    }

    public String speichernDialog() {
        jfcSaveDialog.setDialogTitle("Speichere Datei");
        return jfcSaveDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ? jfcSaveDialog.getSelectedFile().getPath() : null;
    }

    private String freierName() {
        for (char c = 'A'; c <= 'Z'; c++) if (graphPanel.gibGraph().getVertex("" + c) == null) return "" + c;
        return "?";
    }

    public void dateiLaden(ActionEvent evt) {
        String dateiname = ladenDialog();
        if (dateiname != null) try {
            graphPanel.laden(dateiname);
            graphPanel.setzeAlleVerticesFokussiert(false);
            graphPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datei konnte nicht geladen werden!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void dateiSpeichern(ActionEvent evt) {
        String dateiname = speichernDialog();
        if (dateiname != null) try {
            graphPanel.speichern(dateiname);
            graphPanel.setzeAlleVerticesFokussiert(false);
            graphPanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datei konnte nicht gespeichert werden!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void neuerGraph(ActionEvent evt) {
        if (JOptionPane.showConfirmDialog(this, "Aktuellen Graphen verwerfen?", "Graph verwerfen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            graphPanel.setzeGraph(new Graph());
            graphPanel.repaint();
        }
    }

    private void graphAusListe(ActionEvent evt) {
        Graph g = new Graph();
        for (String s : jTfKnotenliste.getText().split(","))
            g.addVertex(neuerVertex(s));
        for (String s : jTfKantenliste.getText().split(","))
            try {
                g.addEdge(new Edge(g.getVertex(s.substring(0, 1)), g.getVertex(s.substring(1, 2)),
                        Double.parseDouble(s.substring(2))));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Bitte eine mit ganze Zahl oder eine mit Komma getrennte Zahl eingeben.");
            }

        graphPanel.setzeGraph(g);
        graphPanel.repaint();
    }

    private V neuerVertex(String pId) {
        if (eigenerKonstruktor != null) try {
            return eigenerKonstruktor.newInstance(pId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            eigenerKonstruktor = null;
        }
        //noinspection unchecked
        return (V) new Vertex(pId);
    }

    private Vertex[] gibFokussierteVertices() {
        VertexGUI<V>[] vertexGUIs = graphPanel.gibFokussierteKnoten();
        Vertex[] vertices = new Vertex[vertexGUIs.length];
        for (int i = 0; i < vertices.length; i++) vertices[i] = vertexGUIs[i].gibVertex();
        return vertices;
    }

    private Edge[] gibFokussierteEdges() {
        EdgeGUI<V>[] edgeGUIs = graphPanel.gibFokussierteKanten();
        Edge[] edges = new Edge[edgeGUIs.length];
        for (int i = 0; i < edges.length; i++) edges[i] = edgeGUIs[i].gibEdge();
        return edges;
    }

    ///////////////////
    // Neue Aktionen //
    ///////////////////

    @SuppressWarnings("unused")
    public GraphFenster<V> aktion(String pName, Aktion pAktion) {
        return aktion(pName, (graph, fokussierteKnoten, fokussierteKanten) -> pAktion.funktion());
    }

    public GraphFenster<V> aktion(String pName, GraphAktion pAktion) {
        return aktion(pName, (graph, fokussierteKnoten, fokussierteKanten) -> pAktion.funktion(graph));
    }

    public GraphFenster<V> aktion(String pName, VertexAktion pAktion) {
        return aktion(pName, (graph, fokussierteKnoten, fokussierteKanten) -> pAktion.funktion(graph, fokussierteKnoten));
    }

    public GraphFenster<V> aktion(String pName, VertexKantenAktion pAktion) {
        JButton neueFunktion = new JButton(pName);
        neueFunktion.addActionListener(e -> {
            String text = pAktion.funktion(graphPanel.gibGraph(), gibFokussierteVertices(), gibFokussierteEdges());
            if (text != null) JOptionPane.showMessageDialog(this, text);
        });
        unten.add(neueFunktion);
        return this;
    }

    public interface Aktion {
        String funktion();
    }

    public interface GraphAktion {
        String funktion(Graph graph);
    }

    public interface VertexAktion {
        String funktion(Graph graph, Vertex[] fokussierteKnoten);
    }

    public interface VertexKantenAktion {
        String funktion(Graph graph, Vertex[] fokussierteKnoten, Edge[] fokussierteKanten);
    }
}
