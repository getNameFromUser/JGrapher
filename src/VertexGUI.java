import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.StringJoiner;

/**
 * Vertex graphisch darstellen
 *
 * Die VertexGUI stellt ein Vertex des Graphen als Java Swing Komponente dar.
 * Sowohl "normale" Vertices, wie auch später DijkstraVertex werden unterstützt.
 *
 * @author Philip Maus (https://github.com/getNameFromUser)
 * @param <V> Vertex; Entweder "normaler" Vertex oder später DijkstraVertex
 */

public class VertexGUI<V extends Vertex> extends JComponent {

    private final Point2D kraft;
    private final GraphPanel<V> panel; // Zugehöriges Panel
    private final V vertex; // Zugehöriges Graph-Vertex
    private boolean fokussiert; // Ausgewählt?

    /**
     * Konstruktor des Graphischen Vertex -> Import aus String
     * @param pString Export-String
     * @param panel Zugehöriges Panel
     */
    public VertexGUI(String pString, GraphPanel<V> panel) {
        String[] parameter = pString.split(";");
        setLocation(Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]));
        kraft = new Point2D.Double(Double.parseDouble(parameter[2]), Double.parseDouble(parameter[3]));
        if (panel.gibGraph().getVertex(parameter[4]) == null) panel.gibGraph().addVertex(new Vertex(parameter[4]));
        //noinspection unchecked
        vertex = (V) panel.gibGraph().getVertex(parameter[4]);
        fokussiert = parameter[5].equalsIgnoreCase("true");
        this.panel = panel;
        init();
    }

    /**
     * Konstruktor des graphischen Vertex aus bereits vorhandenem Graph-Vertex
     *
     * @param vertex Vertex des Graphs
     * @param panel Zugehöriges Panel
     */

    public VertexGUI(V vertex, GraphPanel<V> panel) {
        this.kraft = new Point2D.Double();
        this.vertex = vertex;
        this.panel = panel;
        init();
    }

    public void init() {
        setBorder(BorderFactory.createTitledBorder("VertexGUI"));
        int r = Konfiguration.KNOTEN_RADIUS + Konfiguration.PADDING / 2;
        setBounds(getX() - r, getY() - r, 2 * r, 2 * r);
        setPreferredSize(new Dimension(getX() + r, getY() + r));
    }

    /**
     * Begrenzungen des Vertex
     *
     * @return Grenzen des Vertex
     */

    public Rectangle2D getBounds2D() {
        double d = Konfiguration.KNOTEN_RADIUS + Konfiguration.PADDING / 2.0;
        return new Rectangle2D.Double(getX() - d, getY() - d, 2 * d, 2 * d);
    }

    public Point2D berechneKraft(VertexGUI<V> anderer) {
        double dx = getX() - anderer.getX();
        double dy = getY() - anderer.getY();
        double d = Math.sqrt(dx * dx + dy * dy), f;
        if (d == 0.0) return new Point2D.Double();
        if (Tools.inListe(panel.gibGraph().getNeighbours(vertex), anderer.gibVertex())) {
            f = (Konfiguration.KNOTEN_RADIUS * 4 - d) * 5;
        } else {
            f = Konfiguration.KNOTEN_RADIUS;
            if (d < Konfiguration.KNOTEN_DURCH)
                f += (Konfiguration.KNOTEN_DURCH - d) * 4;
        }
        f /= 20.0;
        return new Point2D.Double(dx / d * f, dy / d * f);
    }

    public boolean trifftPunkt(Point2D p) {
        return p != null && p.distance(getLocation()) < (Konfiguration.KNOTEN_RADIUS + Konfiguration.STRICHSTAERKE / 2.);
    }

    public V gibVertex() {
        return vertex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(vertex.isMarked() ? Color.RED : Color.WHITE);
        g.fillOval(getX() - Konfiguration.KNOTEN_RADIUS, getY() - Konfiguration.KNOTEN_RADIUS,
                Konfiguration.KNOTEN_RADIUS * 2, Konfiguration.KNOTEN_RADIUS * 2);
        g.setColor(Color.BLACK);
        if (istFokussiert()) {
            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(Konfiguration.STRICHSTAERKE * 2));
            g2d.drawOval(getX() - Konfiguration.KNOTEN_RADIUS + Konfiguration.STRICHSTAERKE / 2,
                    getY() - Konfiguration.KNOTEN_RADIUS + Konfiguration.STRICHSTAERKE / 2,
                    Konfiguration.KNOTEN_RADIUS * 2 - Konfiguration.STRICHSTAERKE,
                    Konfiguration.KNOTEN_RADIUS * 2 - Konfiguration.STRICHSTAERKE);
            g2d.setStroke(old);
        } else g.drawOval(getX() - Konfiguration.KNOTEN_RADIUS, getY() - Konfiguration.KNOTEN_RADIUS,
                Konfiguration.KNOTEN_RADIUS * 2, Konfiguration.KNOTEN_RADIUS * 2);
        Tools.zentrierterText(g, getLocation(), Konfiguration.SCHRIFTART, Color.BLACK, vertex.getID(), false);
    }

    public void setzeFokussiert(boolean pWert) {
        this.fokussiert = pWert;
    }

    public boolean istFokussiert() {
        return fokussiert;
    }

    /**
     * Als String-Exportieren. Kann mit einem Konstruktor aus dem String wiederhergestellt werden
     *
     * @return Export-String
     */
    @Override
    public String toString() {
        return new StringJoiner(";").add(String.valueOf(getX()))
                .add(String.valueOf(getY()))
                .add(String.valueOf(kraft.getX()))
                .add(String.valueOf(kraft.getY()))
                .add(vertex.getID())
                .add(fokussiert ? "true" : "false").toString();
    }

    public void zeigeDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(panel), "Vertex " + vertex.getID(), Dialog.ModalityType.APPLICATION_MODAL);
        JPanel inhalt = new JPanel();
        inhalt.setBorder(new EmptyBorder(10, 10, 10, 10));
        inhalt.setLayout(new GridLayout(4, 2, 10, 10));
        inhalt.add(new JLabel("Vertex vom Typ:"));
        inhalt.add(new JLabel(vertex.getClass().getName()));

        inhalt.add(new JLabel("ID: "));
        inhalt.add(new JLabel(vertex.getID()));

        inhalt.add(new JLabel("Fokussiert:"));
        JToggleButton wechseleFokussiert = new JToggleButton(istFokussiert() ? "Ja" : "Nein");
        wechseleFokussiert.setSelected(istFokussiert());
        wechseleFokussiert.addChangeListener(e -> {
            setzeFokussiert(!istFokussiert());
            wechseleFokussiert.setText(istFokussiert() ? "Ja" : "Nein");
            panel.repaint();
        });
        inhalt.add(wechseleFokussiert);

        inhalt.add(new JLabel("Markiert:"));
        JToggleButton wechseleMarkierung = new JToggleButton(vertex.isMarked() ? "Ja" : "Nein");
        wechseleMarkierung.setSelected(vertex.isMarked());
        wechseleMarkierung.addActionListener(e -> {
            vertex.setMark(!vertex.isMarked());
            wechseleMarkierung.setText(vertex.isMarked() ? "Ja" : "Nein");
            panel.repaint();
        });
        inhalt.add(wechseleMarkierung);
        dialog.setContentPane(inhalt);
        dialog.pack();
        dialog.setLocation(-dialog.getWidth()/2, -dialog.getHeight()/2);
        dialog.setLocationRelativeTo(panel);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    /**
     * Gibt die Kraft für die dynamische Ausrichtung zurück
     * @return Kraft für die dynamische Ausrichtung
     */

    public Point2D getKraft() {
        return kraft;
    }
}
