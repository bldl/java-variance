package io.github.bldl.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.bldl.util.Pair;

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
        Set<T> visited = new HashSet<>();
        dfs(terminal, visited);
        if (visited.contains(initial)) {
            throw new IllegalArgumentException(
                    String.format("Adding an edge between %s and %s will create a cycle", initial,
                            terminal));
        }
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

    /**
     * Determines whether one node is a descendant of another
     * 
     * @param ancestor   the presumed ancestor
     * @param descendant the presumed descendant
     * @return whether {@code descendant} is a descendant of {@code ancestor}
     */
    public boolean isDescendant(T ancestor, T descendant) {
        Set<T> visited = new HashSet<>();
        dfs(ancestor, visited);
        return visited.contains(descendant);
    }

    private void dfs(T current, Set<T> visited) {
        if (visited.contains(current))
            return;
        visited.add(current);
        for (T vertex : getOutVertices(current)) {
            dfs(vertex, visited);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class Hierarchy Graph:\n");
        for (T vertex : vertices) {
            sb.append(vertex).append(" -> ");
            Iterable<T> outVertices = getOutVertices(vertex);
            boolean first = true;
            for (T outVertex : outVertices) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(outVertex);
                first = false;
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
