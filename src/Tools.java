import java.awt.*;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Verschiedene hilfreiche Werkzeuge
 *
 * @author Philip Maus (https://github.com/getNameFromUser)
 */
public class Tools {

    private static final String FALSCHER_VERTEX_TYP_FEHLER = "Stattdessen werden jetzt Vertices aus dem Standard Vertex-Typ erstellt.\n" +
            "Achtung: Werden generische Aktionen verwendet, zum Beispiel eine Aktion 'kuerzesteWege2', die einen DijkstraVertex oder ähnliche erwartet, wird dies mit den jetzt erstellten Vertices nicht unterstützt!\n" +
            "Um das Problem zu beheben bitte einen Konstruktor in dem eigenen Vertex-Typ erstellen, der nur einen String übernimmt.\n" +
            "Alternativ dazu kann auch der Quelltext verändert werden, unter anderem in Tools.java:demoGraph.";

    /**
     * Befindet sich ein Element in der Liste
     *
     * @param pList    Liste
     * @param pElement Element
     * @param <T>      Typ des Elements
     * @return Ist das Element in der Liste
     */

    public static <T> boolean inListe(List<T> pList, T pElement) {
        pList.toFirst();
        while (pList.hasAccess() && pList.getContent() != pElement) pList.next();
        return pList.hasAccess();
    }

    /**
     * Kopiert eine Liste.
     *
     * @param pList Liste
     * @param <T>   Typ der Liste
     * @return Kopie der Liste
     */

    public static <T> List<T> kopiereListe(List<T> pList) {
        List<T> kopie = new List<>();
        for (pList.toFirst(); pList.hasAccess(); pList.next()) kopie.append(pList.getContent());
        return kopie;
    }

    /**
     * Gibt die Länge einer Liste zurück
     *
     * @param pList Liste
     * @return Länge der Liste
     */

    public static int listeLaenge(List<?> pList) {
        int i = 0;
        for (pList.toFirst(); pList.hasAccess(); pList.next()) i++;
        return i;
    }

    /**
     * Wandelt eine Liste in ein Array um.
     *
     * @param pList Liste
     * @param <T>   Typ der Liste
     * @return Typ-Array der Liste
     */

    public static <T> T[] alsArray(List<T> pList) {
        return alsArray(pList, listeLaenge(pList));
    }

    /**
     * Wandelt eine Liste in ein Array um
     *
     * @param pList   Liste
     * @param pLaenge Länge der Liste
     * @param <T>     Typ der Liste
     * @return Typ-Array der Liste
     */

    public static <T> T[] alsArray(List<T> pList, int pLaenge) {
        Object[] array = new Object[pLaenge];
        pList.toFirst();
        for (int i = 0; pList.hasAccess(); pList.next()) array[i++] = pList.getContent();
        //noinspection unchecked
        return (T[]) array;
    }

    /**
     * Wandelt eine Liste in ein Double-Array um
     *
     * @param pList Liste
     * @return Double-Array der Liste
     */

    public static Double[] alsDoubleArray(List<?> pList) {
        Object[] array = alsArray(pList);
        Double[] doubleArray = new Double[array.length];
        for (int i = 0; i < array.length; i++) doubleArray[i] = (double) array[i];
        return doubleArray;
    }

    /**
     * Sortiert ein Double-Array mit Mergesort
     *
     * @param array Array
     */

    public static void mergesort(Double[] array) {
        mergesort(array, 0, array.length - 1);
    }

    /**
     * Sortiert ein Double-Array mit Mergesort, wobei eine linke und eine rechte Grenze gesetzt wird-
     *
     * @param array  Array
     * @param links  Linke-Grenze
     * @param rechts Rechte-Grenze
     */

    public static void mergesort(Double[] array, int links, int rechts) {
        if (links < rechts) {// Wenn es noch was zu teilen gibt,
            int mitte = (links + rechts) / 2; // Setze die Mitte
            mergesort(array, links, mitte); // Sortiere die linke Hälfte
            mergesort(array, mitte + 1, rechts); // Sortiere die rechte Hälfte
            merge(array, links, rechts); // Füge die beiden Sortierten Hälften zusammen
        }
    }

    /**
     * Merge-Funktion der Mergesort-Methode
     *
     * @param array  Double-Array
     * @param links  Linke-Grenze
     * @param rechts Rechte-Grenze
     */

    private static void merge(Double[] array, int links, int rechts) {
        int mitte = (links + rechts) / 2; // Setze die Mitte
        Double[] array2 = new Double[mitte - links + 1]; // Kopiere linke Hälfte
        System.arraycopy(array, links, array2, 0, mitte + 1 - links);
        int i = links, j = mitte + 1, k = 0; // Sortierzeiger, Rechter Zeiger, Linker Zeiger
        while (i < j && j <= rechts)
            if (array[j].compareTo(array2[k]) >= 0) array[i++] = array2[k++];
            else array[i++] = array[j++];
        while (k < array2.length) array[i++] = array2[k++]; // Restlichen Links einfügen
    }

    /**
     * Erstellt einen zentrierten Text bei Punkt p
     *
     * @param g           Zeichenfläche
     * @param p           Hier wird der Text geschrieben
     * @param f           Schriftart
     * @param c           Farbe
     * @param s           Text
     * @param hintergrund Soll ein weißer Hintergrund gezeichnet werden
     */

    public static void zentrierterText(Graphics g, Point2D p, Font f, Color c, String s, boolean hintergrund) {
        Rectangle grenzen = g.getFontMetrics(f).getStringBounds(s, g).getBounds();
        g.setFont(f);
        g.setColor(Color.WHITE);
        if (hintergrund) g.fillRect((int) p.getX() - grenzen.width / 2, (int) p.getY() - grenzen.height / 2,
                grenzen.width, grenzen.height);
        g.setColor(c);
        g.drawString(s, (int) p.getX() - grenzen.x - grenzen.width / 2, (int) p.getY() - grenzen.y - grenzen.height / 2);
    }

    /**
     * Entfernt ein Element vom Typ T aus der Liste
     *
     * @param pListe   Liste
     * @param pElement Zu entfernendes Element
     * @param <T>      Typ des Elements
     */

    public static <T> void entferneAusListe(List<T> pListe, T pElement) {
        for (pListe.toFirst(); pListe.hasAccess(); pListe.next())
            if (pElement.equals(pListe.getContent())) {
                pListe.remove();
                pListe.toLast();
            }
    }

    public static Graph demoGraph() {
        return demoGraph(Vertex.class);
    }

    public static <V extends Vertex> Graph demoGraph(Class<V> pKlasse) {
        Graph neuerGraph = new Graph();
        Constructor<? extends Vertex> konstruktor = gibVertexKonstruktor(pKlasse, String.class);
        if (konstruktor == null)
            return neuerGraph;
        Vertex a = neuerVertex(konstruktor, "A"),
                b = neuerVertex(konstruktor, "B"),
                c = neuerVertex(konstruktor, "C"),
                d = neuerVertex(konstruktor, "D");
        neuerGraph.addVertex(a);
        neuerGraph.addVertex(b);
        neuerGraph.addVertex(c);
        neuerGraph.addVertex(d);
        neuerGraph.addEdge(new Edge(a, b, 10));
        neuerGraph.addEdge(new Edge(a, d, 20));
        neuerGraph.addEdge(new Edge(b, c, 40));
        neuerGraph.addEdge(new Edge(c, d, 50));
        neuerGraph.addEdge(new Edge(b, d, 60));
        return neuerGraph;
    }

    public static Constructor<? extends Vertex> gibVertexKonstruktor(Class<? extends Vertex> pKlasse, Class<?>... pParameterTypen) {
        if (pKlasse == null)
            return null;
        Constructor<? extends Vertex> constructor;
        try {
            constructor = pKlasse.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            System.err.println("Konnte Vertex vom Typ " + pKlasse.getName() + " nicht erstellen: Es gibt keinen Konstruktor, der " + Arrays.toString(pParameterTypen) + " übernimmt.\n");
            try {
                constructor = Vertex.class.getConstructor(String.class);
            } catch (NoSuchMethodException ex) {
                System.err.println("""
                        Anscheinend hat die Vertex.java Klasse auch keinen Konstruktor, der die oben genannte Parameter übernimmt:
                        Entweder ist die Originale Vertex.java-Datei nicht vorhanden, oder die Anforderungen des Abiturs haben sich geändert.
                        Im letzten Fall bitte ein Issue unter https://github.com/getNameFromUser/JGrapher/issues/ auf Github erstellen.
                        Der Demo-Graph wird nicht erstellt. Bitte beachte, dass jetzt die Anwendung wahrscheinlich nicht stabil ist.""");
                return null;
            }
        }
        return constructor;
    }

    public static Vertex neuerVertex(Constructor<? extends Vertex> pVertexKonstruktor, Object... pArgumente) {
        if (pVertexKonstruktor != null) try {
            return pVertexKonstruktor.newInstance(pArgumente);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            System.err.println("Konnte Konstruktor von " + pVertexKonstruktor.getDeclaringClass().getName() + " nicht ausführen, ist er privat?\n" + FALSCHER_VERTEX_TYP_FEHLER);
        }
        if (pArgumente.length > 0 && pArgumente[0] instanceof String) {
            return new Vertex((String) pArgumente[0]);
        } else throw new RuntimeException("Argumente sind nicht kompatibel mit dem Standard Vertex-Konstruktor");
    }
}
