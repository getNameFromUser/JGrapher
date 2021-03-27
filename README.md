# JGrapher
Ein Java-Swing Programm um Graphen anzuzeigen, zu manipulieren und einige vereinfachte Algorithmen auszuführen.

INFO: Das Programm ist auf Einsteiger in der Graphentheorie ausgerichtet und ein Lerntool. Die Anwendung ist nicht für Produktivumgebungen vorgesehen. Die Anwendung ist auf das Abitur NRW ab 2018 angepasst.

## Aufbau
Folgendermaßen sind die GUI-Klassen strukturiert:
Alle Klassen haben einen generischen Typ. Der kann zu Beginn missachtet werden, wird aber dazu verwendetet, später eigene Klassen wie DijkstraVertex zu unterstützen. So wird beim Erstellen eines Knoten direkt ein DijkstraVertex erstellt, wenn dieser als generischer Typ übergeben wurde.
+ VertexGUI: GUI für die Vertices
+ EdgeGUI: GUI für die Kanten
+ GraphPanel: Fläche, auf der der Graph angezeigt wird.
+ GraphFenster: Oberfläche mit verschiedenen Manipulierungsfunktionen (neue Knoten/Kanten, Löschen, Neu Anordnen, Speichern/Laden etc.)
+ GraphAnwendung: Erstellt ein GraphFenster, das mit verschiedenen Algorithmen erweitert wurde (Dijkstra, Tiefensuche, Breitensuche etc.)
-> Eine neue Funktion wird durch die Methode(n) aktion hinzugefügt, an die ein Name und eine Methode übergeben wird. Die Methode kann als Parameter entweder garnichts, nur einen Graphen, einen Graphen und ein Array an markierten Knoten oder einen Graphen, ein Array an markierten Knoten und ein Array an markierten Kanten annehmen. Der String-Rückgabewert wird als Dialog angezeigt, ist er null wird kein Dialog geöffnet.
Beispiel:

```java
new GraphFenster<>().graphAktion("Alle Vertices Markieren", graph -> {
    graph.setAllVertexMarks(true);
});

new GraphFenster<>().graphAktion("Alle fokussierten Vertices markieren", (graph, fokussiert) -> {
    for (Vertex v : fokussiert) v.setMarked(true);
});

new GraphFenster<>().graphAktion("Alle fokussierten Kanten markieren", (graph, knoten, kanten) -> {
    for (Edge e : kanten) e.setMarked(true);
});
```

Eine Aufgabe zum Dijkstra-Algorithmus könnte beispielsweise so aussehen:

```java
public class GraphAnwendung {
    public static void main(String... args) {
        new GraphFenster<>(new Graph(), DijkstraVertex.class).graphAktion("Dijkstra-Algorithmus", (graph, fokussiert) -> {
            if (fokussiert.length == 2) return GraphAnwendung.dijkstra(graph, fokussiert[0], fokussiert[1]);
            else return "Bitte genau zwei Knoten auswählen.";
        });
    }

    public static String dijkstra(Graph graph, DijkstraVertex pStartVertex, DijkstraVertex pEndVertex) {
        // TODO Quelltext hier einfügen
    }
}
```
