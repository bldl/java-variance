package anthonisen.felix.graph;

public interface IDirectedGraph<T> {
    public Iterable<T> getInVertices(T vertex);

    public Iterable<T> getOutVertices(T vertex);

    public boolean addEdge(T initial, T terminal);

    public boolean addVertex(T vertex);

    public boolean containsVertex(T vertex);

    public Iterable<T> getVertices();
}
