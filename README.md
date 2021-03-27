# JGrapher
Ein Java-Swing Programm um Graphen anzuzeigen, zu manipulieren und einige vereinfachte Algorithmen auszuführen.

INFO: Das Programm ist auf Einsteiger in der Graphentheorie ausgerichtet und ein Lerntool. Die Anwendung ist nicht für Produktivumgebungen vorgesehen. Die Anwendung ist auf das Abitur NRW ab 2018 angepasst.

## Funktionen:
+ Graph anzeigen (GraphPanel), Graph bearbeiten (GraphFenster) und auf dem Graphen Algorithmen durchführen (GraphAnwendung).
+ Knoten/Kanten hinzufügen und löschen, bereits vorhandene Knoten und Kanten bearbeiten
+ Daten Im- und Export
+ Automatisches Anordnen der Knoten (Kreisform, zufällig und dynamisch)
+ Basierend auf Klassen für das Abitur NRW ab 2018 (beispielsweise werden statt den Java eigenen Listen immer die Abitur-Listen verwendet)
+ Java 8 und Java-Editor kompatibel
+ und mehr

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
    return null;
});

new GraphFenster<>().graphAktion("Alle fokussierten Vertices markieren", (graph, fokussiert) -> {
    for (Vertex v : fokussiert) v.setMarked(true);
    return "Alle fokussierten Vertices wurden markiert!";
});

new GraphFenster<>().graphAktion("Alle fokussierten Kanten markieren", (graph, knoten, kanten) -> {
    for (Edge e : kanten) e.setMarked(true);
    return null;
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

## Lizenz:

   Copyright 2021 Philip Maus (https://github.com/getNameFromUser/)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
