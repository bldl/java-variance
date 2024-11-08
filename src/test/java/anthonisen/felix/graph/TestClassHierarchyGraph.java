package anthonisen.felix.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;;

public class TestClassHierarchyGraph {
    IDirectedGraph<Integer> graph;

    @BeforeEach
    public void setUpGraph() {
        graph = new ClassHierarchyGraph<>();
        for (int i = 0; i < 5; ++i) {
            graph.addVertex(i);
        }
    }

    @Test
    public void testContainsVertex() {
        assertTrue(graph.containsVertex(2));
    }

    @Test
    public void testNotContainsVertex() {
        assertFalse(graph.containsVertex(10));
    }

    @Test
    public void testAddEdge() {
        assertTrue(graph.addEdge(2, 4));

        boolean found = false;
        for (int initial : graph.getInVertices(4))
            if (initial == 2)
                found = true;
        assertTrue(found);

        found = false;
        for (int terminal : graph.getOutVertices(2))
            if (terminal == 4)
                found = true;
        assertTrue(found);
    }

    @Test
    public void testCantAddEdge() {
        assertFalse(graph.addEdge(2, 10));
        assertFalse(graph.addEdge(10, 2));
        assertFalse(graph.addEdge(9, 10));
    }

    @Test
    public void testGetVertices() {
        Set<Integer> expectedVerts = Set.of(0, 1, 2, 3, 4), actualVerts = new HashSet<>();
        graph.getVertices().forEach(vertex -> actualVerts.add(vertex));
        assertEquals(expectedVerts, actualVerts);
    }
}
