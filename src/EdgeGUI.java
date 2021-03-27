import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.StringJoiner;

/**
 * Stellt eine Kante graphisch dar
 *
 * @author Philip Maus (https://github.com/getNameFromUser)
 * @param <V> Typ des Vertex
 */
@SuppressWarnings("unchecked")
public class EdgeGUI<V extends Vertex> {

    private final Edge edge;
    private final GraphPanel<V> panel;
    private boolean fokussiert = false;

    public EdgeGUI(String pString, GraphPanel<V> pPanel) {
        this.panel = pPanel;
        String[] parameter = pString.split(";");
        if (panel.gibGraph().getEdge(panel.gibGraph().getVertex(parameter[0]), panel.gibGraph().getVertex(parameter[1])) == null)
            panel.gibGraph().addEdge(new Edge(panel.gibGraph().getVertex(parameter[0]), panel.gibGraph().getVertex(parameter[1]), Double.parseDouble(parameter[2])));
        edge = panel.gibGraph().getEdge(panel.gibGraph().getVertex(parameter[0]), panel.gibGraph().getVertex(parameter[1]));
    }

    public EdgeGUI(Edge pEdge, GraphPanel<V> pPanel) {
        this.edge = pEdge;
        this.panel = pPanel;
    }

    public Point2D gerade(double p) {
        return new Point2D.Double(gibA().getX() + p * (gibB().getX() - gibA().getX()),
                gibA().getY() + p * (gibB().getY() - gibA().getY()));
    }

    public double schneidet(EdgeGUI<V> pAndere) {
        if (pAndere == this) return Double.NaN;
        double x1 = gibA().getX(), y1 = gibA().getY();
        double x2 = gibB().getX(), y2 = gibB().getY();
        double x3 = pAndere.gibA().getX(), y3 = pAndere.gibA().getY();
        double x4 = pAndere.gibB().getX(), y4 = pAndere.gibB().getY();
        double n = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        double za = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
        double zb = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);
        if (n == 0) return Double.NaN;
        za /= n;
        zb /= n;
        if (zb <= 0 || zb >= 1 || za <= 0 || za >= 1) return Double.NaN;
        return za;
    }

    public boolean trifftPunkt(Point2D ov2) {
        // Lineare Algebra FTW -> Neue Gerade: Punkt = Ortsvektor, Normalenvektor der Kante = Richtungsvektor:
        // Zugehöriges LGS:
        // ov2.x + r * rv2.x = ov1.x + s * rv1.x
        // ov2.y + r * rv2.y = ov1.y + s * rv1.y
        // => Die Formel für s ist das umgeformte LGS
        Point2D rv1 = new Point2D.Double(gibB().getX() - gibA().getX(), gibB().getY() - gibA().getY());
        Point2D ov1 = gibA(), rv2 = new Point2D.Double(-1 * rv1.getY(), rv1.getX()); // rv2 = Normalvektor von rv1
        double abstand;
        if (rv1.getY() == 0) {
            Point2D minX, maxX;
            if (gibA().getX() < gibB().getX()) {
                minX = gibA();
                maxX = gibB();
            } else {
                minX = gibB();
                maxX = gibA();
            }
            if (ov2.getX() < minX.getX()) abstand = ov2.distance(minX);
            else if (ov2.getX() > maxX.getX()) abstand = ov2.distance(maxX);
            else abstand = ov2.getY() - gibA().getY();
        } else  if (rv1.getX() == 0) {
            Point2D minY, maxY;
            if (gibA().getY() < gibB().getY()) {
                minY = gibA();
                maxY = gibB();
            } else {
                minY = gibB();
                maxY = gibA();
            }
            if (ov2.getY() < minY.getY()) abstand = ov2.distance(minY);
            else if (ov2.getY() > maxY.getY()) abstand = ov2.distance(maxY);
            else abstand = ov2.getX() - gibA().getX();
        } else {
            double s = ((ov1.getY() - ov2.getY()) * rv2.getX() / rv2.getY() + ov2.getX() - ov1.getX()) / (rv1.getX() - rv2.getX() * rv1.getY() / rv2.getY());
            Point2D schnittpunkt = s < 0 ? gibA() : s > 1 ? gibB() : new Point2D.Double(ov1.getX() + s * rv1.getX(), ov1.getY() + s * rv1.getY());
            abstand = schnittpunkt.distance(ov2);
        }

        return Math.abs(abstand) <= Konfiguration.STRICHSTAERKE + Konfiguration.PADDING;
    }

    public List<Double> schneidet(List<EdgeGUI<V>> kanten) {
        List<Double> schnitte = new List<>();
        for (kanten.toFirst(); kanten.hasAccess(); kanten.next()) {
            double s = schneidet(kanten.getContent());
            if (!Double.isNaN(s))
                schnitte.append(s);
        }
        return schnitte;
    }

    public Point2D gibA() {
        return panel.gibGUI((V) edge.getVertices()[0]).getLocation();
    }

    public Point2D gibB() {
        return panel.gibGUI((V) edge.getVertices()[1]).getLocation();
    }

    public Edge gibEdge() {
        return edge;
    }

    public void setzeFokussiert(boolean pWert) {
        fokussiert = pWert;
    }

    public boolean istFokussiert() {
        return fokussiert;
    }

    @Override
    public String toString() {
        return new StringJoiner(";")
                .add(edge.getVertices()[0].getID())
                .add(edge.getVertices()[1].getID())
                .add(String.valueOf(edge.getWeight()))
                .add(fokussiert ? "true" : "false")
                .toString();
    }

    public void zeigeDialog() {
        String name = "Kante " + edge.getVertices()[0].getID() + edge.getVertices()[1].getID();
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(panel), name, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel inhalt = new JPanel();
        inhalt.setBorder(new EmptyBorder(10, 10, 10, 10));
        inhalt.setLayout(new GridLayout(4, 2, 10, 10));
        inhalt.add(new JLabel("Kante zwischen:"));
        inhalt.add(new JLabel(edge.getVertices()[0].getID() + " und " +  edge.getVertices()[1].getID()));

        inhalt.add(new JLabel("Gewicht:"));
        JFormattedTextField gewicht = new JFormattedTextField(NumberFormat.getNumberInstance()); // = quasi JNumberField
        gewicht.setValue(edge.getWeight());
        gewicht.addActionListener(e -> {
            edge.setWeight(Double.parseDouble(gewicht.getText()));
            dialog.setVisible(false);
        });
        gewicht.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(gewicht::selectAll);
            }
        });
        inhalt.add(gewicht);

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
        JToggleButton wechseleMarkierung = new JToggleButton(edge.isMarked() ? "Ja" : "Nein");
        wechseleMarkierung.setSelected(edge.isMarked());
        wechseleMarkierung.addActionListener(e -> {
            edge.setMark(!edge.isMarked());
            wechseleMarkierung.setText(edge.isMarked() ? "Ja" : "Nein");
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
}
