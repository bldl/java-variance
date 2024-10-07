package anthonisen.felix.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import anthonisen.felix.util.Pair;

public class ClassHierarchyGraph<T> implements IDirectedGraph<T> {

    List<T> vertices = new ArrayList<>();
    Map<T, Pair<List<T>, List<T>>> neighbourhoods = new HashMap<>();

    @Override
    public Iterable<T> getInVertices(T vertex) {
        return neighbourhoods.get(vertex).second;
    }

    @Override
    public Iterable<T> getOutVertices(T vertex) {
        return neighbourhoods.get(vertex).first;
    }

    @Override
    public boolean addEdge(T initial, T terminal) {
        if (!containsVertex(initial) || !containsVertex(terminal))
            return false;
        neighbourhoods.get(initial).first.add(terminal);
        neighbourhoods.get(terminal).second.add(initial);
        return true;

    }

    @Override
    public boolean addVertex(T vertex) {
        if (vertices.contains(vertex))
            return false;
        vertices.add(vertex);
        neighbourhoods.put(vertex, new Pair<>(new ArrayList<>(), new ArrayList<>()));
        return true;
    }

    @Override
    public boolean containsVertex(T vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public Iterable<T> getVertices() {
        return vertices;
    }
}
